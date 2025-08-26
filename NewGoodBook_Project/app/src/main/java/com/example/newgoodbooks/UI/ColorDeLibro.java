package com.example.newgoodbooks.UI;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.example.newgoodbooks.R;

//El fondo que hay detras de la portada toma el color del libro.
//
//Antes era el mismo marino para todos, asi que "El heroe discreto" y "La sombra del
//viento" se veian identicos. El tono NO sustituye al marino: se mezcla con el, que es
//lo que se aprobo. Asi cada libro se distingue y la aplicacion sigue siendo la misma.
public final class ColorDeLibro {

    //el hero_fondo de siempre, que sigue mandando en la mezcla
    public static final int MARINO = 0xFF0C1E3D;

    //Cuanto del libro entra donde mas se nota, justo detras de la portada. En el borde
    //no entra nada: alli manda el marino.
    //
    //El color NO se reparte por todo el bloque a proposito. Mezclar un tono calido con
    //el marino da gris pardo -- son casi complementarios -- y con la mayoria de portadas
    //el fondo salia embarrado. Concentrandolo en un resplandor detras de la portada, el
    //color se percibe donde tiene sentido y el marino sigue siendo el fondo.
    private static final float PESO_CENTRO = 0.55f;

    //Se le sube la saturacion al tono y se le fija una luminosidad baja, para que el
    //resultado no dependa de si la portada es clara u oscura y siempre quede hondo.
    private static final float SATURACION_MINIMA = 0.60f;
    private static final float LUMINOSIDAD = 0.32f;

    //el resplandor arranca donde esta la portada, no en el centro geometrico
    private static final float CENTRO_X = 0.5f;
    private static final float CENTRO_Y = 0.38f;

    private static final long MS_FUNDIDO = 600;

    private ColorDeLibro() {
    }

    //Tono dominante de la portada, ya listo para mezclar. Devuelve el marino si la
    //imagen no da ninguno aprovechable.
    public static int tonoDe(Palette paleta) {
        if (paleta == null) {
            return MARINO;
        }
        //en este orden: los vibrantes tinen; los apagados son el ultimo recurso
        int color = primero(
                paleta.getVibrantColor(0),
                paleta.getDarkVibrantColor(0),
                paleta.getLightVibrantColor(0),
                paleta.getMutedColor(0),
                paleta.getDarkMutedColor(0));
        if (color == 0) {
            return MARINO;
        }
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[1] = Math.min(Math.max(hsl[1], SATURACION_MINIMA), 0.95f);
        hsl[2] = LUMINOSIDAD;
        return ColorUtils.HSLToColor(hsl);
    }

    private static int primero(int... colores) {
        for (int c : colores) {
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    //el color en el centro del resplandor; en el borde siempre es el marino
    public static int fondoCentro(int tono) {
        return ColorUtils.blendARGB(MARINO, tono, PESO_CENTRO);
    }

    //Pide la paleta de la imagen que ya tiene pintada el ImageView. Se hace aqui y no
    //en quien carga la imagen porque Palette trabaja en su propio hilo y devuelve el
    //resultado en el principal.
    public static void desdeLaPortada(ImageView portada, AlSacarTono alSacar) {
        Bitmap mapa = bitmapDe(portada);
        if (mapa == null || alSacar == null) {
            return;
        }
        Palette.from(mapa).clearFilters().generate(paleta -> alSacar.tono(tonoDe(paleta)));
    }

    private static Bitmap bitmapDe(ImageView vista) {
        if (vista == null) {
            return null;
        }
        Drawable dibujo = vista.getDrawable();
        if (!(dibujo instanceof BitmapDrawable)) {
            return null;
        }
        Bitmap mapa = ((BitmapDrawable) dibujo).getBitmap();
        return mapa == null || mapa.isRecycled() ? null : mapa;
    }

    //Pinta el resplandor del hero. Si ya habia uno, va de un color al otro en vez de
    //saltar: sin el fundido, pasar al siguiente libro da un parpadeo.
    public static void pintarFondo(View hero, int tono, boolean animar) {
        if (hero == null) {
            return;
        }
        int anterior = tonoPintado(hero);
        hero.setTag(R.id.tono_hero, tono);

        if (!animar || anterior == 0) {
            hero.setBackground(resplandor(tono, hero));
            return;
        }
        ValueAnimator fundido = ValueAnimator.ofFloat(0f, 1f);
        fundido.setDuration(MS_FUNDIDO);
        fundido.addUpdateListener(paso -> hero.setBackground(resplandor(
                ColorUtils.blendARGB(anterior, tono, paso.getAnimatedFraction()), hero)));
        fundido.start();
    }

    private static int tonoPintado(View hero) {
        Object guardado = hero.getTag(R.id.tono_hero);
        return guardado instanceof Integer ? (Integer) guardado : 0;
    }

    //Resplandor del color del libro sobre el marino. El radio se saca de la propia
    //vista para que en tableta, donde el bloque es mucho mas ancho, el color no se
    //quede en una mancha pequenia en el medio.
    private static GradientDrawable resplandor(int tono, View hero) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        fondo.setColors(new int[]{fondoCentro(tono), MARINO});
        fondo.setGradientCenter(CENTRO_X, CENTRO_Y);
        int lado = Math.max(hero.getWidth(), hero.getHeight());
        //mientras la vista no esta medida todavia no hay lado; el valor de respaldo
        //solo dura hasta el primer pintado
        fondo.setGradientRadius(lado > 0 ? lado * 0.75f : 600f);
        return fondo;
    }

    public interface AlSacarTono {
        void tono(int tono);
    }
}
