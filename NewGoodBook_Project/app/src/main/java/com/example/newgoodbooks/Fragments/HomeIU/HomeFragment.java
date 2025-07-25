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
import com.example.newgoodbooks.databinding.FragmentHomeBinding;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {
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
        descripcion = binding.textDescripcion;
        botonSig = binding.btnSiguiente;
        portada = binding.imageVPortada;
        btnFav = binding.tBtnFavorite;
        btnCheck = binding.tBtnCheck;
        btnAddList = binding.tBtnAddList;
        etiquetaSinopsis = binding.etiquetaSinopsis;

        mViewModel.getLinkImagen().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //sin portada (o sin libro) se limpia: Picasso revienta con cadena vacia
                if (s == null || s.trim().isEmpty()) {
                    portada.setImageDrawable(null);
                } else {
                    Picasso.get().load(s).into(portada);
                }
            }
        });

        //cuando no hay libro que mostrar, la pantalla pasa a modo "Reintentar"
        mViewModel.getHayLibro().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hay) {
                boolean disponible = Boolean.TRUE.equals(hay);
                botonSig.setText(disponible ? R.string.siguiente : R.string.reintentar);
                btnFav.setEnabled(disponible);
                btnCheck.setEnabled(disponible);
                btnAddList.setEnabled(disponible);
            }
        });

        mViewModel.getTitulo().observe(getViewLifecycleOwner(), titulo::setText);
        mViewModel.getAutor().observe(getViewLifecycleOwner(), autor::setText);
        mViewModel.getNumPag().observe(getViewLifecycleOwner(), numPag::setText);
        mViewModel.getFechaPublicacion().observe(getViewLifecycleOwner(), fecha::setText);
        mViewModel.getGeneros().observe(getViewLifecycleOwner(), genero::setText);
        mViewModel.getDescripcion().observe(getViewLifecycleOwner(), descripcion::setText);
        mViewModel.getEstadoTBtnFav().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean marcado) {
                pintarAccion(btnFav, Boolean.TRUE.equals(marcado),
                        R.drawable.ic_favorite_on, R.drawable.ic_favorite_off);
            }
        });
        mViewModel.getEstadoTBtnCheck().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean marcado) {
                pintarAccion(btnCheck, Boolean.TRUE.equals(marcado),
                        R.drawable.ic_checkbox_on, R.drawable.ic_checkbox_off);
            }
        });

        //los tres botones de accion deben verse igual en reposo; "A lista" no tiene
        //estado activo, pero heredaba el color primario del estilo por defecto
        pintarAccion(btnAddList, false, R.drawable.ic_addlist, R.drawable.ic_addlist);

        botonSig.setOnClickListener(v -> mViewModel.cambioLibro());
        //los toggles solo avisan al repositorio: el estado vuelve por Firestore
        btnFav.setOnClickListener(v -> mViewModel.alternarFavorito());
        btnCheck.setOnClickListener(v -> mViewModel.alternarLeido());
        btnAddList.setOnClickListener(v ->
                AccionesLibro.mostrarDialogoAnadirALista(getContext(), mViewModel.getLibroMostrado()));

        //si al abrir no hay recomendacion, se pide al servidor en segundo plano
        if (mViewModel.getLibroMostrado() == null) {
            mViewModel.cargarRecomendaciones();
        }

        return root;
    }

    //Marca una accion como activa: icono lleno y acento dorado. Antes eran ToggleButton,
    //que no permitian este tratamiento sin pelearse con su fondo por defecto.
    private void pintarAccion(MaterialButton boton, boolean activo, int iconoOn, int iconoOff) {
        int acento = ContextCompat.getColor(requireContext(), R.color.md_tertiary);
        int apagado = com.google.android.material.color.MaterialColors.getColor(
                boton, com.google.android.material.R.attr.colorOnSurfaceVariant);
        boton.setIconResource(activo ? iconoOn : iconoOff);
        boton.setIconTint(android.content.res.ColorStateList.valueOf(activo ? acento : apagado));
        boton.setTextColor(activo ? acento : apagado);
        boton.setStrokeColor(android.content.res.ColorStateList.valueOf(
                activo ? acento : com.google.android.material.color.MaterialColors.getColor(
                        boton, com.google.android.material.R.attr.colorOutline)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
