package com.example.newgoodbooks.Fragments.HomeIU;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

        listaLibrosMostrar=new LinkedList<>(Datos.DatosComunes.getListaRecomendar());
        libroMostrado=Datos.DatosComunes.getLibroRecomendar();

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

    public void cambiarVistaLibro(){
        titulo.postValue(libroMostrado.getTitulo());
        autor.postValue(libroMostrado.getAutor().get(0));
        numPag.postValue(String.valueOf(libroMostrado.getNumPag()));
        fechaPublicacion.postValue(libroMostrado.getFechaPublicacion());
        generos.postValue(libroMostrado.getGeneros().get(0));
        descripcion.postValue(libroMostrado.getDescripcion());
        linkImagen.postValue(libroMostrado.getLinkImg());
    }
    public void cambioLibro(Context contexto){
        if(listaLibrosMostrar.size()>3){
            Executor executor= Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    AccesoFicheros accesoFicheros=new AccesoFicheros(contexto);
                    LinkedList<Libro>historial= (LinkedList<Libro>) Datos.DatosComunes.getHistorialLibros();
                    historial.addFirst(libroMostrado);
                    if(historial.size()>10){
                        historial.removeLast();
                    }
                    libroMostrado=listaLibrosMostrar.poll();
                    cambiarVistaLibro();
                    accesoFicheros.setHistorial(Datos.DatosComunes.getHistorialLibros());

                    while(listaLibrosMostrar.size()<20){
                        Libro l= ClienteBooks.getLibroAleatorio();
                        if(l!=null){
                            listaLibrosMostrar.add(l);
                        }
                    }
                    accesoFicheros.setPrincipal(libroMostrado,listaLibrosMostrar);
                }
            });
        }
    }
}