package com.example.newgoodbooks;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.Fragments.BusquedaViewModel;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.UI.EstadoVacio;
import com.example.newgoodbooks.UI.ModoVista;
import com.example.newgoodbooks.databinding.ActivityResultadoSearchViewBinding;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

//Resultados de buscar por titulo. La busqueda vive en el ViewModel: antes salia de
//onCreate y se repetia entera en cada giro de pantalla.
public class ResultadoSearchView extends AppCompatActivity {
    //clave del extra que manda Explorar
    public static final String EXTRA_CONSULTA = "titulo_a_buscar";

    private ActivityResultadoSearchViewBinding binding;
    private LibroListAdapter adaptador;
    private BusquedaViewModel modelo;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        binding = ActivityResultadoSearchViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        modelo = new ViewModelProvider(this).get(BusquedaViewModel.class);
        String consulta = getIntent().getStringExtra(EXTRA_CONSULTA);

        MaterialToolbar barra = binding.toolbarResultados;
        if (consulta != null) {
            barra.setTitle(getString(R.string.titulo_resultados_de, consulta));
        }
        //Conmutador de lista o cuadricula, comun a las tres pantallas de libros.
        barra.inflateMenu(R.menu.menu_vista);
        ModoVista.pintarIcono(this, barra.getMenu());
        barra.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_vista) {
                ModoVista.alternar(this);
                ModoVista.pintarIcono(this, barra.getMenu());
                ModoVista.aplicar(this, binding.listRecyclerResultadosLibros, adaptador);
                return true;
            }
            return false;
        });

        mostrarLibros(new ArrayList<Libro>());

        modelo.getBuscando().observe(this, this::pintarEstado);
        modelo.getResultados().observe(this, libros -> {
            mostrarLibros(libros != null ? libros : new ArrayList<Libro>());
            pintarEstado(Boolean.FALSE);
        });
        modelo.buscar(consulta);
    }

    private void mostrarLibros(List<Libro> libros) {
        adaptador = new LibroListAdapter(this, libros);
        ModoVista.aplicar(this, binding.listRecyclerResultadosLibros, adaptador);
    }

    //Mientras busca no se anuncia "Sin resultados": eso era mentira y ademas alarmaba.
    private void pintarEstado(Boolean buscando) {
        boolean cargando = Boolean.TRUE.equals(buscando);
        binding.cargandoResultados.setVisibility(cargando ? View.VISIBLE : View.GONE);
        List<Libro> libros = modelo.getResultados().getValue();
        boolean vacio = !cargando && (libros == null || libros.isEmpty());
        EstadoVacio.mostrar(binding.estadoVacio.getRoot(), vacio, R.drawable.ic_explorar,
                R.string.vacio_resultados_titulo, R.string.vacio_resultados_detalle);
    }
}
