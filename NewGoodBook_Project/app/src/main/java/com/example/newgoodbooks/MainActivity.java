package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ya no hay nada que cargar de disco: los datos viven en Firestore y llegan
        //solos por los listeners del repositorio, con cache offline incluida.
        boolean haySesion = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (haySesion) {
            RepositorioUsuario.get().conectar();
        }
        startActivity(new Intent(this, haySesion ? Principal.class : Inicio.class));
        finish();
    }
}
