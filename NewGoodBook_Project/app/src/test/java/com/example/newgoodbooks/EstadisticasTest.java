package com.example.newgoodbooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.UI.Estadisticas;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Las cuentas de "Mi año". Se prueban porque un numero mal sumado sigue pareciendo un
//numero correcto: es el unico sitio de la aplicacion donde el fallo no se ve.
public class EstadisticasTest {

    private Libro libro(int paginas, String genero) {
        return new Libro("id" + paginas + genero, "Titulo", Arrays.asList("Autor"),
                paginas, "2020", Arrays.asList(genero), "Descripcion", "https://x/y.jpg");
    }

    @Test
    public void sumaLibrosYPaginas() {
        Estadisticas e = Estadisticas.de(Arrays.asList(
                libro(100, "Fiction"), libro(250, "Fiction"), libro(50, "Poetry")));
        assertEquals(3, e.libros);
        assertEquals(400, e.paginas);
    }

    @Test
    public void ordenaLosGenerosPorCuantosLibrosAportan() {
        Estadisticas e = Estadisticas.de(Arrays.asList(
                libro(10, "Poetry"), libro(20, "Fiction"),
                libro(30, "Fiction"), libro(40, "Fiction")));
        assertEquals("Fiction", e.generos.get(0).getKey());
        assertEquals(3, (int) e.generos.get(0).getValue());
        assertEquals(3, e.masRepetido());
    }

    @Test
    public void seQuedaConLosTresGenerosMasLeidos() {
        List<Libro> muchos = new ArrayList<>();
        for (String g : new String[]{"A", "B", "C", "D", "E"}) {
            muchos.add(libro(100, g));
        }
        assertEquals(3, Estadisticas.de(muchos).generos.size());
    }

    //La regla que evita ensenar un panel a medio llenar.
    @Test
    public void noHayBastanteConPocosLibros() {
        List<Libro> pocos = new ArrayList<>();
        for (int i = 0; i < Estadisticas.MINIMO_LIBROS - 1; i++) {
            pocos.add(libro(100 + i, "Fiction"));
        }
        assertFalse(Estadisticas.de(pocos).hayBastante());

        pocos.add(libro(999, "Fiction"));
        assertTrue(Estadisticas.de(pocos).hayBastante());
    }

    @Test
    public void aguantaLaListaVaciaYLaNula() {
        assertEquals(0, Estadisticas.de(null).libros);
        assertEquals(0, Estadisticas.de(new ArrayList<Libro>()).paginas);
        assertEquals(0, Estadisticas.de(null).masRepetido());
    }
}
