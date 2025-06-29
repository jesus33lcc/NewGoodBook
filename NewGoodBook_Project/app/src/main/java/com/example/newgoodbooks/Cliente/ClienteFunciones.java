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

    //busca libros por titulo. lista vacia si falla o no hay resultados.
    public static List<Libro> buscarTitulo(String nombre) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("query", nombre);
        return llamarYExtraer("buscarLibros", datos);
    }

    //pide un lote de recomendaciones. lista vacia si falla.
    public static List<Libro> librosAleatorios(int cuantos) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("cuantos", cuantos);
        return llamarYExtraer("librosAleatorios", datos);
    }

    //un solo libro aleatorio, o null
    public static Libro getLibroAleatorio() {
        List<Libro> libros = librosAleatorios(1);
        return libros.isEmpty() ? null : libros.get(0);
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
