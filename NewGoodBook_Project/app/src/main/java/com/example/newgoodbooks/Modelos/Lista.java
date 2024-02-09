package com.example.newgoodbooks.Modelos;

import java.io.Serializable;
import java.util.List;

public class Lista implements Serializable {
    private String nombre;
    private List<String> librosId;

    public Lista(String nombre, List<String> librosId) {
        this.nombre = nombre;
        this.librosId = librosId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getLibrosId() {
        return librosId;
    }

    public void setLibrosId(List<String> librosId) {
        this.librosId = librosId;
    }

}
