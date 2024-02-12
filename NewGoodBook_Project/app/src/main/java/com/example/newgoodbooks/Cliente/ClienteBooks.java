package com.example.newgoodbooks.Cliente;

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

public class ClienteBooks {
    public static List<Libro> buscarTitulo(String nombre) {
        Books books=ClienteApi.getClient();
        Books.Volumes.List volumeList = null;
        ArrayList<Libro>listaLibros=new ArrayList<>();
        long max = 40;
        int num = 0;
        try {
            volumeList = books.volumes().list(nombre);
            volumeList.setOrderBy("relevance");
            volumeList.setMaxResults(max);
            volumeList.setStartIndex(max * num++);
            Volumes volumes;
            volumes=volumeList.execute();
            while (volumes.getItems()!=null) {
                for (Volume v : volumes.getItems()) {
                    if (esValido(v)) {
                        Libro libro = new Libro(v);
                        listaLibros.add(libro);
                    }
                }
                if(listaLibros.size()>20){
                    return listaLibros;
                }
                volumeList.setStartIndex(max * num++);
                volumes = volumeList.execute();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listaLibros;
    }
    public static Libro getLibro(String id){
        Books books=ClienteApi.getClient();
        try {
            return new Libro(books.volumes().get(id).execute());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Libro>getLista(List<String>listaId){
        List<Libro>listaLibros=new ArrayList<>();
        for(String id:listaId){
            listaLibros.add(getLibro(id));
        }
        return listaLibros;
    }
    public static Libro getLibroAleatorio(){
        Libro libroAleatorio=null;

        Books books=ClienteApi.getClient();
        Books.Volumes.List volumeList = null;
        ArrayList<Libro>listaLibros=new ArrayList<>();

        long max = 40;

        String query= Datos.DatosComunes.getPalabraRandom();
        Random random=new Random();
        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                query += "+inauthor:" + Datos.DatosComunes.getAutorRandom();
            } else {
                query += "+subject:" + Datos.DatosComunes.getGeneroRandom();
            }
        }
        try {
            volumeList = books.volumes().list(query);
            volumeList.setPrintType("books");
            volumeList.setOrderBy("relevance");
            volumeList.setMaxResults(max);
            Volumes volumes=volumeList.execute();
            if(volumes.isEmpty()){
                return null;
            }
            for (Volume v : volumes.getItems()) {
                //System.out.println(v.getVolumeInfo().getLanguage());
                if (esValido(v)) {
                    Libro libro = new Libro(v);
                    listaLibros.add(libro);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (listaLibros.size() == 0) {
            return null;
        }
        int indice=new Random().nextInt(listaLibros.size());
        return listaLibros.get(indice);
    }

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
                && info.getImageLinks() != null && !info.getImageLinks().isEmpty();
    }
}
