package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityLibroDataBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Lectura;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.UI.AccionesLibro;
import com.example.newgoodbooks.UI.DatosLibro;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import com.squareup.picasso.Picasso;

public class LibroData extends AppCompatActivity {
    private final RepositorioUsuario repo = RepositorioUsuario.get();
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

    //Los tres estados de lectura. "Leido" no esta aqui: es el boton de accion de
    //siempre, y su coleccion es de la que dependen la lista fija y el recomendador.
    //Estos dos son los que antes no se podian expresar.
    //Se llama UNA vez, desde onCreate. Antes se llamaba tambien desde pintarLectura(),
    //que registraba otro observador de lecturas en cada pintado; cada observador
    //disparaba otro pintado y la aplicacion se colgaba con un ANR.
    private void montarEstadoLectura() {
        escucharChips();
        binding.btnActualizarPagina.setOnClickListener(v -> pedirPagina());
        repo.getLecturas().observe(this, mapa -> pintarLectura());
    }

    private void escucharChips() {
        binding.grupoEstadoLectura.setOnCheckedStateChangeListener((grupo, marcados) -> {
            if (libroActual == null) {
                return;
            }
            if (marcados.isEmpty()) {
                repo.olvidarLectura(libroActual);
            } else if (marcados.contains(R.id.estadoLeyendo)) {
                Lectura previa = repo.lecturaDe(libroActual);
                repo.guardarLectura(libroActual, Lectura.LEYENDO,
                        previa != null ? previa.getPagina() : 0);
            } else {
                repo.guardarLectura(libroActual, Lectura.QUIERO, 0);
            }
        });
    }

    private void pintarLectura() {
        if (libroActual == null) {
            return;
        }
        Lectura lectura = repo.lecturaDe(libroActual);
        //sin escuchar mientras se pinta: si no, marcar por codigo dispara el guardado
        //se deja de escuchar mientras se pinta: marcar por codigo dispararia el guardado
        binding.grupoEstadoLectura.setOnCheckedStateChangeListener(null);
        if (lectura == null) {
            binding.grupoEstadoLectura.clearCheck();
        } else if (lectura.estaLeyendo()) {
            binding.estadoLeyendo.setChecked(true);
        } else {
            binding.estadoQuiero.setChecked(true);
        }
        escucharChips();

        boolean leyendo = lectura != null && lectura.estaLeyendo();
        binding.bloqueProgreso.setVisibility(leyendo ? View.VISIBLE : View.GONE);
        if (leyendo) {
            int total = libroActual.getNumPag();
            int porcentaje = lectura.porcentaje(total);
            binding.textoProgreso.setText(getString(R.string.formato_progreso,
                    lectura.getPagina(), total, porcentaje));
            binding.barraProgreso.setProgress(porcentaje);
        }
    }

    //Pedir la pagina. Se acota al total del libro: una pagina mayor daria un
    //porcentaje por encima de cien y una barra desbordada.
    private void pedirPagina() {
        final android.widget.EditText campo = new android.widget.EditText(this);
        campo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        Lectura actual = repo.lecturaDe(libroActual);
        if (actual != null && actual.getPagina() > 0) {
            campo.setText(String.valueOf(actual.getPagina()));
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.actualizar_pagina)
                .setView(campo)
                .setPositiveButton(R.string.confirmar, (d, w) -> {
                    int pagina;
                    try {
                        pagina = Integer.parseInt(String.valueOf(campo.getText()).trim());
                    } catch (NumberFormatException e) {
                        return;
                    }
                    pagina = Math.max(0, Math.min(pagina, libroActual.getNumPag()));
                    repo.guardarLectura(libroActual, Lectura.LEYENDO, pagina);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    //Abre la busqueda ya acotada. Es la forma natural de tirar del hilo desde la ficha.
    private void buscar(String texto, String ambito) {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        Intent ir = new Intent(this, ResultadoSearchView.class);
        ir.putExtra(ResultadoSearchView.EXTRA_CONSULTA, texto);
        ir.putExtra(ResultadoSearchView.EXTRA_AMBITO, ambito);
        startActivity(ir);
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
            //El autor deja de ser texto muerto: al tocarlo se buscan sus libros.
            autorVista.setOnClickListener(v -> buscar(
                    libroActual.getAutor().isEmpty() ? null : libroActual.getAutor().get(0),
                    ClienteFunciones.AMBITO_AUTOR));

            DatosLibro.pintarValoracion(valoracionVista, libroActual);
            DatosLibro.pintarTemas(etiquetaTemas, grupoTemas, libroActual, tema ->
                    buscar(tema, ClienteFunciones.AMBITO_TODO));
            DatosLibro.pintarEditorial(editorialVista, libroActual);
            montarEstadoLectura();
            pintarLectura();


            //el estado de los dos toggles lo manda Firestore: si lo marcas aqui,
            //la pantalla Home y el otro dispositivo se enteran solos
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
