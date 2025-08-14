package com.example.newgoodbooks.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.Fragments.AdapterList.LibroListAdapter;
import com.example.newgoodbooks.R;

//Lista o cuadricula en las tres pantallas que ensenian libros. Vive aqui porque la
//eleccion es UNA para toda la aplicacion: cambiarla en Explorar y encontrarse otra
//cosa en los resultados de busqueda seria desconcertante.
public final class ModoVista {
    private static final String PREFERENCIAS = "ajustes";
    private static final String CLAVE = "vistaCuadricula";

    private ModoVista() {
    }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public static boolean esCuadricula(Context contexto) {
        return preferencias(contexto).getBoolean(CLAVE, false);
    }

    public static void alternar(Context contexto) {
        preferencias(contexto).edit()
                .putBoolean(CLAVE, !esCuadricula(contexto)).apply();
    }

    //Columnas segun el ancho, como en el resto de la aplicacion: en una tablet dejar
    //tres columnas gigantes seria desaprovechar la pantalla.
    private static int columnas(Context contexto) {
        int anchoDp = contexto.getResources().getConfiguration().screenWidthDp;
        if (anchoDp >= 840) {
            return 6;
        }
        return anchoDp >= 600 ? 4 : 3;
    }

    //Aplica el modo guardado a una rejilla y su adaptador. Se llama tambien al crear
    //la pantalla, no solo al pulsar el boton.
    public static void aplicar(Context contexto, RecyclerView rejilla,
                               LibroListAdapter adaptador) {
        boolean cuadricula = esCuadricula(contexto);
        if (adaptador != null) {
            adaptador.setCuadricula(cuadricula);
        }
        rejilla.setLayoutManager(cuadricula
                ? new GridLayoutManager(contexto, columnas(contexto))
                : new LinearLayoutManager(contexto));
        if (adaptador != null) {
            rejilla.setAdapter(adaptador);
        }
    }

    //El icono ensenia A DONDE se va, no donde se esta: estando en lista se ve el de
    //cuadricula. Es lo que espera la gente de un conmutador de un solo boton.
    public static void pintarIcono(Context contexto, Menu menu) {
        MenuItem item = menu.findItem(R.id.accion_vista);
        if (item == null) {
            return;
        }
        item.setIcon(esCuadricula(contexto)
                ? R.drawable.ic_vista_lista : R.drawable.ic_vista_cuadricula);
        //El tinte se vuelve a poner aqui porque setIcon() cambia el drawable y el que
        //trae el menu del XML no siempre sobrevive. Sin esto el icono salia casi negro
        //sobre la barra marina: 1,03 de contraste, invisible.
        MenuItemCompat.setIconTintList(item, ColorStateList.valueOf(
                ContextCompat.getColor(contexto, R.color.hero_texto)));
    }
}
