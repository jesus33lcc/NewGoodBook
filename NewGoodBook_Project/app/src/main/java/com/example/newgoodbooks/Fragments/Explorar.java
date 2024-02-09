package com.example.newgoodbooks.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Explorar extends Fragment {

    private static Explorar instance;
    private View view;
    private RecyclerView librosRecyclerView;
    LibroListAdapter libroListAdapter;

    List<Libro> librosRecyclerList;
    Toolbar toolbarSearch;
    private MenuItem menuItem;
    private SearchView searchViewExplorar;
    public Explorar() { }

    public static Explorar getInstance() {
        if(instance == null){
            instance = new Explorar();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_explorar,container,false);

        // Obtener referencia del Toolbar desde el layout "fragment_explorar"
        toolbarSearch = view.findViewById(R.id.myToolbarExplorer);
        /*searchViewExplorar = view.findViewById(R.id.app_searchViewBook);
        searchViewExplorar.setQueryHint("Buscar...");*/

        //toolbarSearch.setTitle("Explorar");
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        int whiteColor = ContextCompat.getColor(getActivity(), android.R.color.white);
        toolbarSearch.setTitleTextColor(whiteColor);
        activity.setSupportActionBar(toolbarSearch);
        activity.getSupportActionBar().setTitle("Explorar");


        // Sobre el RecyclerView
        librosRecyclerView = view.findViewById(R.id.listRecyclerLibros);
        librosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Buscar info sobre: FirebaseRecyclerOptions<Libro> listaFill
        List<Libro> listaLibrosFill = new ArrayList<>();
        initialize_ListFillBook(listaLibrosFill);

        // Establezco el Toolbar como ActionBar del fragmento
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarSearch);
        buscar();
        return view;
    }

    public void initialize_ListFillBook(List<Libro> listaLibrosFill){
        libroListAdapter = new LibroListAdapter(getActivity(),listaLibrosFill);
        librosRecyclerView.setAdapter(libroListAdapter);
    }

    private void buscar(){
        Executor executor= Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                librosRecyclerList=new ArrayList<>(Datos.DatosComunes.getListaRecomendar());
                fillRecycleList();
                /*getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initialize_ListFillBook(librosRecyclerList);
                    }
                });*/
            }
        });
    }
    private void fillRecycleList(){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                initialize_ListFillBook(librosRecyclerList);
            }
        });
    }
}