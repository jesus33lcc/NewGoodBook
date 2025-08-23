package com.example.newgoodbooks.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Fragments.AdapterList.LeyendoAdapter;
import com.example.newgoodbooks.Fragments.AdapterList.ListaAdapter;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;
import com.example.newgoodbooks.UI.DialogoNuevaLista;
import com.example.newgoodbooks.databinding.FragmentListasBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;


public class Listas extends Fragment {
    private FragmentListasBinding binding;
    private RecyclerView rejillaListasFijas;
    private RecyclerView rejillaListasPropias;
    private com.google.android.material.button.MaterialButton botonNuevaLista;
    private View vacioListas;
    private ListaAdapter adaptadorFijas;
    private ListaAdapter adaptadorPersonales;
    private LeyendoAdapter adaptadorLeyendo;
    //copia de lo ultimo que ha llegado de Firestore, para resolver la posicion del swipe
    private List<Lista> misListas = new ArrayList<>();
    private final RepositorioUsuario repo = RepositorioUsuario.get();

    public Listas() {
    } // Se requiere de un constructor vacio.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rejillaListasFijas = binding.misListasCheckFav;
        rejillaListasPropias = binding.misListaPersonalizadas;
        botonNuevaLista = binding.btnNewLista;
        vacioListas = binding.vacioListas;

        rejillaListasFijas.setLayoutManager(new LinearLayoutManager(getContext()));
        rejillaListasPropias.setLayoutManager(new LinearLayoutManager(getContext()));

        adaptadorFijas = new ListaAdapter(getActivity(), new ArrayList<>());
        rejillaListasFijas.setAdapter(adaptadorFijas);
        adaptadorPersonales = new ListaAdapter(getActivity(), misListas);
        adaptadorPersonales.setAlPedirOpciones(this::menuDeLista);
        rejillaListasPropias.setAdapter(adaptadorPersonales);

        //Las listas llegan solas desde Firestore: si creas una en el movil, aparece
        //en la tablet sin refrescar nada. Antes habia que reconstruir el adaptador a mano.
        repo.getListas().observe(getViewLifecycleOwner(), listas -> {
            misListas = listas != null ? listas : new ArrayList<>();
            adaptadorPersonales.actualizar(misListas);
            //sin listas propias se explica que hacer, en vez de dejar un hueco en blanco
            vacioListas.setVisibility(misListas.isEmpty() ? View.VISIBLE : View.GONE);
        });
        //Lo que se esta leyendo, arriba del todo. La seccion entera se esconde si no
        //hay nada a medias: una cabecera sola sobre un hueco parece un fallo.
        adaptadorLeyendo = new LeyendoAdapter(getActivity(), new ArrayList<>(), null);
        binding.rejillaLeyendo.setAdapter(adaptadorLeyendo);
        repo.getLecturas().observe(getViewLifecycleOwner(), lecturas -> {
            List<Libro> aMedias = repo.getLibrosLeyendo();
            adaptadorLeyendo.actualizar(aMedias, lecturas);
            int visible = aMedias.isEmpty() ? View.GONE : View.VISIBLE;
            binding.etiquetaLeyendo.setVisibility(visible);
            binding.rejillaLeyendo.setVisibility(visible);
        });

        //las dos listas fijas se derivan de favoritos y leidos
        repo.getFavoritos().observe(getViewLifecycleOwner(),
                libros -> adaptadorFijas.actualizar(repo.getListasImborrables()));
        repo.getLeidos().observe(getViewLifecycleOwner(),
                libros -> adaptadorFijas.actualizar(repo.getListasImborrables()));

        botonNuevaLista.setOnClickListener(v -> mostrarDialogoNuevaLista());

        //Deslizar a la izquierda descubre el boton de borrar. Se mantiene como atajo
        //para quien ya lo conocia; la via visible es el menu de la fila.
        new MySwipeHelper(getContext(), 200, getString(R.string.eliminar),
                R.drawable.ic_delete4ever, Color.parseColor("#FF3C30"),
                this::confirmarBorradoPorPosicion).engancharA(rejillaListasPropias);
    }

    //El dialogo vive en UI/DialogoNuevaLista porque tambien se abre desde "Anadir a
    //lista"; tenerlo aqui obligaba a duplicarlo con su validacion.
    public void mostrarDialogoNuevaLista(){
        DialogoNuevaLista.mostrar(requireContext(), repo::crearLista);
    }

    //Menu de la fila. Es la via visible para borrar; el deslizamiento sigue existiendo
    //para quien ya lo conocia, pero no se descubre solo.
    private void menuDeLista(Lista lista, View ancla) {
        PopupMenu menu = new PopupMenu(requireContext(), ancla);
        menu.inflate(R.menu.menu_lista);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_eliminar_lista) {
                confirmarBorrado(lista);
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void confirmarBorrado(Lista lista) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.lista_confirmar_borrado, lista.getNombre()))
                .setPositiveButton(R.string.eliminar, (d, w) -> borrarConDeshacer(lista))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    //Borra y ofrece deshacer. Sin esto, un borrado por error era definitivo.
    private void borrarConDeshacer(Lista lista) {
        repo.borrarLista(lista);
        if (getView() == null) {
            return;
        }
        Snackbar aviso = Snackbar.make(getView(), R.string.lista_eliminada, Snackbar.LENGTH_LONG)
                .setAction(R.string.lista_deshacer, v -> repo.restaurarLista(lista));
        //El layout no es un CoordinatorLayout, asi que el Snackbar se pega al fondo de
        //la pantalla y se pintaba ENCIMA de la barra de navegacion: "Deshacer" caia
        //sobre la pestania de Listas. Anclarlo a la barra lo sube por encima.
        View barra = requireActivity().findViewById(R.id.nav_view);
        if (barra != null) {
            aviso.setAnchorView(barra);
        }
        aviso.show();
    }

    private void confirmarBorradoPorPosicion(int index){
        if (index < 0 || index >= misListas.size()) {
            return;
        }
        confirmarBorrado(misListas.get(index));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
