package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.UI.EstadoVacio;
import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.UI.ModoVista;
import com.example.newgoodbooks.Modelos.Libro;

public class ResultadoSearchView extends AppCompatActivity {
    Toolbar toolbarResultados;
    RecyclerView recyclerViewResultados;
    List<Libro> listaLibrosResultados;
    LibroListAdapter libroListAdapter;
    private String titulo_a_buscar;
    public ResultadoSearchView() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_search_view);

        recyclerViewResultados = findViewById(R.id.listRecyclerResultadosLibros);


        titulo_a_buscar = getIntent().getStringExtra("titulo_a_buscar");
        com.google.android.material.appbar.MaterialToolbar barra = findViewById(R.id.toolbarResultados);
        //Conmutador de lista o cuadricula, comun a las tres pantallas de libros.
        barra.inflateMenu(R.menu.menu_vista);
        ModoVista.pintarIcono(this, barra.getMenu());
        barra.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_vista) {
                ModoVista.alternar(this);
                ModoVista.pintarIcono(this, barra.getMenu());
                ModoVista.aplicar(this, recyclerViewResultados, libroListAdapter);
                return true;
            }
            return false;
        });

        if (barra != null && titulo_a_buscar != null) {
            barra.setTitle(getString(R.string.titulo_resultados_de, titulo_a_buscar));
        }
        List<Libro> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillBook(listaLibrosVacia);
        buscarTitulo();
    }

    public void initialize_ListFillBook(List<Libro> listaLibrosFill){
        libroListAdapter = new LibroListAdapter(this,listaLibrosFill);
        ModoVista.aplicar(this, recyclerViewResultados, libroListAdapter);
    }

    private void buscarTitulo(){
        Executor executor= Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listaLibrosResultados=new ArrayList<>(ClienteFunciones.buscarTitulo(titulo_a_buscar));
                fillRecycleList();
            }
        });
    }

    private void fillRecycleList(){
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                initialize_ListFillBook(listaLibrosResultados);
                EstadoVacio.mostrar(findViewById(R.id.estadoVacio),
                        listaLibrosResultados.isEmpty(), R.drawable.ic_explorar,
                        R.string.vacio_resultados_titulo, R.string.vacio_resultados_detalle);
            }
        });
    }
}