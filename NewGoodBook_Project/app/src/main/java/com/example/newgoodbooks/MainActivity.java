package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hilo mientras se ejecuta esta actividad a modo de splash screen.
        //Solo lee ficheros locales: las recomendaciones se piden ya dentro de Home,
        //asi que un fallo de red no puede impedir que la app arranque.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AccesoFicheros accesoFicheros = new AccesoFicheros(getApplicationContext());
                    Datos.DatosComunes.setListasUsuario(accesoFicheros.getListas());
                    Datos.DatosComunes.setHistorial(accesoFicheros.getHistorial());
                    Datos.DatosComunes.setPrincipal(accesoFicheros.getPrincipal());
                } catch (Exception e) {
                    //pase lo que pase se entra a la app; los getters de Datos se auto-inicializan vacios
                    Log.e(TAG, "Fallo cargando los datos locales, se arranca sin ellos", e);
                }
                //Verifica si el usuario ya esta logeado
                boolean haySesion = FirebaseAuth.getInstance().getCurrentUser() != null;
                final Class<?> destino = haySesion ? Principal.class : Inicio.class;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        startActivity(new Intent(MainActivity.this, destino));
                        finish();
                    }
                });
            }
        }).start();
    }
}
