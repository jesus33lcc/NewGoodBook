package com.example.newgoodbooks.Datos;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newgoodbooks.Modelos.Lectura;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Unico punto de acceso a los datos del usuario, guardados en Firestore bajo
//usuarios/{uid}. Sustituye a los estaticos de Datos y a los ficheros de AccesoFicheros.
//
//Ventajas frente a lo anterior: se sincroniza solo entre movil y tablet, sobrevive a
//desinstalar la app, no se corrompe al cambiar los modelos (no hay serializacion Java)
//y Firestore trae cache offline de serie, asi que sigue funcionando sin conexion.
public class RepositorioUsuario {
    private static final String TAG = "RepositorioUsuario";
    private static final int MAX_HISTORIAL = 10;

    private static final String COL_USUARIOS = "usuarios";
    private static final String COL_FAVORITOS = "favoritos";
    private static final String COL_LEIDOS = "leidos";
    private static final String COL_HISTORIAL = "historial";
    private static final String COL_LISTAS = "listas";
    private static final String COL_DESCARTADOS = "descartados";
    private static final String COL_LECTURAS = "lecturas";
    private static final String COL_NOTAS = "notas";
    //campo del documento raiz: el libro que se esta enseniando ahora en Principal
    private static final String CAMPO_LIBRO_ACTUAL = "libroActual";
    //preferencias elegidas al empezar; las lee tambien la Cloud Function
    private static final String CAMPO_GENEROS = "generosPreferidos";
    private static final String CAMPO_AUTORES = "autoresPreferidos";
    private static final String CAMPO_ONBOARDING = "onboardingHecho";

    private static RepositorioUsuario instancia;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<ListenerRegistration> escuchas = new ArrayList<>();

    private final MutableLiveData<List<Libro>> favoritos = new MutableLiveData<>(new ArrayList<Libro>());
    private final MutableLiveData<List<Libro>> leidos = new MutableLiveData<>(new ArrayList<Libro>());
    private final MutableLiveData<List<Libro>> historial = new MutableLiveData<>(new ArrayList<Libro>());
    private final MutableLiveData<List<Lista>> listas = new MutableLiveData<>(new ArrayList<Lista>());
    //por donde va el usuario en cada libro, indexado por id de libro
    private final MutableLiveData<Map<String, Lectura>> lecturas =
            new MutableLiveData<>(new HashMap<String, Lectura>());
    //el libro de cada lectura en curso, para poder ensenarlo en Listas
    private final Map<String, Libro> librosEnCurso = new HashMap<>();

    private String uidEscuchado;

    private RepositorioUsuario() {
    }

    public static synchronized RepositorioUsuario get() {
        if (instancia == null) {
            instancia = new RepositorioUsuario();
        }
        return instancia;
    }

    // ---------- ciclo de vida ----------

    //Conecta las escuchas al usuario que haya iniciado sesion. Es idempotente:
    //llamarlo varias veces con el mismo usuario no duplica listeners.
    public void conectar() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            desconectar();
            return;
        }
        if (usuario.getUid().equals(uidEscuchado)) {
            return;
        }
        desconectar();
        uidEscuchado = usuario.getUid();

        DocumentReference raiz = db.collection(COL_USUARIOS).document(uidEscuchado);
        escucharLibros(raiz.collection(COL_FAVORITOS), favoritos, null);
        escucharLibros(raiz.collection(COL_LEIDOS), leidos, null);
        escucharLibros(raiz.collection(COL_HISTORIAL).orderBy("visto", Query.Direction.DESCENDING)
                .limit(MAX_HISTORIAL), historial, null);
        escucharListas(raiz.collection(COL_LISTAS));
        escucharLecturas(raiz.collection(COL_LECTURAS));
    }

    //Corta las escuchas y vacia el estado en memoria (al cerrar sesion)
    public void desconectar() {
        for (ListenerRegistration escucha : escuchas) {
            escucha.remove();
        }
        escuchas.clear();
        uidEscuchado = null;
        favoritos.postValue(new ArrayList<Libro>());
        leidos.postValue(new ArrayList<Libro>());
        historial.postValue(new ArrayList<Libro>());
        listas.postValue(new ArrayList<Lista>());
        lecturas.postValue(new HashMap<String, Lectura>());
        librosEnCurso.clear();
    }

    private void escucharLecturas(CollectionReference coleccion) {
        escuchas.add(coleccion.addSnapshotListener((instantanea, error) -> {
            if (error != null) {
                Log.w(TAG, "Fallo escuchando lecturas", error);
                return;
            }
            Map<String, Lectura> salida = new HashMap<>();
            librosEnCurso.clear();
            if (instantanea != null) {
                for (QueryDocumentSnapshot doc : instantanea) {
                    Lectura lectura = Lectura.desdeMapa(doc.getData());
                    if (lectura != null) {
                        salida.put(doc.getId(), lectura);
                        Libro libro = Libro.desdeMapa(doc.getData());
                        if (libro != null) {
                            librosEnCurso.put(doc.getId(), libro);
                        }
                    }
                }
            }
            lecturas.postValue(salida);
        }));
    }

    private void escucharLibros(Query consulta, MutableLiveData<List<Libro>> destino,
                                @Nullable String etiqueta) {
        escuchas.add(consulta.addSnapshotListener((instantanea, error) -> {
            if (error != null) {
                Log.w(TAG, "Fallo escuchando " + (etiqueta != null ? etiqueta : consulta), error);
                return;
            }
            List<Libro> salida = new ArrayList<>();
            if (instantanea != null) {
                for (QueryDocumentSnapshot doc : instantanea) {
                    Libro libro = Libro.desdeMapa(doc.getData());
                    if (libro != null) {
                        salida.add(libro);
                    }
                }
            }
            destino.postValue(salida);
        }));
    }

    private void escucharListas(CollectionReference coleccion) {
        escuchas.add(coleccion.addSnapshotListener((instantanea, error) -> {
            if (error != null) {
                Log.w(TAG, "Fallo escuchando listas", error);
                return;
            }
            List<Lista> salida = new ArrayList<>();
            if (instantanea != null) {
                for (QueryDocumentSnapshot doc : instantanea) {
                    Lista lista = Lista.desdeMapa(doc.getId(), doc.getData());
                    if (lista != null) {
                        salida.add(lista);
                    }
                }
            }
            listas.postValue(salida);
        }));
    }

    // ---------- lectura ----------

    public LiveData<List<Libro>> getFavoritos() {
        return favoritos;
    }

    public LiveData<List<Libro>> getLeidos() {
        return leidos;
    }

    public LiveData<List<Libro>> getHistorial() {
        return historial;
    }

    public LiveData<List<Lista>> getListas() {
        return listas;
    }

    public LiveData<Map<String, Lectura>> getLecturas() {
        return lecturas;
    }

    public Lectura lecturaDe(Libro libro) {
        Map<String, Lectura> actuales = lecturas.getValue();
        if (libro == null || libro.getId() == null || actuales == null) {
            return null;
        }
        return actuales.get(libro.getId());
    }

    //Los libros que el usuario tiene a medias, para ensenarlos los primeros en Listas.
    public List<Libro> getLibrosLeyendo() {
        List<Libro> salida = new ArrayList<>();
        Map<String, Lectura> actuales = lecturas.getValue();
        if (actuales == null) {
            return salida;
        }
        for (Map.Entry<String, Lectura> e : actuales.entrySet()) {
            Libro libro = librosEnCurso.get(e.getKey());
            if (e.getValue().estaLeyendo() && libro != null) {
                salida.add(libro);
            }
        }
        return salida;
    }

    //Guarda por donde va. Marcar "leido" NO pasa por aqui: eso sigue siendo la
    //coleccion de leidos, para no tener dos verdades sobre el mismo hecho.
    public void guardarLectura(Libro libro, String estado, int pagina) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>(libro.aMapa());
        datos.putAll(new Lectura(estado, pagina).aMapa());
        raiz.collection(COL_LECTURAS).document(libro.getId()).set(datos)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo guardar la lectura", e));
    }

    public void olvidarLectura(Libro libro) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        raiz.collection(COL_LECTURAS).document(libro.getId()).delete()
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo borrar la lectura", e));
    }

    // ---------- notas personales ----------

    //Lectura suelta: la nota solo hace falta al abrir la ficha y no interesa que cambie
    //bajo los dedos mientras se escribe.
    public void leerNota(Libro libro, AlLeerTexto respuesta) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            respuesta.enTexto(null);
            return;
        }
        raiz.collection(COL_NOTAS).document(libro.getId()).get()
                .addOnSuccessListener(doc -> {
                    Object texto = doc.get("texto");
                    respuesta.enTexto(texto == null ? null : String.valueOf(texto));
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "No se pudo leer la nota", e);
                    respuesta.enTexto(null);
                });
    }

    public interface AlLeerTexto {
        void enTexto(@Nullable String texto);
    }

    //Guardar una nota vacia seria dejar basura: se borra el documento.
    public void guardarNota(Libro libro, String texto) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        DocumentReference doc = raiz.collection(COL_NOTAS).document(libro.getId());
        if (texto == null || texto.trim().isEmpty()) {
            doc.delete().addOnFailureListener(e -> Log.w(TAG, "No se pudo borrar la nota", e));
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("texto", texto.trim());
        datos.put("actualizado", System.currentTimeMillis());
        doc.set(datos).addOnFailureListener(e -> Log.w(TAG, "No se pudo guardar la nota", e));
    }

    public boolean esFavorito(Libro libro) {
        return contiene(favoritos.getValue(), libro);
    }

    public boolean esLeido(Libro libro) {
        return contiene(leidos.getValue(), libro);
    }

    private static boolean contiene(List<Libro> lista, Libro libro) {
        return libro != null && lista != null && lista.contains(libro);
    }

    //Las dos listas fijas, construidas al vuelo a partir de favoritos y leidos
    public List<Lista> getListasImborrables() {
        List<Lista> fijas = new ArrayList<>();
        fijas.add(new Lista(Lista.ID_FAVORITOS, Lista.NOMBRE_FAVORITOS, valorOVacio(favoritos)));
        fijas.add(new Lista(Lista.ID_LEIDOS, Lista.NOMBRE_LEIDOS, valorOVacio(leidos)));
        return fijas;
    }

    public Lista getListaPorId(String id) {
        if (Lista.ID_FAVORITOS.equals(id)) {
            return new Lista(Lista.ID_FAVORITOS, Lista.NOMBRE_FAVORITOS, valorOVacio(favoritos));
        }
        if (Lista.ID_LEIDOS.equals(id)) {
            return new Lista(Lista.ID_LEIDOS, Lista.NOMBRE_LEIDOS, valorOVacio(leidos));
        }
        for (Lista lista : valorListasOVacio()) {
            if (lista.getId().equals(id)) {
                return lista;
            }
        }
        return null;
    }

    public String[] getNombresListasPersonales() {
        List<Lista> actuales = valorListasOVacio();
        String[] nombres = new String[actuales.size()];
        for (int i = 0; i < actuales.size(); i++) {
            nombres[i] = actuales.get(i).getNombre();
        }
        return nombres;
    }

    public Lista getListaPersonalPorIndice(int indice) {
        List<Lista> actuales = valorListasOVacio();
        if (indice < 0 || indice >= actuales.size()) {
            return null;
        }
        return actuales.get(indice);
    }

    private static List<Libro> valorOVacio(MutableLiveData<List<Libro>> fuente) {
        List<Libro> valor = fuente.getValue();
        return valor != null ? new ArrayList<>(valor) : new ArrayList<Libro>();
    }

    private List<Lista> valorListasOVacio() {
        List<Lista> valor = listas.getValue();
        return valor != null ? valor : new ArrayList<Lista>();
    }

    // ---------- escritura ----------
    // Firestore encola las escrituras cuando no hay red y las aplica al volver,
    // asi que no hay que hacer nada especial para el modo offline.

    public void alternarFavorito(Libro libro) {
        alternarEnColeccion(COL_FAVORITOS, libro, esFavorito(libro));
    }

    public void alternarLeido(Libro libro) {
        alternarEnColeccion(COL_LEIDOS, libro, esLeido(libro));
    }

    private void alternarEnColeccion(String coleccion, Libro libro, boolean estaba) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        DocumentReference doc = raiz.collection(coleccion).document(libro.getId());
        if (estaba) {
            doc.delete().addOnFailureListener(e -> Log.w(TAG, "No se pudo quitar de " + coleccion, e));
        } else {
            doc.set(libro.aMapa()).addOnFailureListener(e -> Log.w(TAG, "No se pudo anadir a " + coleccion, e));
        }
    }

    // ---------- libro recomendado en curso ----------

    //Se avisa con el libro guardado, o con null si el usuario aun no tiene ninguno.
    public interface AlLeerLibro {
        void enLibro(@Nullable Libro libro);
    }

    //Lectura suelta, no una escucha: el libro en curso solo hace falta al arrancar y
    //no interesa que cambie solo mientras se esta mirando. Firestore responde de su
    //cache si no hay red, asi que esto tambien funciona sin conexion.
    public void leerLibroActual(AlLeerLibro respuesta) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null) {
            respuesta.enLibro(null);
            return;
        }
        raiz.get()
                .addOnSuccessListener(doc -> {
                    Object guardado = doc.get(CAMPO_LIBRO_ACTUAL);
                    respuesta.enLibro(guardado instanceof Map
                            ? Libro.desdeMapa((Map<?, ?>) guardado) : null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "No se pudo leer el libro en curso", e);
                    respuesta.enLibro(null);
                });
    }

    //Antes el libro recomendado solo vivia en el ViewModel, asi que al cerrar la app
    //se perdia y al volver a entrar salia otro distinto. Guardarlo aqui lo mantiene
    //entre arranques, y ademas igual en el movil y en la tablet.
    public void guardarLibroActual(Libro libro) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put(CAMPO_LIBRO_ACTUAL, libro.aMapa());
        //merge: el documento raiz puede no existir todavia y no hay que pisar lo demas
        raiz.set(datos, SetOptions.merge())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo guardar el libro en curso", e));
    }

    //Descartar un libro. No es solo "no me lo enseñes mas": es la unica senial
    //NEGATIVA que tiene el recomendador. Sin ella solo sabe que has marcado, nunca
    //que algo no te interesa, y acaba repitiendose. La funcion lee esta coleccion
    //para restar peso a sus materias y autores.
    public void descartar(Libro libro) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>(libro.aMapa());
        datos.put("descartado", System.currentTimeMillis());
        raiz.collection(COL_DESCARTADOS).document(libro.getId()).set(datos)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo descartar el libro", e));
    }

    // ---------- gustos elegidos al empezar ----------

    //Se guardan en el documento raiz porque los lee la Cloud Function para armar el
    //perfil: asi quien no ha marcado nada todavia ya tiene por donde empezar.
    public void guardarGenerosPreferidos(List<String> generos) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || generos == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put(CAMPO_GENEROS, generos);
        datos.put(CAMPO_ONBOARDING, true);
        raiz.set(datos, SetOptions.merge())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudieron guardar los generos", e));
    }

    //Los autores elegidos son la senial mas fuerte que tiene el recomendador: puntua
    //el autor el doble que el tema. Por eso se preguntan aparte de los generos.
    public void guardarAutoresPreferidos(List<String> autores) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || autores == null || autores.isEmpty()) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put(CAMPO_AUTORES, autores);
        raiz.set(datos, SetOptions.merge())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudieron guardar los autores", e));
    }

    //Marca el proceso como hecho aunque se haya omitido: si no, volveria a salir
    //en cada arranque y seria insoportable.
    public void marcarOnboardingHecho() {
        DocumentReference raiz = raizUsuario();
        if (raiz == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put(CAMPO_ONBOARDING, true);
        raiz.set(datos, SetOptions.merge())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo marcar el onboarding", e));
    }

    //Anade sin alternar: en el onboarding los libros vienen recien traidos y
    //alternar podria quitarlos si el listener aun no habia llegado.
    public void anadirFavorito(Libro libro) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        raiz.collection(COL_FAVORITOS).document(libro.getId()).set(libro.aMapa())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo anadir a favoritos", e));
    }

    //Deja constancia de que el usuario ha visto este libro (pestania Explorar)
    public void registrarVisita(Libro libro) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || libro == null || libro.getId() == null) {
            return;
        }
        Map<String, Object> datos = new HashMap<>(libro.aMapa());
        datos.put("visto", System.currentTimeMillis());
        raiz.collection(COL_HISTORIAL).document(libro.getId()).set(datos)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo registrar en el historial", e));
        podarHistorial();
    }

    //el historial se queda en los MAX_HISTORIAL mas recientes
    private void podarHistorial() {
        DocumentReference raiz = raizUsuario();
        if (raiz == null) {
            return;
        }
        raiz.collection(COL_HISTORIAL)
                .orderBy("visto", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(instantanea -> {
                    int posicion = 0;
                    for (QueryDocumentSnapshot doc : instantanea) {
                        if (posicion++ >= MAX_HISTORIAL) {
                            doc.getReference().delete();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo podar el historial", e));
    }

    public void crearLista(String nombre) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre.trim());
        datos.put("libros", new ArrayList<Map<String, Object>>());
        datos.put("creada", System.currentTimeMillis());
        raiz.collection(COL_LISTAS).add(datos)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo crear la lista", e));
    }

    //Vuelve a crear una lista borrada con su MISMO id y sus libros, para poder
    //deshacer. Con crearLista() se generaria un id nuevo y el deshacer dejaria una
    //lista distinta con el mismo nombre.
    public void restaurarLista(Lista lista) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || lista == null || lista.getId() == null) {
            return;
        }
        List<Map<String, Object>> comoMapas = new ArrayList<>();
        for (Libro libro : lista.getLibros()) {
            comoMapas.add(libro.aMapa());
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", lista.getNombre());
        datos.put("libros", comoMapas);
        datos.put("creada", System.currentTimeMillis());
        raiz.collection(COL_LISTAS).document(lista.getId()).set(datos)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo restaurar la lista", e));
    }

    public void borrarLista(Lista lista) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null || lista == null || lista.getId() == null || lista.esImborrable()) {
            return;
        }
        raiz.collection(COL_LISTAS).document(lista.getId()).delete()
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo borrar la lista", e));
    }

    public void anadirLibroALista(String listaId, Libro libro) {
        Lista lista = getListaPorId(listaId);
        if (lista == null || libro == null || lista.esImborrable()) {
            return;
        }
        if (lista.getLibros().contains(libro)) {
            return;
        }
        List<Libro> nuevos = new ArrayList<>(lista.getLibros());
        nuevos.add(libro);
        guardarLibrosDeLista(listaId, nuevos);
    }

    public void quitarLibroDeLista(String listaId, Libro libro) {
        //en las listas fijas, quitar un libro es dejar de marcarlo
        if (Lista.ID_FAVORITOS.equals(listaId)) {
            alternarEnColeccion(COL_FAVORITOS, libro, true);
            return;
        }
        if (Lista.ID_LEIDOS.equals(listaId)) {
            alternarEnColeccion(COL_LEIDOS, libro, true);
            return;
        }
        Lista lista = getListaPorId(listaId);
        if (lista == null || libro == null) {
            return;
        }
        List<Libro> nuevos = new ArrayList<>(lista.getLibros());
        nuevos.remove(libro);
        guardarLibrosDeLista(listaId, nuevos);
    }

    private void guardarLibrosDeLista(String listaId, List<Libro> libros) {
        DocumentReference raiz = raizUsuario();
        if (raiz == null) {
            return;
        }
        List<Map<String, Object>> comoMapas = new ArrayList<>();
        for (Libro libro : libros) {
            comoMapas.add(libro.aMapa());
        }
        raiz.collection(COL_LISTAS).document(listaId).update("libros", comoMapas)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo actualizar la lista", e));
    }

    @Nullable
    private DocumentReference raizUsuario() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            Log.w(TAG, "Sin sesion: no se escribe nada");
            return null;
        }
        return db.collection(COL_USUARIOS).document(usuario.getUid());
    }
}
