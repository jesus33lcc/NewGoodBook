package com.example.newgoodbooks;

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
import com.google.android.material.button.MaterialButton;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import com.squareup.picasso.Picasso;

public class LibroData extends AppCompatActivity {
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
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro_data);

        bookSelected = (Libro) getIntent().getSerializableExtra("libro");
        initView();
        setDetailsLibro();
    }

    private void initView(){
        portadaIMG = findViewById(R.id.imageVPortada);
        tituloTXT = findViewById(R.id.textTitulo);
        autorTXT = findViewById(R.id.textAutor);
        numPagTXT = findViewById(R.id.textNumPag);
        fechaPubTXT = findViewById(R.id.textFechaPub);
        generosTXT = findViewById(R.id.textGeneros);
        descripcionTXT = findViewById(R.id.textDescripcion);
        btnFav=findViewById(R.id.tBtnFavorite);
        btnCheck=findViewById(R.id.tBtnCheck);
        btnAddList=findViewById(R.id.tBtnAddList);

        com.google.android.material.appbar.MaterialToolbar barra = findViewById(R.id.toolbarLibro);
        if (barra != null) {
            barra.setNavigationOnClickListener(v -> finish());
        }
        pintarAccion(btnAddList, false, R.drawable.ic_addlist, R.drawable.ic_addlist);
    }

    private void setDetailsLibro(){
        if (bookSelected != null) {
            Picasso.get().load(bookSelected.getLinkImg()).into(portadaIMG);
            tituloTXT.setText(bookSelected.getTitulo());
            autorTXT.setText(bookSelected.getAutor().isEmpty() ? "" : bookSelected.getAutor().get(0));
            numPagTXT.setText(getString(R.string.formato_paginas, String.valueOf(bookSelected.getNumPag())));
            fechaPubTXT.setText(soloAnio(bookSelected.getFechaPublicacion()));
            generosTXT.setText(bookSelected.getGeneros().isEmpty() ? "" : bookSelected.getGeneros().get(0));
            descripcionTXT.setText(bookSelected.getDescripcion());


            //el estado de los dos toggles lo manda Firestore: si lo marcas aqui,
            //la pantalla Home y el otro dispositivo se enteran solos
            RepositorioUsuario repo = RepositorioUsuario.get();
            repo.getFavoritos().observe(this, libros -> pintarAccion(btnFav,
                    libros != null && libros.contains(bookSelected),
                    R.drawable.ic_favorite_on, R.drawable.ic_favorite_off));
            repo.getLeidos().observe(this, libros -> pintarAccion(btnCheck,
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
    private void pintarAccion(MaterialButton boton, boolean activo, int iconoOn, int iconoOff) {
        int acento = ContextCompat.getColor(this, R.color.md_tertiary);
        int apagado = com.google.android.material.color.MaterialColors.getColor(
                boton, com.google.android.material.R.attr.colorOnSurfaceVariant);
        boton.setIconResource(activo ? iconoOn : iconoOff);
        boton.setIconTint(ColorStateList.valueOf(activo ? acento : apagado));
        boton.setTextColor(activo ? acento : apagado);
        boton.setStrokeColor(ColorStateList.valueOf(activo ? acento
                : com.google.android.material.color.MaterialColors.getColor(
                        boton, com.google.android.material.R.attr.colorOutline)));
    }

    // Devuelve solo el año de una fecha de publicación.
    private static String soloAnio(String fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.length() >= 4 ? fecha.substring(0, 4) : fecha;
    }
}
