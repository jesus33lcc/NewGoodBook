# Política de privacidad de NewGoodBook

Última actualización: 14 de agosto de 2025

NewGoodBook es una aplicación de recomendación de libros desarrollada por
**Jesús Luis Condori Chambi** (@jesus33lcc) y **Silvia** (@SilviaCC1701).

## Qué datos se recogen

Para poder tener cuenta y que tus listas estén en todos tus dispositivos, se guarda:

| Dato | Para qué | Dónde |
|---|---|---|
| Correo electrónico | Identificar tu cuenta e iniciar sesión | Firebase Authentication |
| Nombre que escribes al registrarte | Saludarte en la aplicación | Firebase Authentication |
| Libros marcados como favoritos o leídos | Tus listas y las recomendaciones | Cloud Firestore |
| Libros que has visto y los que has descartado | No repetirte recomendaciones y afinarlas | Cloud Firestore |
| Listas que creas | Enseñártelas | Cloud Firestore |
| Géneros elegidos al empezar | Primeras recomendaciones | Cloud Firestore |
| Tema e idioma elegidos | Recordar tus preferencias | Solo en tu dispositivo |

**No se recoge** ubicación, agenda de contactos, identificadores de publicidad ni datos
de uso con fines analíticos o publicitarios.

## Con quién se comparten

Con nadie con fines comerciales. **No se venden ni se ceden datos a terceros.**

La aplicación consulta la **API de Google Books** y la de **Open Library** para obtener
información de libros. Esas consultas salen desde nuestro servidor, no desde tu
dispositivo, y **no llevan ningún dato tuyo**: solo el término de búsqueda.

Los datos se alojan en **Google Cloud Firestore**, en la región `europe-west1` (Bélgica),
bajo el proyecto de Firebase de la aplicación.

## Quién puede verlos

Solo tú. Las reglas de seguridad de Firestore permiten a cada usuario leer y escribir
**únicamente su propio subárbol** de datos, comprobado contra el identificador de su
sesión. El código de esas reglas es público en `firestore.rules`.

## Cuánto tiempo se guardan

Mientras tengas la cuenta. El historial de libros vistos se limita a los 10 más
recientes.

## Cómo borrarlos

Dentro de la aplicación: **Ajustes → Eliminar cuenta**. Se borran de forma inmediata y
permanente tu cuenta, tus listas, tu historial, tus descartes y tus preferencias.
No queda copia.

También puedes escribir a **jesus33lcc@gmail.com** para pedir el borrado.

## Menores

La aplicación no está dirigida a menores de 13 años y no recoge datos a sabiendas de
personas de esa edad.

## Cambios

Si esta política cambia, se actualizará este documento y la fecha de arriba.

## Contacto

**jesus33lcc@gmail.com**
