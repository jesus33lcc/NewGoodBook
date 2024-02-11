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

import com.example.newgoodbooks.Cliente.ClienteBooks;
import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
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
        recyclerViewResultados.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        titulo_a_buscar = getIntent().getStringExtra("titulo_a_buscar");
        List<Libro> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillBook(listaLibrosVacia);
        buscarTitulo();
    }

    public void initialize_ListFillBook(List<Libro> listaLibrosFill){
        libroListAdapter = new LibroListAdapter(this,listaLibrosFill);
        recyclerViewResultados.setAdapter(libroListAdapter);
    }

    private void buscarTitulo(){
        Executor executor= Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listaLibrosResultados=new ArrayList<>(ClienteBooks.buscarTitulo(titulo_a_buscar));
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
            }
        });
    }
}