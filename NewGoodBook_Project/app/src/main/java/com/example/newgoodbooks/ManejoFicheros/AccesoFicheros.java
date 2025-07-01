package com.example.newgoodbooks.ManejoFicheros;

import android.content.Context;
import android.util.Log;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.ListasUsuario;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//Lectura/escritura de los datos del usuario en el almacenamiento interno.
//Ningun metodo lanza excepciones: si un fichero no existe o esta corrupto se empieza de cero.
//IMPORTANTE: los modelos guardados son Serializable y NO declaran serialVersionUID,
//asi que cambiar sus campos invalida lo ya guardado en el dispositivo.
public class AccesoFicheros {
    private static final String TAG = "AccesoFicheros";
    private static final String FICHERO_PRINCIPAL = "Principal";
    private static final String FICHERO_HISTORIAL = "Historial";
    private static final String FICHERO_LISTAS = "Listas";

    private final Context context;

    //Constructor que pide el contexto de una actividad de donde se llama
    public AccesoFicheros(Context context) {
        this.context = context;
    }

    //devuelve [Libro, List<Libro>] guardados, o una lista VACIA si todavia no hay nada.
    //no hace red: rellenar las recomendaciones es responsabilidad de la pantalla.
    public ArrayList<Object> getPrincipal() {
        ArrayList<Object> libroYLista = new ArrayList<>();
        if (!tieneContenido(FICHERO_PRINCIPAL)) {
            return libroYLista;
        }
        try (ObjectInputStream ois = new ObjectInputStream(context.openFileInput(FICHERO_PRINCIPAL))) {
            Libro libro = (Libro) ois.readObject();
            @SuppressWarnings("unchecked")
            List<Libro> lista = (List<Libro>) ois.readObject();
            if (libro != null) {
                libroYLista.add(libro);
                libroYLista.add(lista != null ? lista : new LinkedList<Libro>());
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo leer '" + FICHERO_PRINCIPAL + "', se empieza de cero", e);
            libroYLista.clear();
        }
        return libroYLista;
    }

    //devuelve el historial guardado, o una lista vacia
    public List<Libro> getHistorial() {
        if (!tieneContenido(FICHERO_HISTORIAL)) {
            return new LinkedList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(context.openFileInput(FICHERO_HISTORIAL))) {
            @SuppressWarnings("unchecked")
            List<Libro> historial = (List<Libro>) ois.readObject();
            return historial != null ? new LinkedList<>(historial) : new LinkedList<Libro>();
        } catch (Exception e) {
            Log.w(TAG, "No se pudo leer '" + FICHERO_HISTORIAL + "', se empieza de cero", e);
            return new LinkedList<>();
        }
    }

    //devuelve las listas del usuario, o un objeto vacio
    public ListasUsuario getListas() {
        if (!tieneContenido(FICHERO_LISTAS)) {
            return new ListasUsuario();
        }
        try (ObjectInputStream ois = new ObjectInputStream(context.openFileInput(FICHERO_LISTAS))) {
            ListasUsuario listasUsuario = (ListasUsuario) ois.readObject();
            return listasUsuario != null ? listasUsuario : new ListasUsuario();
        } catch (Exception e) {
            Log.w(TAG, "No se pudo leer '" + FICHERO_LISTAS + "', se empieza de cero", e);
            return new ListasUsuario();
        }
    }

    //Recibe un libro y una lista y reescribe el fichero Principal
    public void setPrincipal(Libro escribirLibro, List<Libro> listaRecomendar) {
        escribir(FICHERO_PRINCIPAL, escribirLibro, listaRecomendar);
    }

    //Recibe una lista y reescribe el fichero Historial
    public void setHistorial(List<Libro> historial) {
        escribir(FICHERO_HISTORIAL, historial);
    }

    //Recibe las listas del usuario y reescribe el fichero Listas
    public void setListas(ListasUsuario listasUsuario) {
        escribir(FICHERO_LISTAS, listasUsuario);
    }

    //escribe los objetos en orden. el try-with-resources cierra el stream,
    //que es lo que de verdad vuelca los datos a disco.
    private void escribir(String nombreFichero, Object... objetos) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(context.openFileOutput(nombreFichero, Context.MODE_PRIVATE))) {
            for (Object o : objetos) {
                oos.writeObject(o);
            }
        } catch (Exception e) {
            Log.e(TAG, "No se pudo guardar '" + nombreFichero + "'", e);
        }
    }

    //borra todo lo guardado. se usa al cerrar sesion, para que el siguiente usuario
    //de este dispositivo no se encuentre las listas ni el historial del anterior.
    public void borrarTodo() {
        for (String nombre : new String[]{FICHERO_PRINCIPAL, FICHERO_HISTORIAL, FICHERO_LISTAS}) {
            File fichero = new File(context.getFilesDir(), nombre);
            if (fichero.exists() && !fichero.delete()) {
                Log.w(TAG, "No se pudo borrar '" + nombre + "'");
            }
        }
    }

    //true solo si el fichero existe Y tiene datos (uno vacio cuenta como "no hay nada")
    private boolean tieneContenido(String nombreFichero) {
        File file = new File(context.getFilesDir(), nombreFichero);
        return file.exists() && file.length() > 0;
    }
}
