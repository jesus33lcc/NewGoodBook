package com.example.newgoodbooks.ManejoFicheros;

import android.content.Context;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.ListasUsuario;
import com.example.newgoodbooks.Principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AccesoFicheros {
    private Context context;
    public AccesoFicheros(Context context){
        this.context=context;
    }
    public ArrayList<Object> getPrincipal(){
        String nombreFile="Principal";
        ArrayList<Object>libroYLista=new ArrayList<>();
        if(comprobarFichero(nombreFile)){
            try {
                FileInputStream fis=context.openFileInput(nombreFile);
                ObjectInputStream ois=new ObjectInputStream(fis);
                libroYLista.add(ois.readObject());
                libroYLista.add(ois.readObject());
                return libroYLista;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            Libro libroRecomendar=ClienteBooks.getLibroAleatorio();
            List <Libro> listaRecomendar=new LinkedList<>();
            while(listaRecomendar.size()<10){
                Libro l=ClienteBooks.getLibroAleatorio();
                if(l!=null){
                    listaRecomendar.add(l);
                }
            }
            setPrincipal(libroRecomendar,listaRecomendar);
            libroYLista.add(libroRecomendar);
            libroYLista.add(listaRecomendar);
            return libroYLista;
        }
    }
    public List<Libro> getHistorial(){
        String nombreFile="Historial";
        List<Libro>listaHistorial;
        if (comprobarFichero(nombreFile)){
            try {
                FileInputStream fis=context.openFileInput(nombreFile);
                ObjectInputStream objectInputStream=new ObjectInputStream(fis);
                listaHistorial=new LinkedList<>((List<Libro>)objectInputStream.readObject());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            listaHistorial=new LinkedList<>();
            setHistorial(listaHistorial);
        }
        return listaHistorial;
    }
    public ListasUsuario getListas(){
        String nombreFile="Listas";
        ListasUsuario listasUsuario;
        if(comprobarFichero(nombreFile)){
            try {
                FileInputStream fis=context.openFileInput(nombreFile);
                ObjectInputStream ois=new ObjectInputStream(fis);
                listasUsuario=(ListasUsuario) ois.readObject();
                return listasUsuario;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else {
            listasUsuario=new ListasUsuario();
            setListas(listasUsuario);
        }
        return listasUsuario;
    }


    public void setPrincipal(Libro escribirLibro, List<Libro>listaRecomendar){
        String nombreFile="Principal";
        comprobarFichero(nombreFile);
        try {
            FileOutputStream fos=context.openFileOutput(nombreFile, Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(escribirLibro);
            oos.writeObject(listaRecomendar);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setHistorial(List<Libro>historial){
        String nombreFile="Historial";
        comprobarFichero(nombreFile);
        try {
            FileOutputStream fos=context.openFileOutput(nombreFile, Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(historial);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setListas(ListasUsuario listasUsuario){
        String nombreFile="Listas";
        comprobarFichero(nombreFile);
        try {
            FileOutputStream fos=context.openFileOutput(nombreFile, Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(listasUsuario);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean comprobarFichero(String nombreFichero){
        File file=new File(context.getFilesDir(), nombreFichero);
        if(file.exists()){
            return true;
        }else{
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }
}
