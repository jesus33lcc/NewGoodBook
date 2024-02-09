package com.example.newgoodbooks.ManejoFicheros;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.ListasUsuario;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Datos {
    public static class DatosComunes {
        //Principal
        private static List<Libro>listaRecomendar;
        private static Libro libroRecomendar;

        //Explorar
        private static List<Libro>historialLibros;

        //Listas
        private static ListasUsuario listasUsuario;

        //Datos
        private static List<String>listaGeneros;
        private static List<String>listaAutores;



        public static void setPrincipal(ArrayList<Object>libroYLista) {
            //listaRecomendar = ClienteBooks.buscarTitulo("Harry Potter");
            //libroRecomendar=listaRecomendar.get(0);
            libroRecomendar=(Libro)libroYLista.get(0);
            listaRecomendar=(List<Libro>) libroYLista.get(1);
        }
        public static void setHistorial(List<Libro>historial){
            historialLibros=historial;
        }
        public static void setListasUsuario(ListasUsuario listasUsu){
            listasUsuario=listasUsu;
        }

        public static List<Libro> getListaRecomendar(){
            return listaRecomendar;
        }
        public static Libro getLibroRecomendar(){ return libroRecomendar; }
        public static List<Libro>getHistorialLibros(){ return historialLibros; }
        public static ListasUsuario getListasUsuario(){ return listasUsuario; }
        public static List<String> getListaGeneros(){ return listaGeneros; }
        public static List<String> getListaAutores(){ return listaAutores; }
    }
}
