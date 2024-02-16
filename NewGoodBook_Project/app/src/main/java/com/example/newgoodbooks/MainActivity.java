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
        //Creo un hilo mientras se esta ejecutando esta actividad a modo de splash screen
        new Thread(new Runnable() {
            @Override
            public void run() {
                AccesoFicheros accesoFicheros=new AccesoFicheros(getApplicationContext());
                //Lee los ficheros, recupera los datos y los guarda en los datos estaticos
                Datos.DatosComunes.setListasUsuario(accesoFicheros.getListas());
                Datos.DatosComunes.setHistorial(accesoFicheros.getHistorial());
                Datos.DatosComunes.setPrincipal(accesoFicheros.getPrincipal());
                //Verifica si el usuario ya esta logeado
                FirebaseAuth mAuth=FirebaseAuth.getInstance();
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if(firebaseUser!=null){
                    //Ya hay una cuenta iniciada y lo manda a la actividad principal
                    Intent intent=new Intent(getApplicationContext(), Principal.class);
                    startActivity(intent);
                    finish();
                }else{
                    //No hay ninguna cuenta iniciada y lo lleva al inicio de la app
                    Intent intent=new Intent(MainActivity.this, Inicio.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).start();
    }
}