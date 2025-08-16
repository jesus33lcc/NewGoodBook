package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityLibroDataBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.UI.AccionesLibro;
import com.example.newgoodbooks.UI.DatosLibro;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import com.squareup.picasso.Picasso;

public class LibroData extends AppCompatActivity {
    private ActivityLibroDataBinding binding;
    private View view;
    private Libro libroActual;
    ImageView portadaVista;
    TextView tituloVista;
    TextView autorVista;
    TextView paginasVista;
    TextView fechaVista;
    TextView generosVista;
    TextView descripcionVista;
    MaterialButton btnFav;
    MaterialButton btnCheck;
    private MaterialButton btnAddList;
    private TextView valoracionVista, etiquetaTemas, editorialVista;
    private ChipGroup grupoTemas;
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibroDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        libroActual = (Libro) getIntent().getSerializableExtra("libro");
        initView();
        setDetailsLibro();
    }

    private void initView(){
        portadaVista = binding.imageVPortada;
        tituloVista = binding.textTitulo;
        autorVista = binding.textAutor;
        paginasVista = binding.textNumPag;
        fechaVista = binding.textFechaPub;
        generosVista = binding.textGeneros;
        descripcionVista = binding.textDescripcion;
        valoracionVista = binding.textValoracion;
        etiquetaTemas = binding.etiquetaTemas;
        grupoTemas = binding.grupoTemas;
        editorialVista = binding.textEditorial;
        btnFav=binding.tBtnFavorite;
        btnCheck=binding.tBtnCheck;
        btnAddList=binding.tBtnAddList;

        com.google.android.material.appbar.MaterialToolbar barra = binding.toolbarLibro;
        if (barra != null) {
            barra.setNavigationOnClickListener(v -> finish());
        }
        DatosLibro.pintarAccion(btnAddList, false, R.drawable.ic_addlist, R.drawable.ic_addlist);
    }

    private void setDetailsLibro(){
        if (libroActual != null) {
            Picasso.get().load(libroActual.getLinkImg()).into(portadaVista);
            tituloVista.setText(libroActual.getTitulo());
            autorVista.setText(libroActual.getAutor().isEmpty() ? "" : libroActual.getAutor().get(0));
            paginasVista.setText(getString(R.string.formato_paginas, String.valueOf(libroActual.getNumPag())));
            fechaVista.setText(DatosLibro.soloAnio(libroActual.getFechaPublicacion()));
            generosVista.setText(libroActual.getGeneros().isEmpty() ? "" : libroActual.getGeneros().get(0));
            descripcionVista.setText(libroActual.getDescripcion());
            //datos de Open Library: cada uno se esconde solo si no viene
            DatosLibro.pintarValoracion(valoracionVista, libroActual);
            DatosLibro.pintarTemas(etiquetaTemas, grupoTemas, libroActual);
            DatosLibro.pintarEditorial(editorialVista, libroActual);


            //el estado de los dos toggles lo manda Firestore: si lo marcas aqui,
            //la pantalla Home y el otro dispositivo se enteran solos
            RepositorioUsuario repo = RepositorioUsuario.get();
            repo.getFavoritos().observe(this, libros -> DatosLibro.pintarAccion(btnFav,
                    libros != null && libros.contains(libroActual),
                    R.drawable.ic_favorite_on, R.drawable.ic_favorite_off));
            repo.getLeidos().observe(this, libros -> DatosLibro.pintarAccion(btnCheck,
                    libros != null && libros.contains(libroActual),
                    R.drawable.ic_checkbox_on, R.drawable.ic_checkbox_off));

            btnFav.setOnClickListener(v -> repo.alternarFavorito(libroActual));
            btnCheck.setOnClickListener(v -> repo.alternarLeido(libroActual));
            btnAddList.setOnClickListener(v ->
                    AccionesLibro.mostrarDialogoAnadirALista(LibroData.this, libroActual));
        } else {
            Toast.makeText(this, R.string.libro_no_disponible, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Marca una acción como activa.

    // Devuelve solo el año de una fecha de publicación.
}
