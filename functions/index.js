const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions/v2/options");
const {defineSecret} = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

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

// Cache en memoria de la instancia. Ahorra cuota de la API de Books en las
// peticiones repetidas y no cuesta nada (no usa Firestore ni Storage).
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
async function pedirABooks(params, apiKey, limite, maxIntentos = MAX_INTENTOS) {
  const url = new URL(BASE_URL);
  for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
  url.searchParams.set("key", apiKey);

  const clave = url.toString().replace(apiKey, "");
  const enCache = cacheGet(clave);
  if (enCache) return enCache;

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
  // Books no responde: si tenemos una respuesta vieja, mejor eso que nada
  const viejo = cacheGetCaducado(clave);
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

function exigirUsuario(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Hay que iniciar sesion.");
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
