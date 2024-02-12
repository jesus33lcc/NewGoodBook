package com.example.newgoodbooks.Cliente;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.BooksRequestInitializer;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ClienteApi {
    private static Books books;
    private final static String CLAVE_API="AIzaSyBJvT0i6y_bX_9xhs2-ZzSaoq8T2vzyHZE";
    private final static String NOMBRE_PROYECTO="NewGoodBook";
    private final static String CLAVE_API_2="AIzaSyAzPpXG_8OTDowZqDZ-k6yKe8nWTXf1iQI";
    private final static String NOMBRE_PROYECTO_2="NewGoodB";
    public static Books getClient(){
        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        HttpRequestInitializer httpRequestInitializer = null;
        books = new Books.Builder(httpTransport, jsonFactory, httpRequestInitializer)
                .setApplicationName(NOMBRE_PROYECTO)
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(CLAVE_API))
                .build();
        return books;
    }
}
