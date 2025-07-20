# NewGoodBook

Aplicación Android para descubrir libros: te propone lecturas, las buscas por título y
las organizas en listas propias que se sincronizan entre todos tus dispositivos.

Nació como proyecto de clase del ciclo de **Desarrollo de Aplicaciones Multiplataforma** y
se ha retomado después para rehacerle las tripas: backend propio, datos en la nube,
Material 3 y las claves fuera del código.

---

## Qué hace

- **Descubre libros.** La pantalla principal propone un libro cada vez, y aprende de lo
  que marcas: la mitad de las recomendaciones se piden a partir de los autores y géneros
  de tus favoritos.
- **Busca por título** en el catálogo de Google Books.
- **Listas propias.** Crea las que quieras, añade libros y bórralos deslizando.
- **Favoritos y leídos**, como dos listas fijas que no se pueden borrar.
- **Historial** de los libros por los que has ido pasando.
- **Todo sincronizado.** Marcas un libro en el móvil y aparece en la tablet, sin refrescar.
- **Funciona sin conexión** gracias a la caché local de Firestore; los cambios se suben
  solos al recuperar la red.
- **Modo claro y oscuro**, siguiendo el ajuste del sistema.

## Cómo está montado

```
   App Android  ──►  Cloud Functions  ──►  Google Books API
   (Java)            (Node.js)
        │                  │
        └──►  Firestore  ◄─┘
              (datos del usuario + caché de búsquedas)
```

La app **nunca habla directamente con Google Books**. Lo hace a través de dos Cloud
Functions que guardan la clave de la API en Secret Manager, así que la clave no viaja
dentro del APK ni se puede extraer descomprimiéndolo.

Ese intermediario además resuelve dos problemas reales:

- **Caché en dos niveles** (memoria de la instancia + Firestore). La API de Google Books
  falla con bastante frecuencia; con la caché, lo ya consultado se sirve en ~150 ms y deja
  de depender de que responda.
- **Menos peticiones.** Llenar la cola de recomendaciones costaba una petición *por libro*;
  ahora es una sola llamada que devuelve el lote entero.

### Datos

Cada usuario vive bajo `usuarios/{uid}` en Firestore, con subcolecciones para `favoritos`,
`leidos`, `historial` y `listas`. Las reglas de seguridad (`firestore.rules`) impiden que
nadie lea ni escriba fuera de su propio subárbol.

### Tecnologías

| Capa | Qué se usa |
|---|---|
| App | Java, AndroidX, Material 3, ViewModel + LiveData, RecyclerView, Picasso |
| Autenticación | Firebase Auth (email/contraseña y Google Sign-In) |
| Datos | Cloud Firestore, con persistencia offline |
| Backend | Cloud Functions (Node.js 22), Secret Manager |
| Datos de libros | Google Books API |

## Compilar

Necesitas **JDK 17** y el **Android SDK 34**.

```bash
git clone https://github.com/jesus33lcc/NewGoodBook.git
cd NewGoodBook/NewGoodBook_Project
./gradlew assembleDebug
```

El APK sale en `app/build/outputs/apk/debug/app-debug.apk`.

Para que el login y los datos funcionen hace falta un proyecto de Firebase propio con
Authentication y Firestore activados, y sustituir `app/google-services.json` por el tuyo.

### Las funciones

```bash
cd functions
npm install
# la clave de Google Books va a Secret Manager, nunca al repositorio
firebase functions:secrets:set GOOGLE_BOOKS_API_KEY
firebase deploy --only functions,firestore:rules
```

## Tests

```bash
cd NewGoodBook_Project
./gradlew testDebugUnitTest
```

Cubren la conversión de los modelos entre Firestore y la app, que es donde un fallo
silencioso dejaría datos ilegibles.

## Créditos

NewGoodBook nació como proyecto de clase del ciclo de Desarrollo de Aplicaciones
Multiplataforma, hecho en equipo:

- [@jesus33lcc](https://github.com/jesus33lcc)
- [@SilviaCC1701](https://github.com/SilviaCC1701)
- [@brisasp](https://github.com/brisasp)
- [@angelardm](https://github.com/angelardm)

La versión actual —backend propio, datos en la nube y rediseño— la mantiene
[@jesus33lcc](https://github.com/jesus33lcc).

## Licencia

Ver [LICENSE](LICENSE).
