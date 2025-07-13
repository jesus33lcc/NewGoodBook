package com.example.newgoodbooks.Modelos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Lista implements Serializable {
    //id del documento en Firestore. Las listas fijas (favoritos y leidos) no son
    //documentos reales y usan los ids sinteticos de abajo.
    public static final String ID_FAVORITOS = "__favoritos";
    public static final String ID_LEIDOS = "__leidos";
    //Nombres de las listas fijas. Viven aqui y no en strings.xml porque tambien se
    //usan desde clases sin Context (el repositorio), y porque hacen de identificador
    //ademas de etiqueta. Antes estaban repetidos como literal en tres ficheros.
    public static final String NOMBRE_FAVORITOS = "Libros Favoritos";
    public static final String NOMBRE_LEIDOS = "Libros Leidos";

    private String id;
    private String nombre;
    private List<Libro> libros;

    public Lista(String nombre, List<Libro> libros) {
        this(null, nombre, libros);
    }

    public Lista(String id, String nombre, List<Libro> libros) {
        this.id = id;
        this.nombre = nombre;
        this.libros = libros != null ? libros : new ArrayList<Libro>();
    }

    //Reconstruye una lista a partir de un documento de Firestore
    public static Lista desdeMapa(String id, Map<?, ?> mapa) {
        if (mapa == null) {
            return null;
        }
        String nombre = mapa.get("nombre") == null ? null : String.valueOf(mapa.get("nombre"));
        if (nombre == null || nombre.isEmpty()) {
            return null;
        }
        List<Libro> libros = new ArrayList<>();
        Object guardados = mapa.get("libros");
        if (guardados instanceof List) {
            for (Object elemento : (List<?>) guardados) {
                if (elemento instanceof Map) {
                    Libro libro = Libro.desdeMapa((Map<?, ?>) elemento);
                    if (libro != null) {
                        libros.add(libro);
                    }
                }
            }
        }
        return new Lista(id, nombre, libros);
    }

    public List<Map<String, Object>> librosComoMapas() {
        List<Map<String, Object>> salida = new ArrayList<>();
        for (Libro libro : libros) {
            salida.add(libro.aMapa());
        }
        return salida;
    }

    //true si es una de las dos listas que no se pueden borrar ni renombrar
    public boolean esImborrable() {
        return ID_FAVORITOS.equals(id) || ID_LEIDOS.equals(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros != null ? libros : new ArrayList<Libro>();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Lista) obj).id);
    }
}
