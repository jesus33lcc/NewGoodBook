package com.example.newgoodbooks.ManejoFicheros;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.Modelos.ListasUsuario;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Datos {
    //Estado en memoria compartido por toda la app. Lo rellena MainActivity al arrancar,
    //pero los getters se auto-inicializan vacios: si Android mata el proceso y lo restaura
    //en otra actividad, la app se ve sin datos en vez de reventar con un NullPointerException.
    public static class DatosComunes {
        //Principal
        private static List<Libro> listaRecomendar;
        private static Libro libroRecomendar;

        //Explorar
        private static List<Libro> historialLibros;

        //Listas
        private static ListasUsuario listasUsuario;

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

        //recibe lo leido del fichero Principal, que puede venir vacio si aun no hay nada guardado
        public static void setPrincipal(ArrayList<Object> libroYLista) {
            if (libroYLista == null || libroYLista.size() < 2) {
                libroRecomendar = null;
                listaRecomendar = new LinkedList<>();
                return;
            }
            libroRecomendar = (Libro) libroYLista.get(0);
            @SuppressWarnings("unchecked")
            List<Libro> lista = (List<Libro>) libroYLista.get(1);
            listaRecomendar = lista != null ? lista : new LinkedList<Libro>();
        }

        //guarda en memoria el libro y la cola actuales, para que sobrevivan al cambio de pestaña
        public static void setPrincipal(Libro libro, List<Libro> lista) {
            libroRecomendar = libro;
            listaRecomendar = lista != null ? new LinkedList<>(lista) : new LinkedList<Libro>();
        }

        public static void setHistorial(List<Libro> historial) {
            historialLibros = historial;
        }

        public static void setListasUsuario(ListasUsuario listasUsu) {
            listasUsuario = listasUsu;
        }

        public static List<Libro> getListaRecomendar() {
            if (listaRecomendar == null) {
                listaRecomendar = new LinkedList<>();
            }
            return listaRecomendar;
        }

        //puede ser null: significa que todavia no se ha podido cargar ninguna recomendacion
        public static Libro getLibroRecomendar() {
            return libroRecomendar;
        }

        public static List<Libro> getHistorialLibros() {
            if (historialLibros == null) {
                historialLibros = new LinkedList<>();
            }
            return historialLibros;
        }

        public static ListasUsuario getListasUsuario() {
            if (listasUsuario == null) {
                listasUsuario = new ListasUsuario();
            }
            return listasUsuario;
        }

        //Devuelve una palabra aleatoria para hacer una busqueda
        public static String getPalabraRandom() {
            Random numR = new Random();
            return palabras[numR.nextInt(palabras.length)];
        }

        //Devuelve un autor de los libros favoritos, o uno de prueba si aun no hay favoritos
        public static String getAutorRandom() {
            Random numR = new Random();
            List<String> propios = getListasUsuario().getAutores();
            if (!propios.isEmpty()) {
                return propios.get(numR.nextInt(propios.size()));
            }
            return autores[numR.nextInt(autores.length)];
        }

        //Devuelve un genero de los libros favoritos, o uno de prueba si aun no hay favoritos
        public static String getGeneroRandom() {
            Random numR = new Random();
            List<String> propios = getListasUsuario().getGeneros();
            if (!propios.isEmpty()) {
                return propios.get(numR.nextInt(propios.size()));
            }
            return generos[numR.nextInt(generos.length)];
        }

        public static Lista getListaFav() {
            return new Lista("Libros Favoritos", getListasUsuario().getLibrosLike());
        }

        public static Lista getListaCheck() {
            return new Lista("Libros Leidos", getListasUsuario().getLibrosCheck());
        }

        public static List<Lista> getListasImborrables() {
            List<Lista> listas = new ArrayList<>();
            listas.add(getListaFav());
            listas.add(getListaCheck());
            return listas;
        }

        //devuelve una lista con los nombres de las listas personalizadas
        public static String[] getNomListasPersonal() {
            List<Lista> listas = getListasUsuario().getListas();
            String[] nomListas = new String[listas.size()];
            for (int i = 0; i < listas.size(); i++) {
                nomListas[i] = listas.get(i).getNombre();
            }
            return nomListas;
        }

        //devuelve la lista personalizada por indice, o null si el indice no es valido
        public static Lista searchByIndexListas(int index) {
            List<Lista> listas = getListasUsuario().getListas();
            if (index < 0 || index >= listas.size()) {
                return null;
            }
            return listas.get(index);
        }

        public static Lista searchByNameListas(String name) {
            for (Lista lista : getListasUsuario().getListas()) {
                if (lista.getNombre().equals(name)) {
                    return lista;
                }
            }
            return null;
        }
    }
}
