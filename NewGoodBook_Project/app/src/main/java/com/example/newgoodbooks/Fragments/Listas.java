package com.example.newgoodbooks.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newgoodbooks.Fragments.AdapterList.ListaListAdapter;
import com.example.newgoodbooks.Fragments.AdapterList.ListaImborrableAdapter;
import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.Modelos.ListasUsuario;
import com.example.newgoodbooks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Listas extends Fragment {
    private View view;
    private RecyclerView listasRecyclerView;
    private RecyclerView misListasRecyclerView;
    ListaListAdapter listaListAdapter;
    ListaImborrableAdapter listaImborrableAdapter;
    List<Lista> milistadoListasList;
    List<Lista> listadoListasList;

    public Listas() {
    } // Se requiere de un constructor vacio.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_listas, container, false);

        // Sobre el RecyclerView
        listasRecyclerView = view.findViewById(R.id.misListasCheckFav);
        misListasRecyclerView = view.findViewById(R.id.misListaPersonalizadas);

        listasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        misListasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Insertar Libros en lista.
        List<Lista> listaLibrosVacia = new ArrayList<>();


        // Insertar listas en el RecyclerList
        listadoListasList = Datos.DatosComunes.getListasImborrables();
        initialize_ListFillInmborrable(listadoListasList);

        milistadoListasList = Datos.DatosComunes.getListasUsuario().getListas();
        initialize_ListFillList(milistadoListasList);

        return view;
    }

    public void initialize_ListFillInmborrable(List<Lista> listaListasFill) {
        listaImborrableAdapter = new ListaImborrableAdapter(getActivity(),listaListasFill);
        listasRecyclerView.setAdapter(listaImborrableAdapter);
    }
    public void initialize_ListFillList(List<Lista> listaListasFill) {
        listaListAdapter = new ListaListAdapter(getActivity(),listaListasFill);
        misListasRecyclerView.setAdapter(listaListAdapter);
    }
}