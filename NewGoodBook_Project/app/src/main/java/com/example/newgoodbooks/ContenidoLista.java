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

    private Toolbar barraLista;
    private RecyclerView rejillaContenido;
    private LibroListAdapter adaptadorLibros;
    private MySwipeHelper deslizamiento;
    private String listaId;
    private List<Libro> librosActuales = new ArrayList<>();
    private final RepositorioUsuario repo = RepositorioUsuario.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContenidoListaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        barraLista = binding.toolbarContentList;
        //Conmutador de lista o cuadricula, comun a las tres pantallas de libros.
        barraLista.inflateMenu(R.menu.menu_vista);
        ModoVista.pintarIcono(this, barraLista.getMenu());
        barraLista.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_vista) {
                ModoVista.alternar(this);
                ModoVista.pintarIcono(this, barraLista.getMenu());
                ModoVista.aplicar(this, rejillaContenido, adaptadorLibros);
                //en cuadricula el deslizamiento se suelta; al volver a lista, vuelve
                montarDeslizamiento();
                return true;
            }
            return false;
        });

        rejillaContenido = binding.listRecyclerContentLista;


        listaId = getIntent().getStringExtra(EXTRA_LISTA_ID);
        if (listaId == null) {
            Toast.makeText(this, getString(R.string.lista_no_disponible), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adaptadorLibros = new LibroListAdapter(this, librosActuales);
        ModoVista.aplicar(this, rejillaContenido, adaptadorLibros);

        observarLista();

        //mantener pulsado sirve en los dos modos y es la unica via en cuadricula
        adaptadorLibros.setAlMantenerPulsado(this::confirmarQuitar);
        montarDeslizamiento();
    }

    //Deslizar quita el libro de la lista; en las fijas simplemente lo desmarca.
    //En los descartados hace lo contrario que en el resto: devuelve el libro a las
    //recomendaciones. Por eso ahi ni se llama "Eliminar" ni va en rojo.
    //
    //Solo se engancha en modo lista. El boton se dibuja al ancho del elemento, asi que
    //en cuadricula aparecia encima de las celdas de al lado y llegaban a verse tres
    //botones rojos sueltos entre las portadas.
    private void montarDeslizamiento() {
        if (deslizamiento != null) {
            deslizamiento.soltar(rejillaContenido);
            deslizamiento = null;
        }
        if (ModoVista.esCuadricula(this)) {
            return;
        }
        boolean esDescarte = Lista.ID_DESCARTADOS.equals(listaId);
        deslizamiento = new MySwipeHelper(this, 200,
                getString(esDescarte ? R.string.recuperar : R.string.eliminar),
                esDescarte ? R.drawable.ic_recuperar : R.drawable.ic_delete4ever,
                Color.parseColor(esDescarte ? "#E89D10" : "#FF3C30"),
                this::quitarLibro);
        deslizamiento.engancharA(rejillaContenido);
    }

    //Mantener pulsado pregunta antes de hacer nada: a diferencia del deslizamiento, es
    //un gesto que se dispara sin querer al desplazar la rejilla.
    private void confirmarQuitar(int index) {
        if (index < 0 || index >= librosActuales.size()) {
            return;
        }
        boolean esDescarte = Lista.ID_DESCARTADOS.equals(listaId);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(librosActuales.get(index).getTitulo())
                .setPositiveButton(esDescarte ? R.string.recuperar : R.string.eliminar,
                        (d, w) -> quitarLibro(index))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    //La lista se pinta desde Firestore: al quitar un libro no hay que refrescar a mano,
    //llega solo por el listener (y tambien si lo quitas desde el otro dispositivo).
    private void observarLista() {
        if (Lista.ID_FAVORITOS.equals(listaId)) {
            repo.getFavoritos().observe(this, this::pintar);
        } else if (Lista.ID_DESCARTADOS.equals(listaId)) {
            //aqui deslizar no borra nada: devuelve el libro al recomendador
            repo.getDescartados().observe(this, this::pintar);
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
                barraLista.setTitle(lista.getNombreVisible(this));
                pintar(lista.getLibros());
            });
            return;
        }
        Lista fija = repo.getListaPorId(listaId);
        barraLista.setTitle(fija != null ? fija.getNombreVisible(this) : getString(R.string.title_listas));
    }

    private void pintar(List<Libro> libros) {
        librosActuales = libros != null ? libros : new ArrayList<>();
        adaptadorLibros.actualizar(librosActuales);
        EstadoVacio.mostrar(binding.estadoVacio.getRoot(), librosActuales.isEmpty(),
                R.drawable.ic_listas, R.string.vacio_lista_titulo, R.string.vacio_lista_detalle);
    }

    private void quitarLibro(int index) {
        if (index < 0 || index >= librosActuales.size()) {
            return;
        }
        repo.quitarLibroDeLista(listaId, librosActuales.get(index));
        Toast.makeText(this, getString(Lista.ID_DESCARTADOS.equals(listaId)
                ? R.string.descarte_deshecho : R.string.libro_eliminado),
                Toast.LENGTH_SHORT).show();
    }
}
