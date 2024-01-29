package com.example.newgoodbooks.Cliente;

import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.model.Volume;
import com.google.api.services.books.v1.model.Volumes;

import java.io.IOException;
import java.util.ArrayList;

public class ClienteBooks {

    // Ejemplo de peticion
    public static ArrayList<String> getList(String nombre) {
        Books books=ClienteApi.getClient();
        Books.Volumes.List volumeList = null;

        ArrayList<String>titulos=new ArrayList<>();

        try {
            volumeList = books.volumes().list(nombre);
            volumeList.setOrderBy("relevance");
            Volumes volumes = volumeList.execute();
            for (Volume v : volumes.getItems()) {
                titulos.add(v.getVolumeInfo().getTitle());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return titulos;
    }
    //Ejemplo de peticion
    public static Volumes getListV(String nombre){
        Books books=ClienteApi.getClient();
        Books.Volumes.List volumeList = null;
        Volumes volumes=null;
        try {
            volumeList = books.volumes().list(nombre);
            volumeList.setOrderBy("relevance");
            volumes = volumeList.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return volumes;
    }
}
