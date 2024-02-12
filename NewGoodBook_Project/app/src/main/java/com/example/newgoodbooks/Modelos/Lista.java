package com.example.newgoodbooks.Modelos;

import java.io.Serializable;
import java.util.List;

public class Lista implements Serializable {
    private String nombre;
    private List<Libro> libros;

    public Lista(String nombre, List<Libro> libros) {
        this.nombre = nombre;
        this.libros = libros;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibrosId(List<Libro> libros) {
        this.libros = libros;
    }

}
