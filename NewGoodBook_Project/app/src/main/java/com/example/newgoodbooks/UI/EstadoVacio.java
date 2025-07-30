package com.example.newgoodbooks.UI;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newgoodbooks.R;

// Muestra u oculta el estado vacío incluido en un layout.
public final class EstadoVacio {

    private EstadoVacio() {
    }

    public static void mostrar(View vista, boolean visible, int icono, int titulo, int detalle) {
        if (vista == null) {
            return;
        }
        vista.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            return;
        }
        ((ImageView) vista.findViewById(R.id.vacioIcono)).setImageResource(icono);
        ((TextView) vista.findViewById(R.id.vacioTitulo)).setText(titulo);
        ((TextView) vista.findViewById(R.id.vacioDetalle)).setText(detalle);
    }
}
