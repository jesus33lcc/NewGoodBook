package com.example.newgoodbooks.modelos;

import com.google.api.services.books.v1.model.Volume;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Libro {
    private String id;
    private String titulo;
    private List<String> autor;
    private int numPag;
    private String fechaPublicacion;
    private List<String> generos;
    private String descripcion;
    private String linkImg;

    public Libro(String id, String titulo, List<String> autor, int numPag, String fechaPublicacion, List<String> generos, String descripcion, String linkImg) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.numPag = numPag;
        this.fechaPublicacion = fechaPublicacion;
        this.generos = generos;
        this.descripcion = descripcion;
        this.linkImg = linkImg;
    }

    public Libro() {
    }
    public Libro(Volume volume){
        id=volume.getId();
        titulo=volume.getVolumeInfo().getTitle();
        autor=new ArrayList<>(volume.getVolumeInfo().getAuthors());
        numPag=volume.getVolumeInfo().getPageCount();
        fechaPublicacion=volume.getVolumeInfo().getPublishedDate();
        generos=new ArrayList<>(volume.getVolumeInfo().getCategories());
        descripcion=volume.getVolumeInfo().getDescription();
        linkImg=volume.getVolumeInfo().getImageLinks().getThumbnail();
    }
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("ID: "+id+"\n");
        builder.append("Titulo: "+titulo+"\n");
        builder.append("Autor: "+autor.get(0)+"\n");
        builder.append("NumPag: "+numPag+"\n");
        builder.append("Fecha de publicacion: "+fechaPublicacion+"\n");
        builder.append("Genero: "+generos.get(0)+"\n");
        builder.append("Descripcion: "+descripcion+"\n");
        builder.append("LinkImg. "+linkImg+"\n");
        return builder.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getAutor() {
        return autor;
    }

    public void setAutor(List<String> autor) {
        this.autor = autor;
    }

    public int getNumPag() {
        return numPag;
    }

    public void setNumPag(int numPag) {
        this.numPag = numPag;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
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

    public String getLinkImg() {
        return linkImg;
    }

    public void setLinkImg(String linkImg) {
        this.linkImg = linkImg;
    }

}
