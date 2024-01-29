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
                .setApplicationName("NewGoodBooks")
                .setGoogleClientRequestInitializer(new BooksRequestInitializer("AIzaSyBJvT0i6y_bX_9xhs2-ZzSaoq8T2vzyHZE"))
                .build();
        return books;
    }
}
