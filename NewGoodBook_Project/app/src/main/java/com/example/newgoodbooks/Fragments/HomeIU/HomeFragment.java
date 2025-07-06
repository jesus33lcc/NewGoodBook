package com.example.newgoodbooks.Fragments.HomeIU;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.example.newgoodbooks.Datos.RepositorioUsuario;
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
                botonSig.setText(disponible ? "Siguiente" : "Reintentar");
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
                btnFav.setChecked(Boolean.TRUE.equals(marcado));
            }
        });
        mViewModel.getEstadoTBtnCheck().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean marcado) {
                btnCheck.setChecked(Boolean.TRUE.equals(marcado));
            }
        });

        botonSig.setOnClickListener(v -> mViewModel.cambioLibro());
        //los toggles solo avisan al repositorio: el estado vuelve por Firestore
        btnFav.setOnClickListener(v -> mViewModel.alternarFavorito());
        btnCheck.setOnClickListener(v -> mViewModel.alternarLeido());
        btnAddList.setOnClickListener(v -> {
            if (mViewModel.getLibroMostrado() != null) {
                showAlertDialogSingleChoice_addToList();
            }
        });

        //si al abrir no hay recomendacion, se pide al servidor en segundo plano
        if (mViewModel.getLibroMostrado() == null) {
            mViewModel.cargarRecomendaciones();
        }

        return root;
    }

    //Muestra el dialog que te permite seleccionar la lista para añadir el libro
    private void showAlertDialogSingleChoice_addToList(){
        RepositorioUsuario repo = RepositorioUsuario.get();
        String[] nomListas = repo.getNombresListasPersonales();
        if (nomListas.length == 0) {
            Toast.makeText(getActivity(), "Todavia no tienes ninguna lista. Crea una en Listas.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
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
        alertDialog_Builder.setNeutralButton("Cancelar", null);
        alertDialog_Builder.create().show();
    }

    private void addLibro_To_Lista(int index){
        RepositorioUsuario repo = RepositorioUsuario.get();
        Lista listaSelected = repo.getListaPersonalPorIndice(index);
        if (listaSelected == null || mViewModel.getLibroMostrado() == null) {
            return;
        }
        repo.anadirLibroALista(listaSelected.getId(), mViewModel.getLibroMostrado());
        Toast.makeText(getActivity(), "Añadido a '" + listaSelected.getNombre() + "'",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
