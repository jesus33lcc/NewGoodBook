package com.example.newgoodbooks.Modelos;

import com.example.newgoodbooks.Cliente.ClienteBooks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class ListasUsuario implements Serializable {
    private List<String>librosLike;
    private List<String>librosCheck;
    private List<Lista>listas;
    public ListasUsuario(){
        librosLike=new ArrayList<>();
        librosCheck=new ArrayList<>();
        listas=new ArrayList<>();
    }
    public List<String> getLibrosLike() {
        return librosLike;
    }
    public List<String> getLibrosCheck() {
        return librosCheck;
    }
    public List<Lista> getListas() {
        return listas;
    }


    public List<String> getGeneros(){
        List<Libro>librosFav= ClienteBooks.getLista(librosLike);
        List<String>listaGeneros=new ArrayList<>();
        for(Libro libro:librosFav){
            listaGeneros.addAll(libro.getGeneros());
        }
        return listaGeneros;
    }

    public List<String> getAutores(){
        List<Libro>librosFav=ClienteBooks.getLista(librosLike);
        List<String>listaAutores=new ArrayList<>();
        for (Libro libro:librosFav){
            listaAutores.addAll(libro.getAutor());
        }
        return listaAutores;
    }
}
