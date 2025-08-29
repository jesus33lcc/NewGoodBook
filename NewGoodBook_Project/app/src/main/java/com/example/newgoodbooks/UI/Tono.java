package com.example.newgoodbooks.UI;

//Aritmetica de color, sin nada de Android.
//
//Vive aparte de ColorDeLibro justo por eso: ColorDeLibro necesita Palette, vistas y
//drawables, asi que no se puede probar sin un dispositivo. Esto si, y es donde estan
//las decisiones que pueden salir mal -- sobre todo la de garantizar que el texto
//blanco siga leyendose encima del fondo que se genere.
public final class Tono {

    //ARGB opaco, sin usar android.graphics.Color
    private static final int OPACO = 0xFF000000;

    private Tono() {
    }

    public static int rojo(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int verde(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int azul(int color) {
        return color & 0xFF;
    }

    public static int de(int r, int g, int b) {
        return OPACO | (acotar(r) << 16) | (acotar(g) << 8) | acotar(b);
    }

    private static int acotar(int v) {
        return Math.max(0, Math.min(255, v));
    }

    //Mezcla dos colores. proporcion 0 devuelve el primero; 1, el segundo.
    public static int mezclar(int desde, int hasta, float proporcion) {
        float p = Math.max(0f, Math.min(1f, proporcion));
        return de(Math.round(rojo(desde) + (rojo(hasta) - rojo(desde)) * p),
                Math.round(verde(desde) + (verde(hasta) - verde(desde)) * p),
                Math.round(azul(desde) + (azul(hasta) - azul(desde)) * p));
    }

    //Reescribe la saturacion y la luminosidad conservando el matiz. Es lo que hace que
    //el fondo no dependa de si la portada es clara u oscura.
    public static int conSaturacionYLuz(int color, float saturacion, float luz) {
        float[] hsl = aHsl(color);
        return desdeHsl(hsl[0], acotarUno(saturacion), acotarUno(luz));
    }

    private static float acotarUno(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static float[] aHsl(int color) {
        float r = rojo(color) / 255f;
        float g = verde(color) / 255f;
        float b = azul(color) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float amplitud = max - min;
        float luz = (max + min) / 2f;

        float matiz = 0f;
        float saturacion = 0f;
        if (amplitud > 0f) {
            saturacion = luz > 0.5f ? amplitud / (2f - max - min) : amplitud / (max + min);
            if (max == r) {
                matiz = ((g - b) / amplitud + (g < b ? 6f : 0f));
            } else if (max == g) {
                matiz = (b - r) / amplitud + 2f;
            } else {
                matiz = (r - g) / amplitud + 4f;
            }
            matiz *= 60f;
        }
        return new float[]{matiz, saturacion, luz};
    }

    public static int desdeHsl(float matiz, float saturacion, float luz) {
        if (saturacion <= 0f) {
            int gris = Math.round(luz * 255f);
            return de(gris, gris, gris);
        }
        float q = luz < 0.5f ? luz * (1f + saturacion) : luz + saturacion - luz * saturacion;
        float p = 2f * luz - q;
        float h = matiz / 360f;
        return de(Math.round(canal(p, q, h + 1f / 3f) * 255f),
                Math.round(canal(p, q, h) * 255f),
                Math.round(canal(p, q, h - 1f / 3f) * 255f));
    }

    private static float canal(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    //Luminancia relativa segun la norma de accesibilidad
    public static double luminancia(int color) {
        return 0.2126 * canalLineal(rojo(color))
                + 0.7152 * canalLineal(verde(color))
                + 0.0722 * canalLineal(azul(color));
    }

    private static double canalLineal(int valor) {
        double v = valor / 255d;
        return v <= 0.03928d ? v / 12.92d : Math.pow((v + 0.055d) / 1.055d, 2.4d);
    }

    //Relacion de contraste entre dos colores, de 1:1 a 21:1
    public static double contraste(int uno, int otro) {
        double a = luminancia(uno);
        double b = luminancia(otro);
        return (Math.max(a, b) + 0.05d) / (Math.min(a, b) + 0.05d);
    }
}
