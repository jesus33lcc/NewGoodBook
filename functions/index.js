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
// Idioma de los libros. Sin esto la API devolvia cualquier cosa: en las pruebas
// salio un libro en neerlandes en una app en espaniol.
const IDIOMA_POR_DEFECTO = "es";

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

// FILTRO DE CALIDAD. Google Books mezcla con las novelas material que no se lee por
// gusto: guias de estudio, critica academica, temarios y folletos de 16 paginas. Y
// langRestrict es solo una pista, no una garantia: pidiendo "es" siguen colandose
// libros en ingles y en catalan. Todo esto se descarta aqui, en el servidor, para que
// la cache no se llene de basura y la app no tenga que saber nada de esto.
const CATEGORIAS_VETADAS = [
  "study aids", "education", "literary criticism", "reference",
  "language arts", "bibliography", "juvenile nonfiction",
  "business & economics", "self-help", "computers", "medical", "law",
  "test preparation", "examinations", "games & activities",
];
// Lo que delata un resumen o una guia en el propio titulo
const TITULOS_VETADOS = [
  "study guide", "guia de estudio", "guía de estudio", "summary of",
  "resumen de", "analysis of", "sparknotes", "cliffsnotes", "quicklet",
  "bibliographic guide", "workbook", "teacher's guide", "lesson plan",
  "a study guide for", "test prep", "exam review",
];
// Un libro de menos de 80 paginas casi nunca es la novela, sino un extracto o
// un folleto. Y una sinopsis de dos lineas suele ser texto de relleno del editor.
const PAGINAS_MINIMAS = 80;
const PAGINAS_MAXIMAS = 5000;
const DESCRIPCION_MINIMA = 120;

function motivoRechazo(info, idioma) {
  // langRestrict no basta: hay que comparar el idioma que declara el propio volumen
  if (idioma && info.language &&
      info.language.slice(0, 2) !== idioma.slice(0, 2)) {
    return "idioma";
  }
  if (info.pageCount < PAGINAS_MINIMAS || info.pageCount > PAGINAS_MAXIMAS) {
    return "paginas";
  }
  if (info.description.length < DESCRIPCION_MINIMA) return "descripcion";

  const titulo = info.title.toLowerCase();
  if (TITULOS_VETADOS.some((t) => titulo.includes(t))) return "titulo";

  const categorias = info.categories.map((c) => String(c).toLowerCase());
  if (categorias.some((c) => CATEGORIAS_VETADAS.some((v) => c.includes(v)))) {
    return "categoria";
  }
  return null;
}

// Solo nos quedamos con los campos que la app usa: menos datos por la red
// y la clave y el resto de la respuesta no salen de aqui.
function aLibro(volume, idioma, tally) {
  const info = volume && volume.volumeInfo;
  if (!info) return null;
  const img = info.imageLinks && info.imageLinks.thumbnail;
  if (!volume.id || !info.title || !info.authors || !info.authors.length ||
      !info.pageCount || !info.publishedDate || !info.categories ||
      !info.categories.length || !info.description || !img) {
    return null;
  }
  const motivo = motivoRechazo(info, idioma);
  if (motivo) {
    if (tally) tally[motivo] = (tally[motivo] || 0) + 1;
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
    // el ISBN es la llave para cruzar con Open Library; se prefiere el de 13
    isbn: isbnDe(info),
    editorial: info.publisher || null,
  };
}

function isbnDe(info) {
  const ids = info.industryIdentifiers || [];
  const trece = ids.find((i) => i.type === "ISBN_13");
  const diez = ids.find((i) => i.type === "ISBN_10");
  return (trece && trece.identifier) || (diez && diez.identifier) || null;
}

// ---------- Open Library ----------
// Google Books no da valoraciones y sus categorias son burdas ("Fiction" a secas).
// Open Library si: nota media, numero de votos y materias mucho mas finas. Se usa
// para COMPLETAR lo de Google, no para sustituirlo: la sinopsis y la portada de
// Open Library son irregulares y casi siempre en ingles.
const OL_BUSQUEDA = "https://openlibrary.org/search.json";
// Identificarse sube su limite de 1 a 3 peticiones por segundo.
const OL_AGENTE = "NewGoodBook/1.0 (https://github.com/jesus33lcc/NewGoodBook)";
const OL_MAX_MATERIAS = 8;

async function pedirAOpenLibrary(url, limite) {
  const enMemoria = cacheGet(url);
  if (enMemoria) return enMemoria;
  const persistida = await leerDeFirestore(url);
  if (persistida && persistida.fresco) {
    cacheSet(url, persistida.datos);
    return persistida.datos;
  }
  const viejo = persistida ? persistida.datos : null;
  if (Date.now() >= limite) return viejo;
  try {
    const res = await fetch(url, {
      headers: {"User-Agent": OL_AGENTE},
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok) {
      logger.warn(`Open Library respondio ${res.status}`);
      return viejo;
    }
    const datos = await res.json();
    cacheSet(url, datos);
    guardarEnFirestore(url, datos); //sin await: no bloquea la respuesta
    return datos;
  } catch (e) {
    logger.warn("Fallo llamando a Open Library", e);
    return viejo;
  }
}

// Enriquece un lote entero con UNA sola consulta: Open Library acepta varios
// "isbn:" unidos por OR. Ir libro a libro no cabria en su limite de peticiones.
// Si algo falla se devuelven los libros tal cual: esto anade datos, nunca decide
// si un libro se ensenia o no.
async function enriquecer(libros, limite) {
  const conIsbn = libros.filter((l) => l.isbn).slice(0, 20);
  if (!conIsbn.length) {
    logger.warn(`Open Library: ninguno de los ${libros.length} libros trae ISBN`);
    return libros;
  }

  const consulta = conIsbn.map((l) => `isbn:${l.isbn}`).join(" OR ");
  const url = `${OL_BUSQUEDA}?q=${encodeURIComponent(consulta)}&limit=40` +
      "&fields=isbn,ratings_average,ratings_count,subject";
  const datos = await pedirAOpenLibrary(url, limite);
  if (!datos || !Array.isArray(datos.docs)) {
    logger.warn("Open Library: sin respuesta utilizable");
    return libros;
  }
  logger.info(`Open Library: ${datos.docs.length} obras para ${conIsbn.length} isbn`);

  // Se indexa por TODOS los isbn de cada obra: el que pedimos es el de una edicion
  // concreta y Open Library responde con la obra, que agrupa muchas.
  const porIsbn = new Map();
  for (const doc of datos.docs) {
    for (const isbn of (doc.isbn || [])) porIsbn.set(isbn, doc);
  }

  let enriquecidos = 0;
  for (const libro of libros) {
    const doc = libro.isbn ? porIsbn.get(libro.isbn) : null;
    if (!doc) continue;
    enriquecidos++;
    volcar(libro, doc);
  }

  // SEGUNDA PASADA POR TITULO. Medido el 26-07-2026: cruzando solo por ISBN casaban
  // 6 de cada 20. Google Books devuelve muchas ediciones digitales cuyo ISBN no esta
  // en Open Library, aunque la obra si lo este y con valoraciones. Se rescatan por
  // titulo, exigiendo ademas que coincida un autor para no cazar un libro distinto
  // que se llame igual.
  const sinCasar = libros.filter((l) => !l.materias);
  if (sinCasar.length) {
    enriquecidos += await rescatarPorTitulo(sinCasar, limite);
  }

  logger.info(`Open Library: ${enriquecidos}/${libros.length} enriquecidos`);
  return libros;
}

// Materias que no dicen nada del libro: son etiquetas internas del catalogo de
// Open Library. "Spanish language books" salia como si fuese un tema.
const MATERIAS_RUIDO = [
  "language books", "accessible book", "protected daisy", "in library",
  "large type books", "open library staff picks", "internet archive wishlist",
  "overdrive", "lending library", "popular print disabled books",
];

function esMateriaUtil(materia) {
  const m = String(materia).toLowerCase();
  if (m.startsWith("series:")) return false;
  return !MATERIAS_RUIDO.some((r) => m.includes(r));
}

function volcar(libro, doc) {
  if (typeof doc.ratings_average === "number") {
    libro.valoracion = Math.round(doc.ratings_average * 10) / 10;
    libro.numVotos = doc.ratings_count || 0;
  }
  libro.materias = (doc.subject || [])
      .filter(esMateriaUtil)
      .slice(0, OL_MAX_MATERIAS);
}

// Sin tildes, sin puntuacion y sin subtitulo: "El Quijote: edicion RAE" -> "el quijote"
function normalizar(texto) {
  return String(texto || "")
      .split(":")[0]
      .normalize("NFD").replace(/[̀-ͯ]/g, "")
      .toLowerCase().replace(/[^a-z0-9 ]/g, " ")
      .replace(/\s+/g, " ").trim();
}

function mismoAutor(libro, doc) {
  const suyos = (doc.author_name || []).map(normalizar);
  if (!suyos.length) return false;
  return (libro.autor || []).some((a) => {
    const mio = normalizar(a);
    return suyos.some((s) => s === mio || s.endsWith(" " + mio.split(" ").pop()));
  });
}

async function rescatarPorTitulo(libros, limite) {
  const lote = libros.slice(0, 10);
  // OJO: se pregunta con el titulo TAL CUAL, con sus tildes. Normalizarlo antes de
  // consultar hacia que Open Library no encontrase nada ("cien anos de soledad" no
  // casa con "Cien años de soledad" en su indice). normalizar() es solo para
  // comparar lo que vuelve. Se quita el subtitulo, que si estorba al buscar.
  const consulta = lote
      .map((l) => `title:"${String(l.titulo).split(":")[0].replace(/"/g, "").trim()}"`)
      .join(" OR ");
  const url = `${OL_BUSQUEDA}?q=${encodeURIComponent(consulta)}&limit=60` +
      "&fields=title,author_name,ratings_average,ratings_count,subject";
  const datos = await pedirAOpenLibrary(url, limite);
  if (!datos || !Array.isArray(datos.docs)) return 0;

  let casados = 0;
  for (const libro of lote) {
    const mio = normalizar(libro.titulo);
    const doc = datos.docs.find((d) =>
      normalizar(d.title) === mio && mismoAutor(libro, d));
    if (!doc) continue;
    casados++;
    volcar(libro, doc);
  }
  return casados;
}

// Semillas de recomendacion. Deliberadamente son temas y autores reales:
// con palabras vacias tipo "El" o "De" casi ningun volumen trae descripcion,
// categorias ni portada, y el filtro los descartaba todos.
// SEMILLAS POR IDIOMA. Medido el 26-07-2026: pidiendo langRestrict=es con semillas
// en ingles, Books devuelve sobre todo ediciones inglesas y el filtro de idioma
// tumbaba hasta 19 de cada 20 (inauthor:Terry+Pratchett). Con semillas en el idioma
// que se pide, "cuentos" no perdio ninguno. Es mas barato preguntar bien que filtrar
// despues: cada consulta descartada entera es una llamada tirada.
const SEMILLAS_POR_IDIOMA = {
  es: [
    "novela", "cuentos", "narrativa", "relatos", "aventuras", "misterio",
    "literatura espanola", "literatura hispanoamericana", "novela historica",
    "novela negra", "ciencia ficcion", "poesia", "clasicos",
    "inauthor:Gabriel+Garcia+Marquez", "inauthor:Isabel+Allende",
    "inauthor:Carlos+Ruiz+Zafon", "inauthor:Mario+Vargas+Llosa",
    "inauthor:Arturo+Perez-Reverte", "inauthor:Almudena+Grandes",
    "inauthor:J.K.+Rowling", "inauthor:Agatha+Christie",
  ],
  en: [
    "subject:fiction", "subject:fantasy", "subject:science+fiction",
    "subject:mystery", "subject:thriller", "subject:romance",
    "subject:history", "subject:biography", "subject:poetry",
    "inauthor:Stephen+King", "inauthor:Agatha+Christie",
    "inauthor:Isaac+Asimov", "inauthor:Terry+Pratchett",
    "inauthor:J.K.+Rowling", "inauthor:Neil+Gaiman",
  ],
};

function semillasDe(idioma) {
  return SEMILLAS_POR_IDIOMA[String(idioma).slice(0, 2)] ||
      SEMILLAS_POR_IDIOMA[IDIOMA_POR_DEFECTO];
}

// ================= PERFIL DE GUSTOS Y PUNTUACION =================
// Antes la recomendacion era una semilla al azar de una lista fija: la app no
// aprendia nada de lo que marcabas. Ahora se lee lo que el usuario ya ha hecho y
// se usa para DOS cosas distintas:
//   · elegir a QUIEN preguntar  -> autores y generos de Google, que es su vocabulario
//   · ordenar lo que llega      -> materias finas de Open Library, mucho mas precisas
// El perfil se calcula aqui y no en el movil: la funcion ya entra a Firestore con el
// SDK admin, asi no viaja nada por la red y el movil no necesita saber de esto.
const COL_USUARIOS = "usuarios";
const PESO_FAVORITO = 3;
const PESO_LEIDO = 2;
// Los descartes RESTAN. Sin senial negativa un recomendador no aprende: solo repite
// lo que ya acerto y no tiene forma de saber que algo no gusta.
const PESO_DESCARTADO = -3;
const MAX_PERFIL = 200;
// De cada 3 recomendaciones, 1 viene de fuera del perfil. Sin esta cuota el sistema
// se cierra sobre lo mismo y nunca descubre nada nuevo.
const UNA_DE_CADA = 3;

async function leerColeccion(uid, coleccion) {
  try {
    const snap = await db.collection(COL_USUARIOS).doc(uid)
        .collection(coleccion).limit(MAX_PERFIL).get();
    return snap.docs.map((d) => d.data());
  } catch (e) {
    logger.warn(`No se pudo leer ${coleccion} de ${uid}`, e);
    return [];
  }
}

async function perfilDeGustos(uid) {
  const [favoritos, leidos, descartados, historial] = await Promise.all([
    leerColeccion(uid, "favoritos"),
    leerColeccion(uid, "leidos"),
    leerColeccion(uid, "descartados"),
    leerColeccion(uid, "historial"),
  ]);

  const materias = new Map();
  const generos = new Map();
  const autores = new Map();
  const vistos = new Set();
  const suma = (mapa, clave, peso) => {
    if (!clave) return;
    const k = String(clave).trim();
    if (k) mapa.set(k, (mapa.get(k) || 0) + peso);
  };

  const digerir = (libros, peso) => {
    for (const l of libros) {
      if (l.id) vistos.add(l.id);
      for (const m of (l.materias || [])) suma(materias, String(m).toLowerCase(), peso);
      for (const g of (l.generos || [])) suma(generos, g, peso);
      for (const a of (l.autor || [])) suma(autores, a, peso);
    }
  };
  digerir(favoritos, PESO_FAVORITO);
  digerir(leidos, PESO_LEIDO);
  digerir(descartados, PESO_DESCARTADO);
  // el historial solo sirve para no repetir: haber visto algo no dice que guste
  for (const l of historial) if (l.id) vistos.add(l.id);

  return {materias, generos, autores, vistos,
    tamanio: favoritos.length + leidos.length};
}

const mejores = (mapa, cuantos) => [...mapa.entries()]
    .filter(([, peso]) => peso > 0)
    .sort((a, b) => b[1] - a[1])
    .slice(0, cuantos)
    .map(([clave]) => clave);

// Se pregunta a Google con SU vocabulario: nombres de autor y sus propias categorias.
// Las materias de Open Library ("magic realism") no son terminos de Google y como
// semilla rinden mal; ahi solo se usan para puntuar.
function semillasDePerfil(perfil) {
  const autores = mejores(perfil.autores, 3).map((a) => `inauthor:"${a}"`);
  const generos = mejores(perfil.generos, 3).map((g) => `subject:"${g}"`);
  return [...autores, ...generos];
}

function puntuar(libro, perfil) {
  let afinidad = 0;
  for (const m of (libro.materias || [])) {
    afinidad += perfil.materias.get(String(m).toLowerCase()) || 0;
  }
  for (const g of (libro.generos || [])) afinidad += perfil.generos.get(g) || 0;
  // el autor pesa doble: acertar con el autor acierta mas que acertar con el tema
  for (const a of (libro.autor || [])) afinidad += (perfil.autores.get(a) || 0) * 2;

  // La nota solo cuenta si la respaldan votos suficientes; centrada en 3 para que
  // un libro mediocre reste y uno bueno sume, en vez de premiar por existir.
  const calidad = (libro.numVotos >= 5) ? (libro.valoracion - 3) * 2 : 0;
  return afinidad + calidad;
}

// Mezcla lo afin con lo nuevo, sin que lo segundo quede al final de la cola donde
// nunca se llega: se intercala una exploracion de cada UNA_DE_CADA.
function intercalar(afines, exploracion, cuantos) {
  const salida = [];
  let i = 0;
  let j = 0;
  while (salida.length < cuantos && (i < afines.length || j < exploracion.length)) {
    const tocaExplorar = (salida.length + 1) % UNA_DE_CADA === 0;
    if (tocaExplorar && j < exploracion.length) {
      salida.push(exploracion[j++]);
    } else if (i < afines.length) {
      salida.push(afines[i++]);
    } else if (j < exploracion.length) {
      salida.push(exploracion[j++]);
    }
  }
  return salida;
}

const MAX_CONSULTAS = 10;
// TOPE POR SEMILLA. Sin esto una sola consulta productiva llenaba la cola entera:
// en la prueba del 26-07-2026 salieron 6 Harry Potter seguidos de 11 libros. Cortando
// por semilla se obliga a repartir entre varias y la recomendacion deja de repetirse.
const MAX_POR_SEMILLA = 4;
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
    return uid;
  }
  previo.veces++;
  if (previo.veces > MAX_LLAMADAS) {
    logger.warn(`Usuario ${uid} supera ${MAX_LLAMADAS} llamadas/min`);
    throw new HttpsError("resource-exhausted",
        "Demasiadas peticiones seguidas. Espera un momento.");
  }
  return uid;
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
      const idioma = String((request.data && request.data.idioma) || IDIOMA_POR_DEFECTO)
          .slice(0, 5);
      const datos = await pedirABooks({
        q: consulta.slice(0, 200),
        orderBy: "relevance",
        maxResults: "40",
        printType: "books",
        langRestrict: idioma,
      }, GOOGLE_BOOKS_API_KEY.value(), limite);

      if (!datos) return {libros: [], error: "no-disponible"};
      const libros = (datos.items || [])
          .map((v) => aLibro(v, idioma)).filter(Boolean);
      return {libros: await enriquecer(libros, limite)};
    });

// Lote de recomendaciones aleatorias, para llenar la cola de la pantalla Home
exports.librosAleatorios = onCall(
    {secrets: [GOOGLE_BOOKS_API_KEY]},
    async (request) => {
      const uid = exigirUsuario(request);
      const cuantos = Math.min(Math.max(Number(
          (request.data && request.data.cuantos) || 10), 1), 20);
      const termino = String((request.data && request.data.termino) || "").trim();
      //el idioma lo manda la app segun el del dispositivo; si no, espaniol
      const idioma = String((request.data && request.data.idioma) || IDIOMA_POR_DEFECTO)
          .slice(0, 5);

      const perfil = await perfilDeGustos(uid);
      const deExploracion = new Set(barajar(semillasDe(idioma).slice()));
      // Las del perfil van primero, pero SIEMPRE acompaniadas de exploracion: quien
      // no ha marcado nada todavia no tiene perfil, y quien lo tiene no debe quedarse
      // encerrado en el. Que semilla produjo cada libro se recuerda para poder
      // intercalar despues lo afin con lo nuevo.
      const delPerfil = semillasDePerfil(perfil);
      const semillas = termino ? [termino] :
        [...delPerfil, ...deExploracion];
      logger.info(`Perfil de ${uid}: ${perfil.tamanio} libros marcados, ` +
          `${delPerfil.length} semillas propias, ${perfil.vistos.size} ya vistos`);

      const encontrados = [];
      const vistos = new Set(perfil.vistos);
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
        const esDelPerfil = delPerfil.includes(semilla);
        const datos = await pedirABooks({
          q: semilla,
          orderBy: "relevance",
          maxResults: "40",
          printType: "books",
          langRestrict: idioma,
        }, GOOGLE_BOOKS_API_KEY.value(), limite, INTENTOS_POR_SEMILLA);
        if (!datos) continue;
        const crudos = (datos.items || []).length;
        const tally = {};
        let aceptados = 0;
        let deEstaSemilla = 0;
        for (const libro of (datos.items || []).map((v) => aLibro(v, idioma, tally))) {
          if (!libro) continue;
          aceptados++;
          if (deEstaSemilla >= MAX_POR_SEMILLA) continue;
          //vistos ya trae de serie lo marcado y descartado: no se vuelve a ofrecer
          if (!vistos.has(libro.id)) {
            vistos.add(libro.id);
            libro.delPerfil = esDelPerfil; //marca de trabajo, se quita al final
            encontrados.push(libro);
            deEstaSemilla++;
          }
        }
        // Queda registrado a proposito: Google Books cambia con el tiempo y si el
        // filtro empieza a dejar pasar muy poco, la cola se vacia y el boton
        // "Siguiente" se pone a tardar. Aqui se ve venir.
        logger.info(`${semilla}: ${aceptados}/${crudos} pasan · ` +
            JSON.stringify(tally));
      }

      if (!encontrados.length) return {libros: [], error: "no-disponible"};

      // Se enriquece ANTES de puntuar: la nota y las materias finas con las que se
      // decide el orden las pone Open Library, no Google.
      await enriquecer(encontrados, limite);

      const afines = encontrados.filter((l) => l.delPerfil)
          .sort((a, b) => puntuar(b, perfil) - puntuar(a, perfil));
      const nuevos = barajar(encontrados.filter((l) => !l.delPerfil));
      const salida = intercalar(afines, nuevos, cuantos);
      for (const libro of salida) delete libro.delPerfil;
      logger.info(`Devueltos ${salida.length}: ${afines.length} afines, ` +
          `${nuevos.length} de exploracion`);
      return {libros: salida};
    });
