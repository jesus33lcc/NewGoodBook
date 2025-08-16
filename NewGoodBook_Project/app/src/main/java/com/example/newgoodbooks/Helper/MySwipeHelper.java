package com.example.newgoodbooks.Helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

//Deslizar una fila a la izquierda para revelar un boton.
//
//Reescrito. La version anterior venia de un tutorial y arrastraba tres cosas malas:
//  · se enganchaba sola dentro del constructor, asi que quien la creaba tenia que
//    asignarla a una variable que nunca usaba para que no pareciese un error;
//  · guardaba los botones en un mapa por posicion de adaptador, que cambia al
//    reordenarse la lista, de modo que un boton podia acabar apuntando a otra fila;
//  · dibujaba a mano el fondo, el icono y el texto con Paint en cada frame.
//
//Ahora hay UN boton por fila, se resuelve en el momento del toque a partir del
//ViewHolder que se esta deslizando, y engancharlo es explicito.
public class MySwipeHelper extends ItemTouchHelper.SimpleCallback {

    //Que hacer cuando se toca el boton de una fila.
    public interface AlPulsar {
        void enPosicion(int posicion);
    }

    private final Context contexto;
    private final int anchoBoton;
    private final String texto;
    private final Drawable icono;
    private final int colorFondo;
    private final AlPulsar alPulsar;
    private final Paint pintura = new Paint();

    //la fila que ahora mismo ensenia el boton, y hasta donde llega
    private RecyclerView.ViewHolder filaAbierta;
    private final RectF zonaBoton = new RectF();

    public MySwipeHelper(Context contexto, int anchoBoton, String texto, int idIcono,
                         int colorFondo, AlPulsar alPulsar) {
        super(0, ItemTouchHelper.LEFT);
        this.contexto = contexto;
        this.anchoBoton = anchoBoton;
        this.texto = texto;
        this.icono = ContextCompat.getDrawable(contexto, idIcono);
        this.colorFondo = colorFondo;
        this.alPulsar = alPulsar;
    }

    //Engancharlo es explicito: antes lo hacia el constructor por su cuenta.
    public void engancharA(RecyclerView lista) {
        new ItemTouchHelper(this).attachToRecyclerView(lista);
        lista.setOnTouchListener((v, evento) -> {
            if (filaAbierta == null) {
                return false;
            }
            if (evento.getAction() == android.view.MotionEvent.ACTION_UP
                    && zonaBoton.contains(evento.getX(), evento.getY())) {
                int posicion = filaAbierta.getBindingAdapterPosition();
                //se resuelve AHORA, no cuando se dibujo: la lista puede haber cambiado
                if (posicion != RecyclerView.NO_POSITION) {
                    alPulsar.enPosicion(posicion);
                }
                v.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onMove(@NonNull RecyclerView lista, @NonNull RecyclerView.ViewHolder origen,
                          @NonNull RecyclerView.ViewHolder destino) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder fila, int direccion) {
        //no se borra al deslizar: el deslizamiento solo descubre el boton
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder fila) {
        //asi la fila se queda abierta en vez de completar el gesto
        return 0.6f;
    }

    @Override
    public void onChildDraw(@NonNull Canvas lienzo, @NonNull RecyclerView lista,
                            @NonNull RecyclerView.ViewHolder fila, float dX, float dY,
                            int estado, boolean activo) {
        float desplazamiento = Math.max(dX, -anchoBoton);
        if (estado == ItemTouchHelper.ACTION_STATE_SWIPE && desplazamiento < 0) {
            filaAbierta = fila;
            dibujarBoton(lienzo, fila);
        } else if (desplazamiento == 0) {
            if (filaAbierta == fila) {
                filaAbierta = null;
            }
        }
        super.onChildDraw(lienzo, lista, fila, desplazamiento, dY, estado, activo);
    }

    private void dibujarBoton(Canvas lienzo, RecyclerView.ViewHolder fila) {
        android.view.View vista = fila.itemView;
        zonaBoton.set(vista.getRight() - anchoBoton, vista.getTop(),
                vista.getRight(), vista.getBottom());

        pintura.setColor(colorFondo);
        lienzo.drawRect(zonaBoton, pintura);

        pintura.setColor(Color.WHITE);
        pintura.setTextSize(28);
        pintura.setAntiAlias(true);
        pintura.setTextAlign(Paint.Align.CENTER);
        Rect limites = new Rect();
        pintura.getTextBounds(texto, 0, texto.length(), limites);
        float centroX = zonaBoton.centerX();
        float baseTexto = zonaBoton.centerY() + limites.height() + 18;
        lienzo.drawText(texto, centroX, baseTexto, pintura);

        if (icono != null) {
            int lado = 44;
            int izq = (int) (centroX - lado / 2f);
            int arriba = (int) (zonaBoton.centerY() - lado / 2f - 14);
            icono.setBounds(izq, arriba, izq + lado, arriba + lado);
            icono.setTint(Color.WHITE);
            icono.draw(lienzo);
        }
    }
}
