package com.example.newgoodbooks.Fragments.HomeIU;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Modelos.Datos;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private Libro libroMostrado;
    private LinkedList<Libro> listaLibrosMostrar;
    private MutableLiveData<String> titulo;
    private MutableLiveData<String> autor;
    private MutableLiveData<String> numPag;
    private MutableLiveData<String> fechaPublicacion;
    private MutableLiveData<String> generos;
    private MutableLiveData<String> descripcion;
    private MutableLiveData<String> linkImagen;

    public HomeViewModel(){
        titulo=new MutableLiveData<>();
        autor=new MutableLiveData<>();
        numPag=new MutableLiveData<>();
        fechaPublicacion=new MutableLiveData<>();
        generos=new MutableLiveData<>();
        descripcion=new MutableLiveData<>();
        linkImagen=new MutableLiveData<>();

        listaLibrosMostrar=new LinkedList<>(Datos.DatosComunes.descargarDatos());
        libroMostrado=listaLibrosMostrar.poll();


        linkImagen.setValue(libroMostrado.getLinkImg());
        titulo.setValue(libroMostrado.getTitulo());
        autor.setValue(libroMostrado.getAutor().get(0));
        numPag.setValue(String.valueOf(libroMostrado.getNumPag()));
        fechaPublicacion.setValue(libroMostrado.getFechaPublicacion());
        generos.setValue(libroMostrado.getGeneros().get(0));
        descripcion.setValue(libroMostrado.getDescripcion());

    }

    public LiveData<String> getTitulo() {
        return titulo;
    }
    public LiveData<String> getAutor() {
        return autor;
    }
    public LiveData<String> getNumPag() {
        return numPag;
    }
    public LiveData<String> getFechaPublicacion() {
        return fechaPublicacion;
    }
    public LiveData<String> getGeneros() {
        return generos;
    }
    public LiveData<String> getDescripcion() {
        return descripcion;
    }

    public LiveData<String> getLinkImagen() {
        return linkImagen;
    }

    public void cambiarLibro(){
        libroMostrado=listaLibrosMostrar.poll();
        titulo.setValue(libroMostrado.getTitulo());
        autor.setValue(libroMostrado.getAutor().get(0));
        numPag.setValue(String.valueOf(libroMostrado.getNumPag()));
        fechaPublicacion.setValue(libroMostrado.getFechaPublicacion());
        generos.setValue(libroMostrado.getGeneros().get(0));
        descripcion.setValue(libroMostrado.getDescripcion());
        linkImagen.setValue(libroMostrado.getLinkImg());
    }
}