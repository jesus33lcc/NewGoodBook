package com.example.newgoodbooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.newgoodbooks.Modelos.Libro;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Libro es el modelo por el que pasa TODO: lo que devuelve la Cloud Function y lo que
//se guarda en Firestore usan las mismas claves, asi que si desdeMapa/aMapa se
//desincronizan, los datos guardados dejan de poder leerse.
public class LibroTest {

    private Map<String, Object> mapaCompleto() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", "abc123");
        m.put("titulo", "El nombre del viento");
        m.put("autor", Arrays.asList("Patrick Rothfuss"));
        m.put("numPag", 662);
        m.put("fechaPublicacion", "2007");
        m.put("generos", Arrays.asList("Fantasy"));
        m.put("descripcion", "Un libro de prueba");
        m.put("linkImg", "https://example.com/portada.jpg");
        return m;
    }

    @Test
    public void desdeMapa_construyeElLibroConTodosLosCampos() {
        Libro libro = Libro.desdeMapa(mapaCompleto());

        assertEquals("abc123", libro.getId());
        assertEquals("El nombre del viento", libro.getTitulo());
        assertEquals("Patrick Rothfuss", libro.getAutor().get(0));
        assertEquals(662, libro.getNumPag());
        assertEquals("Fantasy", libro.getGeneros().get(0));
    }

    @Test
    public void desdeMapa_devuelveNullSiFaltaAlgoImprescindible() {
        //sin estos campos la pantalla se quedaria a medias, asi que se descarta el libro
        for (String campo : new String[]{"id", "titulo", "linkImg"}) {
            Map<String, Object> incompleto = mapaCompleto();
            incompleto.remove(campo);
            assertNull("deberia descartarse al faltar " + campo, Libro.desdeMapa(incompleto));
        }
    }

    @Test
    public void desdeMapa_devuelveNullSinAutorNiGeneros() {
        Map<String, Object> sinAutor = mapaCompleto();
        sinAutor.put("autor", new ArrayList<String>());
        assertNull(Libro.desdeMapa(sinAutor));

        Map<String, Object> sinGeneros = mapaCompleto();
        sinGeneros.put("generos", new ArrayList<String>());
        assertNull(Libro.desdeMapa(sinGeneros));
    }

    @Test
    public void desdeMapa_toleraNumeroDePaginasComoDecimal() {
        //Firestore devuelve los numeros como Long o Double segun de donde vengan
        Map<String, Object> m = mapaCompleto();
        m.put("numPag", 662.0d);
        assertEquals(662, Libro.desdeMapa(m).getNumPag());
    }

    @Test
    public void desdeMapa_devuelveNullConMapaNulo() {
        assertNull(Libro.desdeMapa(null));
    }

    @Test
    public void aMapa_y_desdeMapa_son_simetricos() {
        //esta es la garantia importante: lo que se guarda se puede volver a leer
        Libro original = Libro.desdeMapa(mapaCompleto());
        Libro reconstruido = Libro.desdeMapa(original.aMapa());

        assertEquals(original.getId(), reconstruido.getId());
        assertEquals(original.getTitulo(), reconstruido.getTitulo());
        assertEquals(original.getNumPag(), reconstruido.getNumPag());
        assertEquals(original.getAutor(), reconstruido.getAutor());
        assertEquals(original.getGeneros(), reconstruido.getGeneros());
        assertEquals(original.getDescripcion(), reconstruido.getDescripcion());
        assertEquals(original.getLinkImg(), reconstruido.getLinkImg());
    }

    @Test
    public void dosLibrosConElMismoIdSonIguales() {
        //equals compara SOLO por id, y de eso depende que quitar un libro de una lista
        //funcione aunque el objeto no sea la misma instancia
        Libro a = Libro.desdeMapa(mapaCompleto());
        Map<String, Object> otro = mapaCompleto();
        otro.put("titulo", "Titulo distinto");
        Libro b = Libro.desdeMapa(otro);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        List<Libro> lista = new ArrayList<>();
        lista.add(a);
        assertTrue("remove debe encontrarlo aunque sea otra instancia", lista.remove(b));
    }

    @Test
    public void librosConIdDistintoNoSonIguales() {
        Libro a = Libro.desdeMapa(mapaCompleto());
        Map<String, Object> otro = mapaCompleto();
        otro.put("id", "xyz789");
        assertNotEquals(a, Libro.desdeMapa(otro));
    }
}
