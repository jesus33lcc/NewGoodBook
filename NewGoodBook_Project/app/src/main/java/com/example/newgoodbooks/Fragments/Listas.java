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
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Fragments.AdapterList.ListaAdapter;
import com.example.newgoodbooks.Helper.MyButtonClickListener;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;
import com.example.newgoodbooks.databinding.FragmentListasBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;


public class Listas extends Fragment {
    private FragmentListasBinding binding;
    private RecyclerView listasRecyclerView;
    private RecyclerView misListasRecyclerView;
    private com.google.android.material.button.MaterialButton btn_newAddLista;
    private View vacioListas;
    private ListaAdapter adaptadorFijas;
    private ListaAdapter adaptadorPersonales;
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

        listasRecyclerView = binding.misListasCheckFav;
        misListasRecyclerView = binding.misListaPersonalizadas;
        btn_newAddLista = binding.btnNewLista;
        vacioListas = binding.vacioListas;

        listasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        misListasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adaptadorFijas = new ListaAdapter(getActivity(), new ArrayList<>());
        listasRecyclerView.setAdapter(adaptadorFijas);
        adaptadorPersonales = new ListaAdapter(getActivity(), misListas);
        adaptadorPersonales.setAlPedirOpciones(this::menuDeLista);
        misListasRecyclerView.setAdapter(adaptadorPersonales);

        //Las listas llegan solas desde Firestore: si creas una en el movil, aparece
        //en la tablet sin refrescar nada. Antes habia que reconstruir el adaptador a mano.
        repo.getListas().observe(getViewLifecycleOwner(), listas -> {
            misListas = listas != null ? listas : new ArrayList<>();
            adaptadorPersonales.actualizar(misListas);
            //sin listas propias se explica que hacer, en vez de dejar un hueco en blanco
            vacioListas.setVisibility(misListas.isEmpty() ? View.VISIBLE : View.GONE);
        });
        //las dos listas fijas se derivan de favoritos y leidos
        repo.getFavoritos().observe(getViewLifecycleOwner(),
                libros -> adaptadorFijas.actualizar(repo.getListasImborrables()));
        repo.getLeidos().observe(getViewLifecycleOwner(),
                libros -> adaptadorFijas.actualizar(repo.getListasImborrables()));

        btn_newAddLista.setOnClickListener(v -> showInputTextDialog_newList());

        //swipe a la izquierda para borrar, solo en las listas personalizadas
        new MySwipeHelper(getContext(), misListasRecyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(getContext(),
                        getString(R.string.eliminar),
                        30,
                        R.drawable.ic_delete4ever,
                        Color.parseColor("#FF3C30"),
                        new MyButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                showTextDialog_ConfirmDelete(pos);
                            }
                        }));
            }
        };
    }

    //Crear lista. El campo es de Material y el error sale DEBAJO del propio campo, no
    //como aviso flotante: antes el dialogo se cerraba al fallar y habia que empezar de
    //cero para leer un mensaje que aparecia en la otra punta de la pantalla.
    public void showInputTextDialog_newList(){
        View contenido = getLayoutInflater().inflate(R.layout.dialogo_nueva_lista, null);
        TextInputLayout capa = contenido.findViewById(R.id.capaNombreLista);
        TextInputEditText campo = contenido.findViewById(R.id.campoNombreLista);

        androidx.appcompat.app.AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.nueva_lista)
                .setView(contenido)
                .setPositiveButton(R.string.crear, null)
                .setNegativeButton(R.string.cancelar, null)
                .create();

        //El boton se engancha DESPUES de mostrar el dialogo: asi se puede validar sin
        //que se cierre, que es lo que hace que el error se pueda leer y corregir.
        dialogo.setOnShowListener(d -> dialogo.getButton(
                androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String nombre = String.valueOf(campo.getText()).trim();
                    String error = validarNombre(nombre);
                    if (error != null) {
                        capa.setError(error);
                        return;
                    }
                    capa.setError(null);
                    repo.crearLista(nombre);
                    dialogo.dismiss();
                }));
        dialogo.show();
    }

    //Devuelve el motivo por el que el nombre no vale, o null si esta bien.
    private String validarNombre(String nombre) {
        if (nombre.isEmpty()) {
            return getString(R.string.lista_sin_nombre);
        }
        if (nombre.length() > 40) {
            return getString(R.string.lista_nombre_largo);
        }
        //tambien contra los nombres traducidos: con la app en ingles se podia
        //crear una lista llamada igual que una de las fijas
        if (nombre.equalsIgnoreCase(Lista.NOMBRE_FAVORITOS)
                || nombre.equalsIgnoreCase(Lista.NOMBRE_LEIDOS)
                || nombre.equalsIgnoreCase(getString(R.string.lista_favoritos))
                || nombre.equalsIgnoreCase(getString(R.string.lista_leidos))) {
            return getString(R.string.lista_nombre_reservado);
        }
        for (String existente : repo.getNombresListasPersonales()) {
            if (existente.equalsIgnoreCase(nombre)) {
                return getString(R.string.lista_nombre_existente);
            }
        }
        return null;
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

    private void showTextDialog_ConfirmDelete(int index){
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
