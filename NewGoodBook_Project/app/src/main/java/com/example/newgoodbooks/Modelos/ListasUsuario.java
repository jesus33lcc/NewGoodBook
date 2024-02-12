package com.example.newgoodbooks.Modelos;

import com.example.newgoodbooks.Cliente.ClienteBooks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class ListasUsuario implements Serializable {
    private List<Libro>librosLike;
    private List<Libro>librosCheck;
    private List<Lista>listas;
    public ListasUsuario(){
        librosLike=new ArrayList<>();
        librosCheck=new ArrayList<>();
        listas=new ArrayList<>();
    }
    public List<Libro> getLibrosLike() {
        return librosLike;
    }
    public List<Libro> getLibrosCheck() {
        return librosCheck;
    }
    public List<Lista> getListas() {
        return listas;
    }


    public List<String> getGeneros(){
        List<String>listaGeneros=new ArrayList<>();
        for(Libro libro:librosLike){
            listaGeneros.addAll(libro.getGeneros());
        }
        return listaGeneros;
    }

    public List<String> getAutores(){
        List<String>listaAutores=new ArrayList<>();
        for (Libro libro:librosLike){
            listaAutores.addAll(libro.getAutor());
        }
        return listaAutores;
    }
}
