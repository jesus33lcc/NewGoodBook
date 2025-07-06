package com.example.newgoodbooks.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Fragments.AdapterList.ListaListAdapter;
import com.example.newgoodbooks.Fragments.AdapterList.ListaImborrableAdapter;
import com.example.newgoodbooks.Helper.MyButtonClickListener;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;

import java.util.ArrayList;
import java.util.List;


public class Listas extends Fragment {
    private RecyclerView listasRecyclerView;
    private RecyclerView misListasRecyclerView;
    private ImageButton btn_newAddLista;
    private ListaListAdapter listaListAdapter;
    private ListaImborrableAdapter listaImborrableAdapter;
    //copia de lo ultimo que ha llegado de Firestore, para resolver la posicion del swipe
    private List<Lista> misListas = new ArrayList<>();
    private final RepositorioUsuario repo = RepositorioUsuario.get();

    public Listas() {
    } // Se requiere de un constructor vacio.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listasRecyclerView = view.findViewById(R.id.misListasCheckFav);
        misListasRecyclerView = view.findViewById(R.id.misListaPersonalizadas);
        btn_newAddLista = view.findViewById(R.id.btn_newLista);

        listasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        misListasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaImborrableAdapter = new ListaImborrableAdapter(getActivity(), new ArrayList<>());
        listasRecyclerView.setAdapter(listaImborrableAdapter);
        listaListAdapter = new ListaListAdapter(getActivity(), misListas);
        misListasRecyclerView.setAdapter(listaListAdapter);

        //Las listas llegan solas desde Firestore: si creas una en el movil, aparece
        //en la tablet sin refrescar nada. Antes habia que reconstruir el adaptador a mano.
        repo.getListas().observe(getViewLifecycleOwner(), listas -> {
            misListas = listas != null ? listas : new ArrayList<>();
            listaListAdapter.actualizar(misListas);
        });
        //las dos listas fijas se derivan de favoritos y leidos
        repo.getFavoritos().observe(getViewLifecycleOwner(),
                libros -> listaImborrableAdapter.actualizar(repo.getListasImborrables()));
        repo.getLeidos().observe(getViewLifecycleOwner(),
                libros -> listaImborrableAdapter.actualizar(repo.getListasImborrables()));

        btn_newAddLista.setOnClickListener(v -> showInputTextDialog_newList());

        //swipe a la izquierda para borrar, solo en las listas personalizadas
        new MySwipeHelper(getContext(), misListasRecyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(getContext(),
                        "Delete",
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

    public void showInputTextDialog_newList(){
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(getContext());
        alertDialog_Builder.setTitle("Nueva Lista");

        EditText inputText = new EditText(getContext());
        alertDialog_Builder.setView(inputText);

        alertDialog_Builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre_newList = inputText.getText().toString().trim();
                if(nombre_newList.isEmpty()){
                    Toast.makeText(getActivity(), "Ponle un nombre a la lista", Toast.LENGTH_SHORT).show();
                    return;
                }
                //los nombres de las listas fijas estan reservados
                if(nombre_newList.equalsIgnoreCase("Libros Favoritos")
                        || nombre_newList.equalsIgnoreCase("Libros Leidos")){
                    Toast.makeText(getActivity(), "Ese nombre esta reservado", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(String existente : repo.getNombresListasPersonales()){
                    if(existente.equalsIgnoreCase(nombre_newList)){
                        Toast.makeText(getActivity(), "Nombre de Lista existente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                repo.crearLista(nombre_newList);
                Toast.makeText(getActivity(), "Lista '" + nombre_newList + "' creada", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog_Builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        alertDialog_Builder.show();
    }

    private void showTextDialog_ConfirmDelete(int index){
        if (index < 0 || index >= misListas.size()) {
            return;
        }
        Lista aBorrar = misListas.get(index);
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(getContext());
        alertDialog_Builder.setTitle("¿Seguro que quieres eliminar '" + aBorrar.getNombre() + "'?");
        alertDialog_Builder.setPositiveButton("Confirmar", (dialog, which) -> {
            repo.borrarLista(aBorrar);
            Toast.makeText(getActivity(), "Lista eliminada", Toast.LENGTH_SHORT).show();
        });
        alertDialog_Builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            //devuelve la fila a su sitio tras cancelar el swipe
            listaListAdapter.notifyItemChanged(index);
        });
        alertDialog_Builder.show();
    }
}
