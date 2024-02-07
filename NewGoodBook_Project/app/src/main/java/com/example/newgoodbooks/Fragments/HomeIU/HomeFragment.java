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

import com.example.newgoodbooks.R;
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
    private Button botonSig;
    private ImageView portada;

    public HomeFragment(){
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding=FragmentHomeBinding.inflate(inflater, container, false);
        View root =binding.getRoot();
        titulo=binding.textTitulo;
        autor=binding.textAutor;
        numPag=binding.textNumPag;
        fecha=binding.textFechaPub;
        genero=binding.textGeneros;
        descripcion=binding.textDescripcion;
        botonSig=binding.btnSiguiente;
        portada=binding.imageVPortada;


        homeViewModel.getLinkImagen().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Picasso.get().load(s).into(portada);
            }
        });

        homeViewModel.getTitulo().observe(getViewLifecycleOwner(),titulo::setText);
        homeViewModel.getAutor().observe(getViewLifecycleOwner(),autor::setText);
        homeViewModel.getNumPag().observe(getViewLifecycleOwner(),numPag::setText);
        homeViewModel.getFechaPublicacion().observe(getViewLifecycleOwner(),fecha::setText);
        homeViewModel.getGeneros().observe(getViewLifecycleOwner(),genero::setText);
        homeViewModel.getDescripcion().observe(getViewLifecycleOwner(),descripcion::setText);

        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.cambiarLibro();
            }
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}