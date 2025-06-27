package com.example.newgoodbooks.Cliente;

import android.util.Log;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.BooksRequestInitializer;

public class ClienteApi {
    private static final String TAG = "ClienteApi";
    //Clave de la api y nombre del proyecto
    private final static String CLAVE_API = "AIzaSyAzPpXG_8OTDowZqDZ-k6yKe8nWTXf1iQI";
    private final static String NOMBRE_PROYECTO = "NewGoodB";

    private static Books books;

    //devuelve el objeto Books al cual se le hacen las peticiones.
    //se construye una sola vez y se reutiliza. devuelve null si no se pudo crear.
    public static synchronized Books getClient() {
        if (books != null) {
            return books;
        }
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            books = new Books.Builder(httpTransport, jsonFactory, null)
                    .setApplicationName(NOMBRE_PROYECTO)
                    .setGoogleClientRequestInitializer(new BooksRequestInitializer(CLAVE_API))
                    .build();
        } catch (Exception e) {
            //sin cliente no hay peticiones, pero no se tumba la app: quien llame maneja el null
            Log.e(TAG, "No se pudo crear el cliente de Google Books", e);
            books = null;
        }
        return books;
    }
}
