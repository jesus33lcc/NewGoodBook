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

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.LibroData;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.UI.EstadoVacio;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.example.newgoodbooks.databinding.FragmentExplorarBinding;
import com.example.newgoodbooks.UI.ModoVista;
import com.example.newgoodbooks.ResultadoSearchView;

import java.util.ArrayList;
import java.util.List;


public class Explorar extends Fragment {
    private FragmentExplorarBinding binding;
    private View view;
    private RecyclerView rejillaLibros;
    LibroListAdapter adaptadorLibros;
    Toolbar barraExplorar;
    private SearchView buscador;

    public Explorar() { } // Se requiere de un constructor vacio.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExplorarBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        // Sobre el RecyclerView
        rejillaLibros = binding.listRecyclerLibros;


        // Insertar Libros en lista.
        montarAdaptador(new ArrayList<Libro>());
        //el historial llega solo desde Firestore; antes se leia de un estatico
        //envuelto en un hilo y un Handler que no hacian falta para nada
        final View vacio = binding.estadoVacio.getRoot();
        RepositorioUsuario.get().getHistorial().observe(getViewLifecycleOwner(), libros -> {
            adaptadorLibros.actualizar(libros);
            EstadoVacio.mostrar(vacio, libros == null || libros.isEmpty(),
                    R.drawable.ic_explorar, R.string.vacio_explorar_titulo,
                    R.string.vacio_explorar_detalle);
        });

        // Sobre el Toolbar
        barraExplorar = binding.myToolbarExplorer;

        // Sobre el SearchView
        MenuItem searchItem = barraExplorar.getMenu().findItem(R.id.action_searchExplore);
        buscador = (SearchView) searchItem.getActionView();
        buscador.setQueryHint(getString(R.string.buscar_hint));

        //Conmutador de lista o cuadricula. La eleccion es comun a las tres pantallas
        //que ensenian libros, asi que se guarda y se lee desde ModoVista.
        ModoVista.pintarIcono(requireContext(), barraExplorar.getMenu());
        barraExplorar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_vista) {
                ModoVista.alternar(requireContext());
                ModoVista.pintarIcono(requireContext(), barraExplorar.getMenu());
                ModoVista.aplicar(requireContext(), rejillaLibros, adaptadorLibros);
                return true;
            }
            return false;
        });

        // Configuracion color del SearchView
        int colorBlanco = ContextCompat.getColor(requireContext(), android.R.color.white);
        EditText searchEditText = buscador.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(colorBlanco);
        searchEditText.setHintTextColor(colorBlanco);
        Drawable iconoSearch = searchItem.getIcon();
        iconoSearch.setColorFilter(colorBlanco, PorterDuff.Mode.SRC_ATOP);

        // Configuracion de busqueda del SearchView
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent viewResultadosActivity = new Intent(getContext(), ResultadoSearchView.class);
                viewResultadosActivity.putExtra(ResultadoSearchView.EXTRA_CONSULTA, query);
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

    public void montarAdaptador(List<Libro> listaLibrosFill){
        adaptadorLibros = new LibroListAdapter(getActivity(),listaLibrosFill);
        ModoVista.aplicar(requireContext(), rejillaLibros, adaptadorLibros);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //sin esto el binding sobrevive a la vista y se filtra
        binding = null;
    }
}
