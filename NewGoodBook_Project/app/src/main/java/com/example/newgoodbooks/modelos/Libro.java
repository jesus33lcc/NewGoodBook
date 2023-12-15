package com.example.newgoodbooks.modelos;

import java.util.Date;
import java.util.List;

public class Libro {
    private String titulo;
    private String autor;
    private int numPag;
    private Date fechaPublicacion;
    private List<String> generos;
    private String descripcion;

    public Libro(String titulo, String autor, int numPag, Date fechaPublicacion, List<String> generos, String descripcion) {
        this.titulo = titulo;
        this.autor = autor;
        this.numPag = numPag;
        this.fechaPublicacion = fechaPublicacion;
        this.generos = generos;
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getNumPag() {
        return numPag;
    }

    public void setNumPag(int numPag) {
        this.numPag = numPag;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
