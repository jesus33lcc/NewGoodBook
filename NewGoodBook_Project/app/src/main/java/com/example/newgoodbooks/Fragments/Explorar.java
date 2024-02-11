package com.example.newgoodbooks.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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
import android.widget.EditText;
import android.widget.ImageView;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.LibroData;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.example.newgoodbooks.ResultadoSearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Explorar extends Fragment {

    private static Explorar instance;
    private View view;
    private RecyclerView librosRecyclerView;
    LibroListAdapter libroListAdapter;
    List<Libro> listadoLibrosList;
    Toolbar toolbarSearch;
    ImageView imgSearch;
    private MenuItem menuItem;
    private SearchView searchViewExplorar;
    public Explorar() { }

    /*public static Explorar getInstance() {
        if(instance == null){
            instance = new Explorar();
        }
        return instance;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_explorar,container,false);

        // Sobre el RecyclerView
        librosRecyclerView = view.findViewById(R.id.listRecyclerLibros);
        librosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Insertar Libros en lista.
        List<Libro> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillBook(listaLibrosVacia);
        buscar();

        // Sobre el Toolbar
        toolbarSearch = view.findViewById(R.id.myToolbarExplorer);

        // Sobre el SearchView
        MenuItem searchItem = toolbarSearch.getMenu().findItem(R.id.action_searchExplore);
        searchViewExplorar = (SearchView) searchItem.getActionView();
        searchViewExplorar.setQueryHint("Buscar titulo...");

        // Configuracion color del SearchView
        int colorBlanco = ContextCompat.getColor(requireContext(), android.R.color.white);
        EditText searchEditText = searchViewExplorar.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(colorBlanco);
        searchEditText.setHintTextColor(colorBlanco);
        Drawable iconoSearch = searchItem.getIcon();
        iconoSearch.setColorFilter(colorBlanco, PorterDuff.Mode.SRC_ATOP);

        // Configuracion de busqueda del SearchView
        searchViewExplorar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent viewResultadosActivity = new Intent(getContext(), ResultadoSearchView.class);
                viewResultadosActivity.putExtra("titulo_a_buscar", query);
                getContext().startActivity(viewResultadosActivity);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
                listadoLibrosList =new ArrayList<>(Datos.DatosComunes.getListaRecomendar());
                fillRecycleList();
            }
        });
    }
    private void fillRecycleList(){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                initialize_ListFillBook(listadoLibrosList);
            }
        });
    }
}