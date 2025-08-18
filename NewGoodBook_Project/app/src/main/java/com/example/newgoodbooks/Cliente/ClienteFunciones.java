package com.example.newgoodbooks.Cliente;

import android.util.Log;

import com.example.newgoodbooks.Modelos.Libro;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//Acceso a los libros a traves de las Cloud Functions del proyecto.
//La clave de Google Books vive en el servidor: la app nunca la ve.
//
//IMPORTANTE: estos metodos BLOQUEAN esperando la respuesta -> llamarlos siempre
//fuera del hilo principal. Nunca lanzan excepciones: si algo falla devuelven
//lista vacia o null, y quien llama decide que enseniar.
public class ClienteFunciones {
    private static final String TAG = "ClienteFunciones";
    private static final String REGION = "europe-west1";
    //la funcion reintenta por dentro contra Books, asi que le damos margen
    private static final long ESPERA_MAX_SEG = 40;

    private static FirebaseFunctions funciones;

    private static synchronized FirebaseFunctions getFunciones() {
        if (funciones == null) {
            funciones = FirebaseFunctions.getInstance(REGION);
        }
        return funciones;
    }

    //Ambitos que entiende el servidor. Cadena vacia = buscar en todo.
    public static final String AMBITO_TODO = "";
    public static final String AMBITO_TITULO = "titulo";
    public static final String AMBITO_AUTOR = "autor";

    //busca libros. lista vacia si falla o no hay resultados.
    public static List<Libro> buscar(String texto, String ambito) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("query", texto);
        if (ambito != null && !ambito.isEmpty()) {
            datos.put("ambito", ambito);
        }
        return llamarYExtraer("buscarLibros", datos);
    }

    //pide un lote de recomendaciones. lista vacia si falla.
    //si se pasa un termino, el servidor busca por ahi en vez de por una semilla al azar.
    public static List<Libro> librosAleatorios(int cuantos, String termino) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("cuantos", cuantos);
        if (termino != null && !termino.trim().isEmpty()) {
            datos.put("termino", termino);
        }
        return llamarYExtraer("librosAleatorios", datos);
    }

    public static List<Libro> librosAleatorios(int cuantos) {
        return librosAleatorios(cuantos, null);
    }

    //un solo libro aleatorio, o null
    public static Libro getLibroAleatorio() {
        List<Libro> libros = librosAleatorios(1);
        return libros.isEmpty() ? null : libros.get(0);
    }

    //Borra la cuenta y todos sus datos. true si el servidor confirma. Bloquea, como
    //el resto: llamar fuera del hilo principal.
    public static boolean borrarCuenta() {
        try {
            Task<HttpsCallableResult> tarea = getFunciones()
                    .getHttpsCallable("borrarCuenta").call(new HashMap<String, Object>());
            HttpsCallableResult resultado = Tasks.await(tarea, ESPERA_MAX_SEG, TimeUnit.SECONDS);
            Object cuerpo = resultado != null ? resultado.getData() : null;
            return (cuerpo instanceof Map)
                    && Boolean.TRUE.equals(((Map<?, ?>) cuerpo).get("borrada"));
        } catch (Exception e) {
            Log.w(TAG, "Fallo borrando la cuenta", e);
            return false;
        }
    }

    private static List<Libro> llamarYExtraer(String funcion, Map<String, Object> datos) {
        List<Libro> libros = new ArrayList<>();
        try {
            Task<HttpsCallableResult> tarea = getFunciones()
                    .getHttpsCallable(funcion)
                    .call(datos);
            HttpsCallableResult resultado = Tasks.await(tarea, ESPERA_MAX_SEG, TimeUnit.SECONDS);

            Object cuerpo = resultado != null ? resultado.getData() : null;
            if (!(cuerpo instanceof Map)) {
                return libros;
            }
            Object lista = ((Map<?, ?>) cuerpo).get("libros");
            if (!(lista instanceof List)) {
                Object error = ((Map<?, ?>) cuerpo).get("error");
                if (error != null) {
                    Log.w(TAG, funcion + " devolvio error: " + error);
                }
                return libros;
            }
            for (Object elemento : (List<?>) lista) {
                if (elemento instanceof Map) {
                    Libro libro = Libro.desdeMapa((Map<?, ?>) elemento);
                    if (libro != null) {
                        libros.add(libro);
                    }
                }
            }
        } catch (Exception e) {
            //incluye falta de sesion, timeout y cualquier fallo de red
            Log.w(TAG, "Fallo llamando a " + funcion, e);
        }
        return libros;
    }
}
