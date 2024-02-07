package com.example.newgoodbooks.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeIU#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinkedList<Libro>listaRecomendar;
    private Libro mostrado;
    private TextView titulo;
    private TextView autor;
    private TextView numPag;
    private TextView fecha;
    private TextView genero;
    private TextView descripcion;
    private Button botonSig;
    private ImageView portada;
    public Home() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        titulo= view.findViewById(R.id.textTitulo);
        autor=view.findViewById(R.id.textAutor);
        numPag=view.findViewById(R.id.textNumPag);
        fecha=view.findViewById(R.id.textFechaPub);
        genero=view.findViewById(R.id.textGeneros);
        descripcion=view.findViewById(R.id.textDescripcion);
        botonSig=view.findViewById(R.id.btnSiguiente);
        portada=view.findViewById(R.id.imageVPortada);

        cargarLista();
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizar();
            }
        });

    }

    public void cargarLista(){
        Executor executor= Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                cargar();
                actualizar();
            }
        });
    }
    public void actualizar(){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Picasso.get().load(mostrado.getLinkImg().replace("http", "https")).placeholder(R.drawable.ic_home).into(portada);
                titulo.setText(mostrado.getTitulo());
                autor.setText(mostrado.getAutor().get(0));
                numPag.setText(String.valueOf(mostrado.getNumPag()));
                fecha.setText(mostrado.getFechaPublicacion());
                genero.setText(mostrado.getGeneros().get(0));
                descripcion.setText(mostrado.getDescripcion());
                mostrado=listaRecomendar.poll();
            }
        });
    }
    private void grabar(){
        String nombreFile="Inicio";
        try {
            FileOutputStream fos=getContext().openFileOutput(nombreFile, Context.MODE_PRIVATE);
            ObjectOutputStream os=new ObjectOutputStream(fos);
            os.writeObject(listaRecomendar);
            os.writeObject(mostrado);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void cargar(){
        String nombreFile="Inicio";
        try {
            FileInputStream fis=getContext().openFileInput(nombreFile);
            ObjectInputStream is=new ObjectInputStream(fis);
            listaRecomendar=(LinkedList<Libro>) is.readObject();
            mostrado=(Libro) is.readObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}