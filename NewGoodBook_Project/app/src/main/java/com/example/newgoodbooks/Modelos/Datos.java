package com.example.newgoodbooks.Modelos;

import com.example.newgoodbooks.Cliente.ClienteBooks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Datos {
    public static class DatosComunes {
        private static List<Libro> datos = new LinkedList<>();

        public static void cargarDatos() {
            datos = ClienteBooks.buscarTitulo("Harry Potter");
        }
        public static List<Libro> descargarDatos(){
            return datos;
        }
    }
}
