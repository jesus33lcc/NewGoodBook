package com.example.newgoodbooks.Fragments.HomeIU;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.newgoodbooks.R;
import com.google.android.material.button.MaterialButton;
import com.example.newgoodbooks.UI.AccionesLibro;
import com.example.newgoodbooks.UI.DatosLibro;
import com.example.newgoodbooks.databinding.FragmentHomeBinding;
import android.content.Intent;
import androidx.core.app.ActivityOptionsCompat;
import com.example.newgoodbooks.LibroData;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.UI.ColorDeLibro;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    //tiene que coincidir con el android:transitionName de las dos portadas
    public static final String TRANSICION_PORTADA = "portada";

    private FragmentHomeBinding binding;
    private HomeViewModel mViewModel;
    private TextView titulo;
    private TextView autor;
    private TextView numPag;
    private TextView fecha;
    private TextView genero;
    private TextView descripcion;
    private MaterialButton botonSig;
    private ImageView portada;
    private MaterialButton btnFav;
    private MaterialButton btnCheck;
    private MaterialButton btnAddList;
    private TextView etiquetaSinopsis;
    private TextView valoracion;
    private MaterialButton btnNoInteresa;

    public HomeFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //scope de Activity: asi la cola de recomendaciones sobrevive al cambio de pestania
        //y volver a Home no dispara otra llamada al servidor
        mViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        titulo = binding.textTitulo;
        autor = binding.textAutor;
        numPag = binding.textNumPag;
        fecha = binding.textFechaPub;
        genero = binding.textGeneros;
        valoracion = binding.textValoracion;
        descripcion = binding.textDescripcion;
        botonSig = binding.btnSiguiente;
        portada = binding.imageVPortada;
        btnFav = binding.tBtnFavorite;
        btnCheck = binding.tBtnCheck;
        btnAddList = binding.tBtnAddList;
        etiquetaSinopsis = binding.etiquetaSinopsis;
        btnNoInteresa = binding.btnNoInteresa;

        mViewModel.getLinkImagen().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //sin portada (o sin libro) se limpia: Picasso revienta con cadena vacia
                if (s == null || s.trim().isEmpty()) {
                    portada.setImageDrawable(null);
                    ColorDeLibro.pintarFondo(binding.bloquePortada, ColorDeLibro.MARINO, true);
                    return;
                }
                //el tono se saca cuando la imagen ya esta pintada, no antes
                Picasso.get().load(s).into(portada, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (binding == null) {
                            return;
                        }
                        ColorDeLibro.desdeLaPortada(portada, tono -> {
                            if (binding != null) {
                                ColorDeLibro.pintarFondo(binding.bloquePortada, tono, true);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (binding != null) {
                            ColorDeLibro.pintarFondo(binding.bloquePortada,
                                    ColorDeLibro.MARINO, true);
                        }
                    }
                });
            }
        });

        //El estado del boton depende de DOS cosas: si hay libro y si esta buscando.
        //Antes solo miraba lo primero, asi que mientras cargaba ya ofrecia "Reintentar".
        mViewModel.getHayLibro().observe(getViewLifecycleOwner(), hay -> refrescarEstado());
        //la valoracion no siempre existe: el ayudante esconde la pildora cuando falta
        mViewModel.getLibroEnPantalla().observe(getViewLifecycleOwner(),
                libro -> DatosLibro.pintarValoracion(valoracion, libro));
        mViewModel.getEstaCargando().observe(getViewLifecycleOwner(), c -> refrescarEstado());

        mViewModel.getTitulo().observe(getViewLifecycleOwner(), titulo::setText);
        mViewModel.getAutor().observe(getViewLifecycleOwner(), autor::setText);
        mViewModel.getNumPag().observe(getViewLifecycleOwner(), numPag::setText);
        mViewModel.getFechaPublicacion().observe(getViewLifecycleOwner(), fecha::setText);
        mViewModel.getGeneros().observe(getViewLifecycleOwner(), genero::setText);
        mViewModel.getDescripcion().observe(getViewLifecycleOwner(), descripcion::setText);
        mViewModel.getEstadoTBtnFav().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean marcado) {
                DatosLibro.pintarAccion(btnFav, Boolean.TRUE.equals(marcado),
                        R.drawable.ic_favorite_on, R.drawable.ic_favorite_off);
            }
        });
        mViewModel.getEstadoTBtnCheck().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean marcado) {
                DatosLibro.pintarAccion(btnCheck, Boolean.TRUE.equals(marcado),
                        R.drawable.ic_checkbox_on, R.drawable.ic_checkbox_off);
            }
        });

        //los tres botones de accion deben verse igual en reposo; "A lista" no tiene
        //estado activo, pero heredaba el color primario del estilo por defecto
        DatosLibro.pintarAccion(btnAddList, false, R.drawable.ic_addlist, R.drawable.ic_addlist);

        //La sinopsis aqui va recortada; el texto entero esta en la ficha, a un toque.
        //Tanto el enlace como la propia portada llevan a ella.
        binding.btnSeguirLeyendo.setOnClickListener(v -> abrirFicha());
        portada.setOnClickListener(v -> abrirFicha());

        botonSig.setOnClickListener(v -> mViewModel.cambioLibro());
        btnNoInteresa.setOnClickListener(v -> mViewModel.descartarLibro());
        //los toggles solo avisan al repositorio: el estado vuelve por Firestore
        btnFav.setOnClickListener(v -> mViewModel.alternarFavorito());
        btnCheck.setOnClickListener(v -> mViewModel.alternarLeido());
        btnAddList.setOnClickListener(v ->
                AccionesLibro.mostrarDialogoAnadirALista(getContext(), mViewModel.getLibroMostrado()));

        //al abrir se recupera el libro en el que se quedo; solo se pide otro si no hay
        mViewModel.restaurarOCargar();

        return root;
    }

    //Abre la ficha del libro que se esta ensenando. La portada viaja con la transicion
    //de elemento compartido: crece hasta su sitio en la ficha en vez de que la pantalla
    //salte de golpe.
    private void abrirFicha() {
        Libro libro = mViewModel.getLibroMostrado();
        if (libro == null || getActivity() == null) {
            return;
        }
        Intent ir = new Intent(requireContext(), LibroData.class);
        ir.putExtra("libro", libro);
        startActivity(ir, ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), portada, TRANSICION_PORTADA).toBundle());
    }

    private void refrescarEstado() {
        boolean hayLibro = Boolean.TRUE.equals(mViewModel.getHayLibro().getValue());
        boolean cargando = Boolean.TRUE.equals(mViewModel.getEstaCargando().getValue());

        if (cargando) {
            botonSig.setText(R.string.buscando);
        } else {
            botonSig.setText(hayLibro ? R.string.siguiente : R.string.reintentar);
        }
        //mientras busca, el boton no invita a reintentar algo que ya esta en marcha
        botonSig.setEnabled(!cargando);

        btnFav.setEnabled(hayLibro);
        btnCheck.setEnabled(hayLibro);
        btnAddList.setEnabled(hayLibro);
        btnNoInteresa.setEnabled(hayLibro);
        //las pildoras vacias daban sensacion de app rota mientras no habia datos
        int visibilidad = hayLibro ? View.VISIBLE : View.INVISIBLE;
        numPag.setVisibility(visibilidad);
        fecha.setVisibility(visibilidad);
        genero.setVisibility(visibilidad);
        autor.setVisibility(visibilidad);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
