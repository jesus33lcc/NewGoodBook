package com.example.newgoodbooks.Modelos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Libro implements Serializable {
    private String id;
    private String titulo;
    private List<String> autor;
    private int numPag;
    private String fechaPublicacion;
    private List<String> generos;
    private String descripcion;
    private String linkImg;
    //Datos que aporta Open Library, no Google Books. Son OPCIONALES a proposito: los
    //libros ya guardados en Firestore no los tienen, y muchos volumenes tampoco. Nada
    //de esto puede hacer que un libro se descarte.
    private double valoracion;
    private int numVotos;
    private List<String> materias;
    private String isbn;
    private String editorial;

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

    //Construye un Libro con lo que devuelve la Cloud Function.
    //Devuelve null si al mapa le falta algo imprescindible, para que nunca
    //llegue a la pantalla un libro a medias.
    public static Libro desdeMapa(Map<?, ?> mapa) {
        if (mapa == null) {
            return null;
        }
        Libro libro = new Libro();
        libro.id = texto(mapa.get("id"));
        libro.titulo = texto(mapa.get("titulo"));
        libro.autor = listaTextos(mapa.get("autor"));
        libro.fechaPublicacion = texto(mapa.get("fechaPublicacion"));
        libro.generos = listaTextos(mapa.get("generos"));
        libro.descripcion = texto(mapa.get("descripcion"));
        libro.linkImg = texto(mapa.get("linkImg"));

        Object paginas = mapa.get("numPag");
        libro.numPag = (paginas instanceof Number) ? ((Number) paginas).intValue() : 0;

        //los opcionales se leen sin exigirlos: si faltan quedan a cero o vacios
        Object nota = mapa.get("valoracion");
        libro.valoracion = (nota instanceof Number) ? ((Number) nota).doubleValue() : 0;
        Object votos = mapa.get("numVotos");
        libro.numVotos = (votos instanceof Number) ? ((Number) votos).intValue() : 0;
        libro.materias = listaTextos(mapa.get("materias"));
        libro.isbn = texto(mapa.get("isbn"));
        libro.editorial = texto(mapa.get("editorial"));

        if (libro.id == null || libro.titulo == null || libro.linkImg == null
                || libro.autor.isEmpty() || libro.generos.isEmpty()) {
            return null;
        }
        return libro;
    }

    //Representacion para guardar en Firestore. Mismas claves que usa la Cloud Function,
    //asi que desdeMapa() sirve para leer de las dos fuentes.
    public Map<String, Object> aMapa() {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("id", id);
        mapa.put("titulo", titulo);
        mapa.put("autor", autor != null ? autor : new ArrayList<String>());
        mapa.put("numPag", numPag);
        mapa.put("fechaPublicacion", fechaPublicacion);
        mapa.put("generos", generos != null ? generos : new ArrayList<String>());
        mapa.put("descripcion", descripcion);
        mapa.put("linkImg", linkImg);
        mapa.put("valoracion", valoracion);
        mapa.put("numVotos", numVotos);
        mapa.put("materias", materias != null ? materias : new ArrayList<String>());
        mapa.put("isbn", isbn);
        mapa.put("editorial", editorial);
        return mapa;
    }

    //Hay valoracion util solo si la respaldan unos cuantos votos: una nota de 5,0
    //puesta por una sola persona no dice nada y quedaria fatal en la ficha.
    public boolean tieneValoracion() {
        return valoracion > 0 && numVotos >= MINIMO_VOTOS;
    }

    public static final int MINIMO_VOTOS = 5;

    private static String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    private static List<String> listaTextos(Object valor) {
        List<String> salida = new ArrayList<>();
        if (valor instanceof List) {
            for (Object elemento : (List<?>) valor) {
                if (elemento != null) {
                    salida.add(String.valueOf(elemento));
                }
            }
        }
        return salida;
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

    public double getValoracion() {
        return valoracion;
    }

    public void setValoracion(double valoracion) {
        this.valoracion = valoracion;
    }

    public int getNumVotos() {
        return numVotos;
    }

    public void setNumVotos(int numVotos) {
        this.numVotos = numVotos;
    }

    public List<String> getMaterias() {
        return materias != null ? materias : new ArrayList<String>();
    }

    public void setMaterias(List<String> materias) {
        this.materias = materias;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Libro other = (Libro) obj;
        return Objects.equals(this.id, other.id);
    }
}
