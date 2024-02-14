package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;

import java.util.List;

public class ContenidoLista extends AppCompatActivity {
    Toolbar toolbarListaSelected;
    RecyclerView recyclerViewContenido;
    Lista listaLibrosSelected;
    LibroListAdapter libroListAdapter;
    public ContenidoLista(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenido_lista);

        toolbarListaSelected = findViewById(R.id.toolbarContentList);
        recyclerViewContenido = findViewById(R.id.listRecyclerContentLista);
        listaLibrosSelected = (Lista) getIntent().getSerializableExtra("item_lista");

        String nomLista = listaLibrosSelected.getNombre();
        List<Libro> listadoLibros = listaLibrosSelected.getLibros();

        toolbarListaSelected.setTitle(nomLista);
        initialize_ListFillBook(listadoLibros);
    }
    public void initialize_ListFillBook(List<Libro> listaLibrosFill){
        libroListAdapter = new LibroListAdapter(this,listaLibrosFill);
        recyclerViewContenido.setAdapter(libroListAdapter);
    }
}