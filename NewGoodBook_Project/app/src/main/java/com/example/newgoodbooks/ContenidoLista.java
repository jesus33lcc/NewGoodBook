package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityContenidoListaBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.UI.ModoVista;
import com.example.newgoodbooks.Helper.MyButtonClickListener;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.UI.EstadoVacio;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ContenidoLista extends AppCompatActivity {
    private ActivityContenidoListaBinding binding;
    //Se recibe el ID de la lista, no el objeto. Antes llegaba una copia Serializable
    //y cualquier borrado habia que hacerlo dos veces para que se persistiera.
    public static final String EXTRA_LISTA_ID = "lista_id";

    private Toolbar toolbarListaSelected;
    private RecyclerView recyclerViewContenido;
    private LibroListAdapter libroListAdapter;
    private String listaId;
    private List<Libro> librosActuales = new ArrayList<>();
    private final RepositorioUsuario repo = RepositorioUsuario.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContenidoListaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbarListaSelected = binding.toolbarContentList;
        //Conmutador de lista o cuadricula, comun a las tres pantallas de libros.
        toolbarListaSelected.inflateMenu(R.menu.menu_vista);
        ModoVista.pintarIcono(this, toolbarListaSelected.getMenu());
        toolbarListaSelected.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_vista) {
                ModoVista.alternar(this);
                ModoVista.pintarIcono(this, toolbarListaSelected.getMenu());
                ModoVista.aplicar(this, recyclerViewContenido, libroListAdapter);
                return true;
            }
            return false;
        });

        recyclerViewContenido = binding.listRecyclerContentLista;


        listaId = getIntent().getStringExtra(EXTRA_LISTA_ID);
        if (listaId == null) {
            Toast.makeText(this, getString(R.string.lista_no_disponible), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        libroListAdapter = new LibroListAdapter(this, librosActuales);
        ModoVista.aplicar(this, recyclerViewContenido, libroListAdapter);

        observarLista();

        //las listas fijas (favoritos y leidos) tambien admiten quitar libros:
        //ahi el swipe simplemente desmarca
        new MySwipeHelper(this, recyclerViewContenido, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(ContenidoLista.this,
                        getString(R.string.eliminar),
                        30,
                        R.drawable.ic_delete4ever,
                        Color.parseColor("#FF3C30"),
                        new MyButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                quitarLibro(pos);
                            }
                        }));
            }
        };
    }

    //La lista se pinta desde Firestore: al quitar un libro no hay que refrescar a mano,
    //llega solo por el listener (y tambien si lo quitas desde el otro dispositivo).
    private void observarLista() {
        if (Lista.ID_FAVORITOS.equals(listaId)) {
            repo.getFavoritos().observe(this, this::pintar);
        } else if (Lista.ID_LEIDOS.equals(listaId)) {
            repo.getLeidos().observe(this, this::pintar);
        } else {
            repo.getListas().observe(this, listas -> {
                Lista lista = repo.getListaPorId(listaId);
                if (lista == null) {
                    //la lista se ha borrado (posiblemente desde el otro dispositivo)
                    finish();
                    return;
                }
                toolbarListaSelected.setTitle(lista.getNombreVisible(this));
                pintar(lista.getLibros());
            });
            return;
        }
        Lista fija = repo.getListaPorId(listaId);
        toolbarListaSelected.setTitle(fija != null ? fija.getNombreVisible(this) : getString(R.string.title_listas));
    }

    private void pintar(List<Libro> libros) {
        librosActuales = libros != null ? libros : new ArrayList<>();
        libroListAdapter.actualizar(librosActuales);
        EstadoVacio.mostrar(binding.estadoVacio.getRoot(), librosActuales.isEmpty(),
                R.drawable.ic_listas, R.string.vacio_lista_titulo, R.string.vacio_lista_detalle);
    }

    private void quitarLibro(int index) {
        if (index < 0 || index >= librosActuales.size()) {
            return;
        }
        repo.quitarLibroDeLista(listaId, librosActuales.get(index));
        Toast.makeText(this, getString(R.string.libro_eliminado), Toast.LENGTH_SHORT).show();
    }
}
