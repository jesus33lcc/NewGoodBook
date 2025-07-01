package com.example.newgoodbooks;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

//Pestania de cerrar sesion. Antes cerraba la sesion directamente en onCreate,
//asi que bastaba con rozar la pestania para salir, y ademas dejaba las listas y el
//historial del usuario anterior en memoria y en disco (se los encontraba el siguiente).
public class FragmentLogout extends Fragment {

    public FragmentLogout() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pedirConfirmacion();
    }

    private void pedirConfirmacion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesion")
                .setMessage("Vas a salir de tu cuenta. Tus listas seguiran guardadas.")
                .setPositiveButton("Cerrar sesion", (dialog, boton) -> cerrarSesion())
                .setNegativeButton("Cancelar", (dialog, boton) -> volverAHome())
                .setOnCancelListener(dialog -> volverAHome())
                .show();
    }

    //si el usuario se arrepiente, se vuelve a la pestania principal
    private void volverAHome() {
        if (!isAdded()) {
            return;
        }
        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setSelectedItemId(R.id.homeFragment);
        }
    }

    private void cerrarSesion() {
        if (!isAdded()) {
            return;
        }
        //se limpia ANTES de salir: si no, el siguiente usuario que entrase en este
        //dispositivo se encontraria las listas y el historial del anterior
        new AccesoFicheros(requireContext().getApplicationContext()).borrarTodo();
        Datos.DatosComunes.limpiar();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(requireContext(), Inicio.class);
        //vacia la pila: sin esto se puede volver atras a la app ya sin sesion
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
