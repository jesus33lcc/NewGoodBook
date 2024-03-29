package com.example.newgoodbooks.ManejoFicheros;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
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

        //Palabras Comunes
        private static String[] palabras = new String[]{
                "El", "La", "Los", "Las", "Un", "Una", "Unos", "Unas", "Y", "O",
                "De", "En", "A", "Para", "Con", "Por", "Sin", "Hacia", "Sobre", "Entre",
                "Tras", "Durante", "Ante", "Desde", "Hasta"};
        //autores de prueba
        private static String[] autores = new String[]{
                "Stephen King", "Agatha Christie", "Danielle Steel", "James Patterson",
                "Nora Roberts", "J.K. Rowling", "Enid Blyton", "Terry Pratchett",
                "Isaac Asimov", "Barbara Cartland"
        };
        //generos de prueba
        private static String[] generos = new String[]{
                "Thriller", "Fiction", "Science", "Romance", "Terror", "Drama", "Suspense", "Juvenil"
        };

        public static void setPrincipal(ArrayList<Object>libroYLista) {
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

        //Devuelve una palabra aleatoria para hacer una busqueda
        public static String getPalabraRandom(){
            Random numR = new Random();
            int indice = numR.nextInt(palabras.length);
            return palabras[indice];
        }
        //Devuelve un autor aleatorio o de los libros favoritos
        public static String getAutorRandom(){
            if(!listasUsuario.getAutores().isEmpty()){
                Random numR=new Random();
                int indice=numR.nextInt(listasUsuario.getAutores().size());
                return listasUsuario.getAutores().get(indice);
            }else{
                Random numR = new Random();
                int indice = numR.nextInt(autores.length);
                return autores[indice];
            }
        }
        //Devuelve un genero aleatorio o de los libros favoritos
        public static String getGeneroRandom(){
            if(!listasUsuario.getGeneros().isEmpty()){
                Random numR = new Random();
                int indice = numR.nextInt(listasUsuario.getGeneros().size());
                return listasUsuario.getGeneros().get(indice);
            }else {
                Random numR = new Random();
                int indice = numR.nextInt(generos.length);
                return generos[indice];
            }
        }
        public static Lista getListaFav(){
            Lista listaFav=new Lista("Libros Favoritos", getListasUsuario().getLibrosLike());
            return listaFav;
        }
        public static Lista getListaCheck(){
            Lista listaCheck=new Lista("Libros Leidos", getListasUsuario().getLibrosCheck());
            return listaCheck;
        }
        public static List<Lista>getListasImborrables(){
            List<Lista>listas=new ArrayList<>();
            listas.add(getListaFav());
            listas.add(getListaCheck());
            return listas;
        }
        //devuelve una lista de con los nombres de las listas personalizadas
        public static String[] getNomListasPersonal(){
            List<Lista> listas = getListasUsuario().getListas();
            int tam = listas.size();
            String[] nomListas = new String[tam];
            for(int i=0 ; i<tam ; i++) {

                nomListas[i] = listas.get(i).getNombre();
            }
            return nomListas;
        }
        //devuelve la lista por nombre introducido de las listas personalizadas
        public static Lista searchByIndexListas(int index){
            Lista listaSelected = getListasUsuario().getListas().get(index);
            return listaSelected;
        }
        public static Lista searchByNameListas(String name) {
            List<Lista> listas = DatosComunes.getListasUsuario().getListas();
            for (Lista lista : listas) {
                if (lista.getNombre().equals(name)) {
                    return lista;
                }
            }
            return null;
        }
    }
}
