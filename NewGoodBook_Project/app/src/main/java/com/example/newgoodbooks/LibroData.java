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
    private Libro bookSelected;
    ImageView portadaIMG;
    TextView tituloTXT;
    TextView autorTXT;
    TextView numPagTXT;
    TextView fechaPubTXT;
    TextView generosTXT;
    TextView descripcionTXT;
    MaterialButton btnFav;
    MaterialButton btnCheck;
    private MaterialButton btnAddList;
    private TextView valoracionTXT, etiquetaTemas, editorialTXT;
    private ChipGroup grupoTemas;
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibroDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookSelected = (Libro) getIntent().getSerializableExtra("libro");
        initView();
        setDetailsLibro();
    }

    private void initView(){
        portadaIMG = binding.imageVPortada;
        tituloTXT = binding.textTitulo;
        autorTXT = binding.textAutor;
        numPagTXT = binding.textNumPag;
        fechaPubTXT = binding.textFechaPub;
        generosTXT = binding.textGeneros;
        descripcionTXT = binding.textDescripcion;
        valoracionTXT = binding.textValoracion;
        etiquetaTemas = binding.etiquetaTemas;
        grupoTemas = binding.grupoTemas;
        editorialTXT = binding.textEditorial;
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
        if (bookSelected != null) {
            Picasso.get().load(bookSelected.getLinkImg()).into(portadaIMG);
            tituloTXT.setText(bookSelected.getTitulo());
            autorTXT.setText(bookSelected.getAutor().isEmpty() ? "" : bookSelected.getAutor().get(0));
            numPagTXT.setText(getString(R.string.formato_paginas, String.valueOf(bookSelected.getNumPag())));
            fechaPubTXT.setText(DatosLibro.soloAnio(bookSelected.getFechaPublicacion()));
            generosTXT.setText(bookSelected.getGeneros().isEmpty() ? "" : bookSelected.getGeneros().get(0));
            descripcionTXT.setText(bookSelected.getDescripcion());
            //datos de Open Library: cada uno se esconde solo si no viene
            DatosLibro.pintarValoracion(valoracionTXT, bookSelected);
            DatosLibro.pintarTemas(etiquetaTemas, grupoTemas, bookSelected);
            DatosLibro.pintarEditorial(editorialTXT, bookSelected);


            //el estado de los dos toggles lo manda Firestore: si lo marcas aqui,
            //la pantalla Home y el otro dispositivo se enteran solos
            RepositorioUsuario repo = RepositorioUsuario.get();
            repo.getFavoritos().observe(this, libros -> DatosLibro.pintarAccion(btnFav,
                    libros != null && libros.contains(bookSelected),
                    R.drawable.ic_favorite_on, R.drawable.ic_favorite_off));
            repo.getLeidos().observe(this, libros -> DatosLibro.pintarAccion(btnCheck,
                    libros != null && libros.contains(bookSelected),
                    R.drawable.ic_checkbox_on, R.drawable.ic_checkbox_off));

            btnFav.setOnClickListener(v -> repo.alternarFavorito(bookSelected));
            btnCheck.setOnClickListener(v -> repo.alternarLeido(bookSelected));
            btnAddList.setOnClickListener(v ->
                    AccionesLibro.mostrarDialogoAnadirALista(LibroData.this, bookSelected));
        } else {
            Toast.makeText(this, R.string.libro_no_disponible, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Marca una acción como activa.

    // Devuelve solo el año de una fecha de publicación.
}
