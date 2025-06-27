package com.example.newgoodbooks.Cliente;

import android.util.Log;

import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.model.Volume;
import com.google.api.services.books.v1.model.Volume.VolumeInfo;
import com.google.api.services.books.v1.model.Volumes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//IMPORTANTE: todos estos metodos son bloqueantes -> llamarlos SIEMPRE fuera del hilo principal.
//Ninguno lanza excepciones: ante un fallo de red devuelven null o lista vacia.
public class ClienteBooks {
    private static final String TAG = "ClienteBooks";
    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_BASE_MS = 500;

    //la api de Google Books devuelve 503 con bastante frecuencia, asi que cada peticion
    //se reintenta con espera creciente antes de darla por perdida
    private interface Peticion<T> {
        T ejecutar() throws IOException;
    }

    private static <T> T conReintentos(String queHace, Peticion<T> peticion) {
        long espera = ESPERA_BASE_MS;
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                return peticion.ejecutar();
            } catch (IOException e) {
                Log.w(TAG, queHace + ": intento " + intento + "/" + MAX_INTENTOS + " fallido", e);
                if (intento == MAX_INTENTOS) {
                    return null;
                }
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                espera *= 2;
            }
        }
        return null;
    }

    //metodo que devuelve una lista de libros por un nombre. lista vacia si falla.
    public static List<Libro> buscarTitulo(String nombre) {
        ArrayList<Libro> listaLibros = new ArrayList<>();
        Books books = ClienteApi.getClient();
        if (books == null) {
            return listaLibros;
        }
        final long max = 40;
        final int MAX_PAGINAS = 10;
        int num = 0;
        try {
            final Books.Volumes.List volumeList = books.volumes().list(nombre);
            volumeList.setOrderBy("relevance");
            volumeList.setMaxResults(max);
            //tope de paginas: sin el, una respuesta rara de la api dejaria el bucle girando
            while (num < MAX_PAGINAS) {
                volumeList.setStartIndex(max * num++);
                Volumes volumes = conReintentos("buscarTitulo(" + nombre + ")", volumeList::execute);
                if (volumes == null || volumes.getItems() == null) {
                    return listaLibros;
                }
                for (Volume v : volumes.getItems()) {
                    if (esValido(v)) {
                        listaLibros.add(new Libro(v));
                    }
                }
                if (listaLibros.size() > 20) {
                    return listaLibros;
                }
            }
            return listaLibros;
        } catch (IOException e) {
            Log.w(TAG, "No se pudo preparar la busqueda de '" + nombre + "'", e);
            return listaLibros;
        }
    }

    //mediante un id devuelve el libro, o null si falla
    public static Libro getLibro(String id) {
        Books books = ClienteApi.getClient();
        if (books == null) {
            return null;
        }
        try {
            Volume volume = conReintentos("getLibro(" + id + ")", books.volumes().get(id)::execute);
            return volume == null ? null : new Libro(volume);
        } catch (IOException e) {
            Log.w(TAG, "No se pudo preparar la peticion del libro " + id, e);
            return null;
        }
    }

    //mediante una lista de ids devuelve los libros que se hayan podido recuperar
    public static List<Libro> getLista(List<String> listaId) {
        List<Libro> listaLibros = new ArrayList<>();
        for (String id : listaId) {
            Libro libro = getLibro(id);
            if (libro != null) {
                listaLibros.add(libro);
            }
        }
        return listaLibros;
    }

    //devuelve un Libro aleatorio, sesgado hacia los autores/generos favoritos del usuario.
    //devuelve null si la api no responde o no hay ningun volumen valido.
    public static Libro getLibroAleatorio() {
        Books books = ClienteApi.getClient();
        if (books == null) {
            return null;
        }
        String query = Datos.DatosComunes.getPalabraRandom();
        Random random = new Random();
        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                query += "+inauthor:" + Datos.DatosComunes.getAutorRandom();
            } else {
                query += "+subject:" + Datos.DatosComunes.getGeneroRandom();
            }
        }
        try {
            final Books.Volumes.List volumeList = books.volumes().list(query);
            volumeList.setPrintType("books");
            volumeList.setOrderBy("relevance");
            volumeList.setMaxResults(40L);

            Volumes volumes = conReintentos("getLibroAleatorio(" + query + ")", volumeList::execute);
            if (volumes == null || volumes.getItems() == null) {
                return null;
            }
            ArrayList<Libro> listaLibros = new ArrayList<>();
            for (Volume v : volumes.getItems()) {
                if (esValido(v)) {
                    listaLibros.add(new Libro(v));
                }
            }
            if (listaLibros.isEmpty()) {
                return null;
            }
            return listaLibros.get(random.nextInt(listaLibros.size()));
        } catch (IOException e) {
            Log.w(TAG, "No se pudo preparar la peticion de libro aleatorio", e);
            return null;
        }
    }

    //comprueba de que un Volume cumple con los campos requeridos
    public static boolean esValido(Volume volume) {
        if (volume == null || volume.getVolumeInfo() == null) {
            return false;
        }
        VolumeInfo info = volume.getVolumeInfo();
        return volume.getId() != null && !volume.getId().isEmpty()
                && info.getTitle() != null && !info.getTitle().isEmpty()
                && info.getAuthors() != null && !info.getAuthors().isEmpty()
                && info.getPageCount() != null && info.getPageCount() > 0
                && info.getPublishedDate() != null && !info.getPublishedDate().isEmpty()
                && info.getCategories() != null && !info.getCategories().isEmpty()
                && info.getDescription() != null && !info.getDescription().isEmpty()
                //el thumbnail hay que exigirlo: el constructor de Libro lo usa sin comprobarlo
                && info.getImageLinks() != null && info.getImageLinks().getThumbnail() != null;
    }
}
