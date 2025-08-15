package com.example.newgoodbooks.UI;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;

//Pintado de los datos que aporta Open Library: valoracion, temas y editorial.
//Vive aparte porque la valoracion se ensenia en dos sitios (Principal y la ficha) y
//porque la regla importante es la misma en todos: si el dato no esta, la fila entera
//desaparece. Un hueco con una etiqueta y nada al lado es lo que hacia que la app
//pareciese rota antes.
public final class DatosLibro {
    //mas de esto no cabe sin que los temas se coman la pantalla
    private static final int MAX_TEMAS = 6;

    private DatosLibro() {
    }

    //Marca un boton de accion como activo: icono lleno y acento dorado. Estaba
    //duplicado LITERALMENTE en LibroData y HomeFragment; cada retoque habia que
    //hacerlo dos veces y una se olvidaba.
    public static void pintarAccion(MaterialButton boton, boolean activo,
                                    int iconoOn, int iconoOff) {
        int acento = ContextCompat.getColor(boton.getContext(), R.color.md_tertiary);
        int apagado = MaterialColors.getColor(boton,
                com.google.android.material.R.attr.colorOnSurfaceVariant);
        boton.setIconResource(activo ? iconoOn : iconoOff);
        boton.setIconTint(ColorStateList.valueOf(activo ? acento : apagado));
        boton.setTextColor(activo ? acento : apagado);
        boton.setStrokeColor(ColorStateList.valueOf(activo ? acento
                : MaterialColors.getColor(boton,
                        com.google.android.material.R.attr.colorOutline)));
    }

    //Google devuelve la fecha como "2009-10-13" o como "2004". En la ficha solo
    //interesa el anio: la fecha completa no aporta y descuadra la pildora.
    //Tambien estaba duplicado, en LibroData y en HomeViewModel.
    public static String soloAnio(String fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.length() >= 4 ? fecha.substring(0, 4) : fecha;
    }

    public static void pintarValoracion(TextView pildora, Libro libro) {
        if (pildora == null) {
            return;
        }
        if (libro == null || !libro.tieneValoracion()) {
            pildora.setVisibility(View.GONE);
            return;
        }
        //con coma o con punto segun el idioma del dispositivo
        String nota = String.format(Locale.getDefault(), "%.1f", libro.getValoracion());
        pildora.setText(nota);
        pildora.setContentDescription(pildora.getContext()
                .getString(R.string.desc_valoracion, nota, libro.getNumVotos()));
        pildora.setVisibility(View.VISIBLE);
    }

    public static void pintarTemas(TextView etiqueta, ChipGroup grupo, Libro libro) {
        if (grupo == null) {
            return;
        }
        grupo.removeAllViews();
        List<String> temas = libro == null ? null : libro.getMaterias();
        if (temas == null || temas.isEmpty()) {
            grupo.setVisibility(View.GONE);
            if (etiqueta != null) {
                etiqueta.setVisibility(View.GONE);
            }
            return;
        }
        Context contexto = grupo.getContext();
        for (String tema : temas.subList(0, Math.min(MAX_TEMAS, temas.size()))) {
            Chip chip = new Chip(contexto);
            chip.setText(tema);
            //informativos: sin ellos el chip sale pulsable y con marca de seleccion
            chip.setClickable(false);
            chip.setCheckable(false);
            grupo.addView(chip);
        }
        grupo.setVisibility(View.VISIBLE);
        if (etiqueta != null) {
            etiqueta.setVisibility(View.VISIBLE);
        }
    }

    public static void pintarEditorial(TextView vista, Libro libro) {
        if (vista == null) {
            return;
        }
        String editorial = libro == null ? null : libro.getEditorial();
        if (editorial == null || editorial.trim().isEmpty()) {
            vista.setVisibility(View.GONE);
            return;
        }
        vista.setText(vista.getContext().getString(R.string.etiqueta_editorial)
                + " · " + editorial);
        vista.setVisibility(View.VISIBLE);
    }
}
