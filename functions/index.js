const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions/v2/options");
const {defineSecret} = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const crypto = require("crypto");

initializeApp();
const db = getFirestore();

// TOPE DE GASTO: como maximo 5 instancias a la vez y ninguna encendida en reposo.
// Es el unico freno real al coste; el presupuesto de la consola solo manda avisos.
setGlobalOptions({
  region: "europe-west1",
  maxInstances: 5,
  minInstances: 0,
  timeoutSeconds: 60,
  memory: "256MiB",
});

// La clave vive en Secret Manager, nunca en el repo ni en el movil.
const GOOGLE_BOOKS_API_KEY = defineSecret("GOOGLE_BOOKS_API_KEY");

const BASE_URL = "https://www.googleapis.com/books/v1/volumes";
// Medido el 25-07-2026: la API de Books devuelve 503 en torno a la mitad de las
// peticiones. Con 3 intentos solo salian adelante 4 de cada 8 busquedas.
const MAX_INTENTOS = 6;
const ESPERA_BASE_MS = 400;
const ESPERA_MAX_MS = 2000;
// PRESUPUESTO DE TIEMPO. Sin esto, varias semillas encadenando 6 reintentos cada una
// se comian el timeout de la funcion y moria sin devolver nada: por eso la pantalla
// Home salia vacia en el primer arranque (instancia fria + Books dando 503).
// Se corta al llegar aqui y se devuelve lo que se tenga.
const PRESUPUESTO_MS = 40000;

// DOS NIVELES DE CACHE:
//  1) memoria de la instancia -> instantaneo, pero se pierde al reciclarse
//  2) Firestore -> compartido entre instancias y persistente
// El nivel 2 es el que de verdad quita la dependencia de que Books responda: con un
// 50% de 503 medido, lo ya consultado una vez deja de depender de la suerte.
// La coleccion no aparece en firestore.rules, asi que ningun cliente puede leerla;
// la funcion entra con el SDK admin, que se salta las reglas.
const COL_CACHE = "cache_libros";
const cache = new Map();
const CACHE_TTL_MS = 60 * 60 * 1000; // 1 hora: por debajo de esto se sirve tal cual
const CACHE_STALE_MS = 24 * 60 * 60 * 1000; // hasta 24h vale como red de seguridad
const CACHE_MAX_ENTRADAS = 300;

function cacheGet(clave) {
  const entrada = cache.get(clave);
  if (!entrada) return null;
  if (Date.now() - entrada.t > CACHE_TTL_MS) {
    return null;
  }
  return entrada.v;
}

// Resultado caducado pero utilizable: mejor devolver algo de hace unas horas
// que una pantalla vacia porque Books esta dando 503.
function cacheGetCaducado(clave) {
  const entrada = cache.get(clave);
  if (!entrada) return null;
  if (Date.now() - entrada.t > CACHE_STALE_MS) {
    cache.delete(clave);
    return null;
  }
  return entrada.v;
}

function cacheSet(clave, valor) {
  if (cache.size >= CACHE_MAX_ENTRADAS) {
    cache.delete(cache.keys().next().value); // saca la mas antigua
  }
  cache.set(clave, {v: valor, t: Date.now()});
}

const dormir = (ms) => new Promise((r) => setTimeout(r, ms));

// La API de Books devuelve 503 con frecuencia: reintentamos con espera creciente,
// pero sin pasarnos del plazo (limite) que nos deja el presupuesto de la invocacion.
function idDeCache(clave) {
  return crypto.createHash("sha1").update(clave).digest("hex");
}

//Nivel 2: lee de Firestore. Devuelve {datos, fresco} o null.
async function leerDeFirestore(clave) {
  try {
    const doc = await db.collection(COL_CACHE).doc(idDeCache(clave)).get();
    if (!doc.exists) return null;
    const d = doc.data();
    const edad = Date.now() - (d.creado || 0);
    if (edad > CACHE_STALE_MS) return null;
    return {datos: d.datos, fresco: edad <= CACHE_TTL_MS};
  } catch (e) {
    logger.warn("No se pudo leer la cache de Firestore", e);
    return null;
  }
}

async function guardarEnFirestore(clave, datos) {
  try {
    await db.collection(COL_CACHE).doc(idDeCache(clave)).set({
      datos, creado: Date.now(), consulta: clave.slice(0, 300),
    });
  } catch (e) {
    logger.warn("No se pudo guardar en la cache de Firestore", e);
  }
}

async function pedirABooks(params, apiKey, limite, maxIntentos = MAX_INTENTOS) {
  const url = new URL(BASE_URL);
  for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
  url.searchParams.set("key", apiKey);

  const clave = url.toString().replace(apiKey, "");
  const enMemoria = cacheGet(clave);
  if (enMemoria) return enMemoria;

  const persistida = await leerDeFirestore(clave);
  if (persistida && persistida.fresco) {
    cacheSet(clave, persistida.datos);
    return persistida.datos;
  }

  let espera = ESPERA_BASE_MS;
  for (let intento = 1; intento <= maxIntentos; intento++) {
    if (Date.now() >= limite) {
      logger.warn(`Sin tiempo tras ${intento - 1} intentos; se abandona la peticion`);
      break;
    }
    try {
      const res = await fetch(url, {signal: AbortSignal.timeout(8000)});
      if (res.ok) {
        const datos = await res.json();
        cacheSet(clave, datos);
        guardarEnFirestore(clave, datos); //sin await: no bloquea la respuesta
        return datos;
      }
      // 4xx que no sea 429 no se arregla reintentando (clave mala, query invalida...)
      if (res.status >= 400 && res.status < 500 && res.status !== 429) {
        logger.error("Books respondio " + res.status, {params});
        return cacheGetCaducado(clave);
      }
      logger.warn(`Books ${res.status}, intento ${intento}/${maxIntentos}`);
    } catch (e) {
      logger.warn(`Fallo de red, intento ${intento}/${maxIntentos}`, e);
    }
    if (intento < maxIntentos) {
      // espera creciente con algo de aleatoriedad, recortada si queda poco plazo
      const propuesta = Math.round(espera * (0.7 + Math.random() * 0.6));
      const restante = limite - Date.now();
      if (restante <= 0) break;
      await dormir(Math.min(propuesta, restante));
      espera = Math.min(espera * 2, ESPERA_MAX_MS);
    }
  }
  // Books no responde: mejor una respuesta vieja que una pantalla vacia
  const viejo = cacheGetCaducado(clave) || (persistida && persistida.datos);
  if (viejo) {
    logger.info("Books no responde; se sirve resultado cacheado antiguo");
    return viejo;
  }
  return null;
}

// Solo nos quedamos con los campos que la app usa: menos datos por la red
// y la clave y el resto de la respuesta no salen de aqui.
function aLibro(volume) {
  const info = volume && volume.volumeInfo;
  if (!info) return null;
  const img = info.imageLinks && info.imageLinks.thumbnail;
  if (!volume.id || !info.title || !info.authors || !info.authors.length ||
      !info.pageCount || !info.publishedDate || !info.categories ||
      !info.categories.length || !info.description || !img) {
    return null;
  }
  return {
    id: volume.id,
    titulo: info.title,
    autor: info.authors,
    numPag: info.pageCount,
    fechaPublicacion: info.publishedDate,
    generos: info.categories,
    descripcion: info.description,
    linkImg: img.replace("http://", "https://"),
  };
}

// Semillas de recomendacion. Deliberadamente son temas y autores reales:
// con palabras vacias tipo "El" o "De" casi ningun volumen trae descripcion,
// categorias ni portada, y el filtro los descartaba todos.
const SEMILLAS = [
  "subject:fiction", "subject:fantasy", "subject:science+fiction",
  "subject:mystery", "subject:thriller", "subject:romance",
  "subject:history", "subject:biography", "subject:poetry",
  "subject:philosophy", "subject:adventure", "subject:horror",
  "subject:juvenile+fiction", "subject:drama", "subject:classics",
  "novela", "cuentos", "aventuras", "misterio", "historia",
  "inauthor:Stephen+King", "inauthor:Agatha+Christie",
  "inauthor:Isaac+Asimov", "inauthor:Terry+Pratchett",
  "inauthor:J.K.+Rowling", "inauthor:Gabriel+Garcia+Marquez",
];
// Pocos reintentos por semilla y mas semillas: si una consulta da 503, probar otra
// distinta rinde mas que insistir en la misma, y ademas cachea mas variedad.
const MAX_CONSULTAS = 8;
const INTENTOS_POR_SEMILLA = 2;

function barajar(lista) {
  for (let i = lista.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [lista[i], lista[j]] = [lista[j], lista[i]];
  }
  return lista;
}

// LIMITE POR USUARIO. La funcion ya exige sesion, asi que el abuso realista es un
// usuario logueado machacandola. El contador vive en memoria de la instancia: con
// maxInstances=5 un atacante podria sacar hasta 5 veces el limite, pero combinado con
// el tope de instancias el gasto sigue acotado, que es lo que importa.
// (App Check seria lo suyo, pero exige registrar Play Integrity en consola primero.)
const MAX_LLAMADAS = 30;
const VENTANA_MS = 60 * 1000;
const contadores = new Map();

function exigirUsuario(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Hay que iniciar sesion.");
  }
  const uid = request.auth.uid;
  const ahora = Date.now();
  const previo = contadores.get(uid);

  if (!previo || ahora - previo.desde > VENTANA_MS) {
    contadores.set(uid, {desde: ahora, veces: 1});
    if (contadores.size > 500) {
      //poda para que el Map no crezca sin fin
      for (const [k, v] of contadores) {
        if (ahora - v.desde > VENTANA_MS) contadores.delete(k);
      }
    }
    return;
  }
  previo.veces++;
  if (previo.veces > MAX_LLAMADAS) {
    logger.warn(`Usuario ${uid} supera ${MAX_LLAMADAS} llamadas/min`);
    throw new HttpsError("resource-exhausted",
        "Demasiadas peticiones seguidas. Espera un momento.");
  }
}

// Busqueda por titulo
exports.buscarLibros = onCall(
    {secrets: [GOOGLE_BOOKS_API_KEY]},
    async (request) => {
      exigirUsuario(request);
      const consulta = String((request.data && request.data.query) || "").trim();
      if (!consulta) {
        throw new HttpsError("invalid-argument", "Falta el texto a buscar.");
      }
      const limite = Date.now() + PRESUPUESTO_MS;
      const datos = await pedirABooks({
        q: consulta.slice(0, 200),
        orderBy: "relevance",
        maxResults: "40",
        printType: "books",
      }, GOOGLE_BOOKS_API_KEY.value(), limite);

      if (!datos) return {libros: [], error: "no-disponible"};
      const libros = (datos.items || []).map(aLibro).filter(Boolean);
      return {libros};
    });

// Lote de recomendaciones aleatorias, para llenar la cola de la pantalla Home
exports.librosAleatorios = onCall(
    {secrets: [GOOGLE_BOOKS_API_KEY]},
    async (request) => {
      exigirUsuario(request);
      const cuantos = Math.min(Math.max(Number(
          (request.data && request.data.cuantos) || 10), 1), 20);
      const termino = String((request.data && request.data.termino) || "").trim();

      const semillas = termino ? [termino] : barajar(SEMILLAS.slice());

      const encontrados = [];
      const vistos = new Set();
      let consultas = 0;
      const limite = Date.now() + PRESUPUESTO_MS;

      // Varias consultas por llamada hasta reunir los libros pedidos, siempre
      // dentro del presupuesto: antes se encadenaban sin control y la invocacion
      // moria por timeout devolviendo nada.
      for (const semilla of semillas) {
        if (encontrados.length >= cuantos || consultas >= MAX_CONSULTAS) break;
        if (Date.now() >= limite) {
          logger.warn(`Sin tiempo: se devuelven ${encontrados.length} libros`);
          break;
        }
        consultas++;
        const datos = await pedirABooks({
          q: semilla,
          orderBy: "relevance",
          maxResults: "40",
          printType: "books",
        }, GOOGLE_BOOKS_API_KEY.value(), limite, INTENTOS_POR_SEMILLA);
        if (!datos) continue;
        for (const libro of (datos.items || []).map(aLibro)) {
          if (libro && !vistos.has(libro.id)) {
            vistos.add(libro.id);
            encontrados.push(libro);
          }
        }
      }

      if (!encontrados.length) return {libros: [], error: "no-disponible"};
      return {libros: barajar(encontrados).slice(0, cuantos)};
    });
