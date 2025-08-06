package com.example.newgoodbooks;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

//Clase de aplicacion. Existe por una razon concreta: el tema elegido hay que volver a
//aplicarlo en cada arranque, y hacerlo en una Activity no basta, porque Android puede
//restaurar el proceso directamente en Principal sin pasar por MainActivity.
//
//El idioma NO se guarda aqui: de eso se encarga AppCompat con autoStoreLocales, que
//esta declarado en el manifiesto. Asi el que se elige dentro de la app y el que se
//elige en los ajustes del sistema (Android 13+) son el mismo dato y no se pisan.
public class App extends Application {
    private static final String PREFERENCIAS = "ajustes";
    private static final String CLAVE_TEMA = "tema";

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(temaGuardado(this));
    }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    //Por defecto se sigue al sistema: es lo que espera quien tiene el movil en oscuro.
    public static int temaGuardado(Context contexto) {
        return preferencias(contexto)
                .getInt(CLAVE_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void guardarTema(Context contexto, int modo) {
        preferencias(contexto).edit().putInt(CLAVE_TEMA, modo).apply();
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
