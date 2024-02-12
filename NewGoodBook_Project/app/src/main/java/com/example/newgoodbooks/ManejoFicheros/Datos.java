package com.example.newgoodbooks.ManejoFicheros;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.ListasUsuario;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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

        private static String[] palabras = new String[]{
                "El", "La", "Los", "Las", "Un", "Una", "Unos", "Unas", "Y", "O",
                "De", "En", "A", "Para", "Con", "Por", "Sin", "Hacia", "Sobre", "Entre",
                "Tras", "Durante", "Ante", "Desde", "Hasta"};
        private static String[] autores = new String[]{
                "Stephen King", "Agatha Christie", "Danielle Steel", "James Patterson",
                "Nora Roberts", "J.K. Rowling", "Enid Blyton", "Terry Pratchett",
                "Isaac Asimov", "Barbara Cartland"
        };
        private static String[] generos = new String[]{
                "Thriller", "Fiction", "Science", "Romance", "Terror", "Drama", "Suspense", "Juvenil"
        };

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

        public static String getPalabraRandom(){
            Random numR = new Random();
            int indice = numR.nextInt(palabras.length);
            return palabras[indice];
        }
        public static String getAutorRandom(){
            /* if(!listasUsuario.getAutores().isEmpty()){
                Random numR=new Random();
                int indice=numR.nextInt(listasUsuario.getAutores().size());
                return listasUsuario.getAutores().get(indice);
            }else{
                Random numR = new Random();
                int indice = numR.nextInt(autores.length);
                return autores[indice];
            }*/
            Random numR = new Random();
            int indice = numR.nextInt(autores.length);
            return autores[indice];
        }
        public static String getGeneroRandom(){
            /*if(!listasUsuario.getGeneros().isEmpty()){
                Random numR = new Random();
                int indice = numR.nextInt(listasUsuario.getGeneros().size());
                return listasUsuario.getGeneros().get(indice);
            }else {
                Random numR = new Random();
                int indice = numR.nextInt(generos.length);
                return generos[indice];
            }*/
            Random numR = new Random();
            int indice = numR.nextInt(generos.length);
            return generos[indice];
        }
    }
}
