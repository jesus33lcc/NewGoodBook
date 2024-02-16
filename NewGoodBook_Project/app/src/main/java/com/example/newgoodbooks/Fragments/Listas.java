package com.example.newgoodbooks.Fragments;

import android.app.AlertDialog;
//import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newgoodbooks.Fragments.AdapterList.ListaListAdapter;
import com.example.newgoodbooks.Fragments.AdapterList.ListaImborrableAdapter;
import com.example.newgoodbooks.Helper.MyButtonClickListener;
import com.example.newgoodbooks.Helper.MySwipeHelper;
import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;

import java.util.ArrayList;
import java.util.List;


public class Listas extends Fragment {
    private View view;
    private RecyclerView listasRecyclerView;
    private RecyclerView misListasRecyclerView;
    private ImageButton btn_newAddLista;
    ListaListAdapter listaListAdapter;
    ListaImborrableAdapter listaImborrableAdapter;
    List<Lista> milistadoListasList;
    List<Lista> listadoListasList;

    public Listas() {
    } // Se requiere de un constructor vacio.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_listas, container, false);

        // Sobre el RecyclerView
        listasRecyclerView = view.findViewById(R.id.misListasCheckFav);
        misListasRecyclerView = view.findViewById(R.id.misListaPersonalizadas);
        btn_newAddLista = view.findViewById(R.id.btn_newLista);

        listasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        misListasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Insertar Libros en lista.
        vaciarRecyclerView_misListas();

        // Insertar listas en el RecyclerList
        rellenarRecylerView_listasImborrables();
        rellenarRecylerView_misListas();


        btn_newAddLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputTextDialog_newList();
            }
        });

        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(), misListasRecyclerView, 200) {
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
                                Toast.makeText(getContext(), "Lista eliminada", Toast.LENGTH_SHORT).show();
                            }
                        }));
            }
        };
        return view;
    }

    private void vaciarRecyclerView_listasImborrables(){
        List<Lista> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillInmborrable(listaLibrosVacia);
    }
    private void vaciarRecyclerView_misListas() {
        List<Lista> listaLibrosVacia = new ArrayList<>();
        initialize_ListFillList(listaLibrosVacia);
    }
    private void rellenarRecylerView_listasImborrables(){
        listadoListasList = Datos.DatosComunes.getListasImborrables();
        initialize_ListFillInmborrable(listadoListasList);
    }

    private void rellenarRecylerView_misListas(){
        milistadoListasList = Datos.DatosComunes.getListasUsuario().getListas();
        initialize_ListFillList(milistadoListasList);
    }

    public void initialize_ListFillInmborrable(List<Lista> listaListasFill) {
        listaImborrableAdapter = new ListaImborrableAdapter(getActivity(),listaListasFill);
        listasRecyclerView.setAdapter(listaImborrableAdapter);
    }

    public void initialize_ListFillList(List<Lista> listaListasFill) {
        listaListAdapter = new ListaListAdapter(getActivity(),listaListasFill);
        misListasRecyclerView.setAdapter(listaListAdapter);
    }

    public void showInputTextDialog_newList(){
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(getContext());
        alertDialog_Builder.setTitle("Nueva Lista");

        EditText inputText = new EditText(getContext());
        alertDialog_Builder.setView(inputText);

        alertDialog_Builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre_newList = inputText.getText().toString();
                crearNuevaLista(nombre_newList);
            }
        });
        alertDialog_Builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog_Builder.show();
    }

    private void crearNuevaLista(String nomLista){
        List<Libro> listadoLibros = new ArrayList<>();
        Lista nuevaLista = new Lista(nomLista,listadoLibros);
        Datos.DatosComunes.getListasUsuario().getListas().add(nuevaLista);

        AccesoFicheros accesoFicheros = new AccesoFicheros(getContext());
        accesoFicheros.setListas(Datos.DatosComunes.getListasUsuario());

        vaciarRecyclerView_misListas();
        rellenarRecylerView_misListas();

        vaciarRecyclerView_listasImborrables();
        rellenarRecylerView_listasImborrables();

        Toast.makeText(getActivity(), "Lista '" + nomLista + "' creada", Toast.LENGTH_SHORT).show();
    }
}