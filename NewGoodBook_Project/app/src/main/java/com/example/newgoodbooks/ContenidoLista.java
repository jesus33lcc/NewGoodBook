package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.Helper.MyButtonClickListener;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;

import java.util.ArrayList;
import java.util.List;

public class ContenidoLista extends AppCompatActivity {
    Toolbar toolbarListaSelected;
    RecyclerView recyclerViewContenido;
    Lista listaLibrosSelected;
    LibroListAdapter libroListAdapter;
    List<Libro> listadoLibros;
    public ContenidoLista(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenido_lista);

        toolbarListaSelected = findViewById(R.id.toolbarContentList);
        recyclerViewContenido = findViewById(R.id.listRecyclerContentLista);
        listaLibrosSelected = (Lista) getIntent().getSerializableExtra("item_lista");

        String nomLista = listaLibrosSelected.getNombre();
        listadoLibros = listaLibrosSelected.getLibros();

        toolbarListaSelected.setTitle(nomLista);
        initialize_ListFillBook(listadoLibros);

        if(!esListaImborrable(listaLibrosSelected)){
            MySwipeHelper swipeHelper = new MySwipeHelper(getBaseContext(), recyclerViewContenido, 200) {
                @Override
                public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                    buffer.add(new MyButton(getBaseContext(),
                            "Delete",
                            30,
                            R.drawable.ic_delete4ever,
                            Color.parseColor("#FF3C30"),
                            new MyButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
                                    deleteLista(pos);
                                }
                            }));
                }
            };
        }
    }
    public void initialize_ListFillBook(List<Libro> listaLibrosFill){
        libroListAdapter = new LibroListAdapter(this,listaLibrosFill);
        recyclerViewContenido.setAdapter(libroListAdapter);
    }
    private boolean esListaImborrable(Lista lis) {
        String nombreLista = lis.getNombre();

        if ("Libros Favoritos".equals(nombreLista) || "Libros Leidos".equals(nombreLista)) {
            List<Libro> fav = Datos.DatosComunes.getListasUsuario().getLibrosLike();
            List<Libro> leido = Datos.DatosComunes.getListasUsuario().getLibrosCheck();

            return sonListasIguales(lis.getLibros(), fav) || sonListasIguales(lis.getLibros(), leido);
        }

        return false;
    }
    public boolean sonListasIguales(List<Libro> lista1, List<Libro> lista2) {
        return lista1.equals(lista2);
    }

    private void deleteLista(int index){
        Libro libroDelete = listaLibrosSelected.getLibros().get(index);
        listaLibrosSelected.getLibros().remove(index);
        Datos.DatosComunes.searchByNameListas(listaLibrosSelected.getNombre()).getLibros().remove(libroDelete);
        vaciarRecyclerView_misLibros();
        rellenarRecylerView_misLibros();

        AccesoFicheros accesoFicheros = new AccesoFicheros(getBaseContext());

        accesoFicheros.setListas(Datos.DatosComunes.getListasUsuario());

        Toast.makeText(getBaseContext(), "Libro eliminado", Toast.LENGTH_SHORT).show();
    }
    private void vaciarRecyclerView_misLibros() {
        List<Libro> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillBook(listaLibrosVacia);
    }
    private void rellenarRecylerView_misLibros(){
        List<Libro> rellenoNuevo = listaLibrosSelected.getLibros();
        initialize_ListFillBook(rellenoNuevo);
    }
}