package com.example.newgoodbooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.newgoodbooks.UI.Tono;

import org.junit.Test;

//Pruebas de la aritmetica de color del fondo de la ficha.
//
//La que de verdad importa es la ultima: el fondo se saca de la portada, o sea de una
//imagen que no controlamos, asi que hay que demostrar que NINGUNA portada posible
//puede generar un fondo donde el texto blanco deje de leerse.
public class TonoTest {

    private static final int MARINO = 0xFF0C1E3D;
    private static final int BLANCO = 0xFFFFFFFF;

    //los mismos valores que usa ColorDeLibro
    private static final float SATURACION_MINIMA = 0.60f;
    private static final float LUMINOSIDAD = 0.32f;
    private static final float PESO_CENTRO = 0.55f;

    //lo que exige la norma de accesibilidad para texto normal
    private static final double CONTRASTE_MINIMO = 4.5d;

    @Test
    public void mezclarEnLosExtremosDevuelveCadaColor() {
        assertEquals(MARINO, Tono.mezclar(MARINO, BLANCO, 0f));
        assertEquals(BLANCO, Tono.mezclar(MARINO, BLANCO, 1f));
    }

    @Test
    public void mezclarAMitadCaeEntreLosDos() {
        int medio = Tono.mezclar(0xFF000000, 0xFF646464, 0.5f);
        assertEquals(50, Tono.rojo(medio));
        assertEquals(50, Tono.verde(medio));
        assertEquals(50, Tono.azul(medio));
    }

    @Test
    public void unaProporcionFueraDeRangoNoSeSaleDeLosExtremos() {
        assertEquals(MARINO, Tono.mezclar(MARINO, BLANCO, -3f));
        assertEquals(BLANCO, Tono.mezclar(MARINO, BLANCO, 7f));
    }

    @Test
    public void pasarAHslYVolverConservaElColor() {
        for (int color : new int[]{0xFFE89D10, 0xFF0C1E3D, 0xFFB3261E, 0xFF4CAF50}) {
            float[] hsl = Tono.aHsl(color);
            int vuelta = Tono.desdeHsl(hsl[0], hsl[1], hsl[2]);
            //se admite un punto de diferencia por el redondeo a enteros
            assertTrue("no vuelve " + Integer.toHexString(color),
                    Math.abs(Tono.rojo(color) - Tono.rojo(vuelta)) <= 1
                            && Math.abs(Tono.verde(color) - Tono.verde(vuelta)) <= 1
                            && Math.abs(Tono.azul(color) - Tono.azul(vuelta)) <= 1);
        }
    }

    @Test
    public void unGrisNoTieneSaturacionNiSeInventaUnMatiz() {
        float[] hsl = Tono.aHsl(0xFF808080);
        assertEquals(0f, hsl[1], 0.001f);
        assertEquals(0f, hsl[0], 0.001f);
    }

    @Test
    public void elContrasteDelBlancoSobreNegroEsElMaximo() {
        assertEquals(21d, Tono.contraste(BLANCO, 0xFF000000), 0.1d);
    }

    @Test
    public void elContrasteDeUnColorConsigoMismoEsUno() {
        assertEquals(1d, Tono.contraste(MARINO, MARINO), 0.001d);
    }

    //LA PRUEBA IMPORTANTE. Recorre todo el circulo de matices y todas las saturaciones
    //y luminosidades de partida, o sea cualquier portada imaginable, y comprueba que el
    //fondo que sale de ahi deja leerse el texto blanco que va encima.
    @Test
    public void ningunaPortadaPuedeGenerarUnFondoIlegible() {
        double peor = 21d;
        int culpable = 0;
        for (int matiz = 0; matiz < 360; matiz += 5) {
            for (float sat = 0f; sat <= 1f; sat += 0.1f) {
                for (float luz = 0.05f; luz <= 0.95f; luz += 0.1f) {
                    int dePortada = Tono.desdeHsl(matiz, sat, luz);
                    //lo mismo que hace ColorDeLibro.tonoDe y fondoCentro
                    float saturado = Math.min(Math.max(Tono.aHsl(dePortada)[1],
                            SATURACION_MINIMA), 0.95f);
                    int tono = Tono.conSaturacionYLuz(dePortada, saturado, LUMINOSIDAD);
                    int fondo = Tono.mezclar(MARINO, tono, PESO_CENTRO);

                    double contraste = Tono.contraste(BLANCO, fondo);
                    if (contraste < peor) {
                        peor = contraste;
                        culpable = fondo;
                    }
                }
            }
        }
        assertTrue("el fondo #" + Integer.toHexString(culpable)
                        + " deja el blanco a " + Math.round(peor * 10) / 10d + ":1",
                peor >= CONTRASTE_MINIMO);
    }
}
