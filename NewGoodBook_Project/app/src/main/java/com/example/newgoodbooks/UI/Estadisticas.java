package com.example.newgoodbooks.UI;

import com.example.newgoodbooks.Modelos.Libro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Cuentas de "Mi año", calculadas sobre los libros marcados como leidos.
//
//Aparte de la pantalla y sin dependencias de Android para poder probarlas: son la
//unica parte de la aplicacion donde un error de calculo pasaria desapercibido, porque
//un numero mal sumado sigue pareciendo un numero correcto.
public final class Estadisticas {
    //Por debajo de esto no se ensenian: tres libros no dan para un panel, y unas
    //estadisticas vacias parecen una promesa incumplida.
    public static final int MINIMO_LIBROS = 5;
    private static final int MAX_GENEROS = 3;

    public final int libros;
    public final int paginas;
    public final List<Map.Entry<String, Integer>> generos;

    private Estadisticas(int libros, int paginas, List<Map.Entry<String, Integer>> generos) {
        this.libros = libros;
        this.paginas = paginas;
        this.generos = generos;
    }

    public boolean hayBastante() {
        return libros >= MINIMO_LIBROS;
    }

    //El genero que mas se repite, para escalar las barras de los demas.
    public int masRepetido() {
        return generos.isEmpty() ? 0 : generos.get(0).getValue();
    }

    public static Estadisticas de(List<Libro> leidos) {
        if (leidos == null) {
            return new Estadisticas(0, 0, new ArrayList<Map.Entry<String, Integer>>());
        }
        int paginas = 0;
        Map<String, Integer> cuenta = new LinkedHashMap<>();
        for (Libro libro : leidos) {
            paginas += Math.max(0, libro.getNumPag());
            List<String> generos = libro.getGeneros();
            if (generos == null || generos.isEmpty()) {
                continue;
            }
            //solo el primero: los demas son subcategorias y ensucian el recuento
            String genero = generos.get(0);
            cuenta.put(genero, (cuenta.get(genero) == null ? 0 : cuenta.get(genero)) + 1);
        }
        List<Map.Entry<String, Integer>> ordenados = new ArrayList<>(cuenta.entrySet());
        Collections.sort(ordenados, (a, b) -> b.getValue() - a.getValue());
        if (ordenados.size() > MAX_GENEROS) {
            ordenados = new ArrayList<>(ordenados.subList(0, MAX_GENEROS));
        }
        return new Estadisticas(leidos.size(), paginas, ordenados);
    }
}
