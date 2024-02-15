package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AccesoFicheros accesoFicheros=new AccesoFicheros(getApplicationContext());

                Datos.DatosComunes.setListasUsuario(accesoFicheros.getListas());
                Datos.DatosComunes.setHistorial(accesoFicheros.getHistorial());
                Datos.DatosComunes.setPrincipal(accesoFicheros.getPrincipal());

                FirebaseAuth mAuth=FirebaseAuth.getInstance();
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if(firebaseUser!=null){
                    Intent intent=new Intent(getApplicationContext(), Principal.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent=new Intent(MainActivity.this, Inicio.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).start();
    }
}