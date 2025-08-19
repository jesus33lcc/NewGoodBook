package com.example.newgoodbooks.Modelos;

import java.util.HashMap;
import java.util.Map;

//Por donde va el usuario en un libro. Vive aparte del libro porque es SUYO, no del
//libro: el mismo volumen puede estar a medias para uno y sin empezar para otro.
//
//El estado "leido" NO se guarda aqui: sigue siendo la coleccion de leidos de siempre,
//de la que ya dependen la lista fija y el perfil del recomendador. Aqui solo estan los
//dos estados que antes no se podian expresar.
public class Lectura {
    public static final String QUIERO = "quiero";
    public static final String LEYENDO = "leyendo";

    private final String estado;
    private final int pagina;

    public Lectura(String estado, int pagina) {
        this.estado = estado;
        this.pagina = pagina;
    }

    public static Lectura desdeMapa(Map<?, ?> mapa) {
        if (mapa == null) {
            return null;
        }
        Object estado = mapa.get("estado");
        if (estado == null) {
            return null;
        }
        Object pagina = mapa.get("pagina");
        return new Lectura(String.valueOf(estado),
                (pagina instanceof Number) ? ((Number) pagina).intValue() : 0);
    }

    public Map<String, Object> aMapa() {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("estado", estado);
        mapa.put("pagina", pagina);
        mapa.put("actualizado", System.currentTimeMillis());
        return mapa;
    }

    public String getEstado() {
        return estado;
    }

    public int getPagina() {
        return pagina;
    }

    public boolean estaLeyendo() {
        return LEYENDO.equals(estado);
    }

    //Cuanto lleva, de 0 a 100. Necesita el total porque el libro no viaja con la lectura.
    public int porcentaje(int totalPaginas) {
        if (totalPaginas <= 0 || pagina <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(pagina * 100f / totalPaginas));
    }
}
