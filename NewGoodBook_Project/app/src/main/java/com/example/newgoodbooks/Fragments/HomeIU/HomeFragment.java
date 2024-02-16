package com.example.newgoodbooks.Fragments.HomeIU;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Lista;
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
    private ToggleButton btnFav;
    private ToggleButton btnCheck;
    private ImageButton btnAddList;

    public HomeFragment(){
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Asignacion de variables locales
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
        btnFav=binding.tBtnFavorite;
        btnCheck=binding.tBtnCheck;
        btnAddList=binding.tBtnAddList;

        //vincula el dato del viewmodel con su componente
        mViewModel.getLinkImagen().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Picasso.get().load(s).into(portada);
            }
        });

        mViewModel.getTitulo().observe(getViewLifecycleOwner(),titulo::setText);
        mViewModel.getAutor().observe(getViewLifecycleOwner(),autor::setText);
        mViewModel.getNumPag().observe(getViewLifecycleOwner(),numPag::setText);
        mViewModel.getFechaPublicacion().observe(getViewLifecycleOwner(),fecha::setText);
        mViewModel.getGeneros().observe(getViewLifecycleOwner(),genero::setText);
        mViewModel.getDescripcion().observe(getViewLifecycleOwner(),descripcion::setText);
        mViewModel.getEstadoTBtnFav().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                btnFav.setChecked(aBoolean);
            }
        });
        mViewModel.getEstadoTBtnCheck().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                btnCheck.setChecked(aBoolean);
            }
        });
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.cambioLibro(getContext());
            }
        });
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccesoFicheros accesoFicheros=new AccesoFicheros(getContext());
                if(mViewModel.getEstadoTBtnFav().getValue()){
                    Datos.DatosComunes.getListasUsuario().getLibrosLike().remove(mViewModel.getLibroMostrado());
                    mViewModel.setEstadoTBtnFav(false);
                }else {
                    Datos.DatosComunes.getListasUsuario().getLibrosLike().add(mViewModel.getLibroMostrado());
                    mViewModel.setEstadoTBtnFav(true);
                }
                accesoFicheros.setListas(Datos.DatosComunes.getListasUsuario());
            }
        });
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccesoFicheros accesoFicheros=new AccesoFicheros(getContext());
                if(mViewModel.getEstadoTBtnCheck().getValue()){
                    Datos.DatosComunes.getListasUsuario().getLibrosCheck().remove(mViewModel.getLibroMostrado());
                    mViewModel.setEstadoTBtnCheck(false);
                }else {
                    Datos.DatosComunes.getListasUsuario().getLibrosCheck().add(mViewModel.getLibroMostrado());
                    mViewModel.setEstadoTBtnCheck(true);
                }
                accesoFicheros.setListas(Datos.DatosComunes.getListasUsuario());
            }
        });

        btnAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogSingleChoice_addToList();
            }
        });

        return root;
    }
    //Muestra el dialog que te permite seleccionar la lista para añadir el libro
    private void showAlertDialogSingleChoice_addToList(){
        String[] nomListas = Datos.DatosComunes.getNomListasPersonal();
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(getContext());
        alertDialog_Builder.setTitle("Añadir a lista");
        alertDialog_Builder.setIcon(R.drawable.ic_listas);
        alertDialog_Builder.setSingleChoiceItems(nomListas, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                addLibro_To_Lista(index);
                dialog.dismiss();
            }
        });
        alertDialog_Builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){

            }
        });
        AlertDialog addDialog = alertDialog_Builder.create();
        addDialog.show();
        //alertDialog_Builder.show();
    }
    //metodo que añade el libro a la lista seleccionada
    private void addLibro_To_Lista(int index){
        Lista listaSelected = Datos.DatosComunes.searchByIndexListas(index);
        listaSelected.getLibros().add(mViewModel.getLibroMostrado());

        AccesoFicheros accesoFicheros = new AccesoFicheros(getContext());
        accesoFicheros.setListas(Datos.DatosComunes.getListasUsuario());

        Toast.makeText(getActivity(), "Añadido a '" + listaSelected.getNombre(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}