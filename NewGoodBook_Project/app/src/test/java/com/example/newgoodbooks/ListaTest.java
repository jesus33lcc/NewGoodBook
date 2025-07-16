package com.example.newgoodbooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaTest {

    private Map<String, Object> libroMapa(String id) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("titulo", "Libro " + id);
        m.put("autor", Arrays.asList("Autor"));
        m.put("numPag", 100);
        m.put("fechaPublicacion", "2020");
        m.put("generos", Arrays.asList("Fantasy"));
        m.put("descripcion", "descripcion");
        m.put("linkImg", "https://example.com/" + id + ".jpg");
        return m;
    }

    @Test
    public void desdeMapa_leeNombreYLibros() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("nombre", "Para el verano");
        doc.put("libros", Arrays.asList(libroMapa("a"), libroMapa("b")));

        Lista lista = Lista.desdeMapa("id-firestore", doc);

        assertEquals("id-firestore", lista.getId());
        assertEquals("Para el verano", lista.getNombre());
        assertEquals(2, lista.getLibros().size());
    }

    @Test
    public void desdeMapa_descartaLosLibrosIncompletos() {
        //un libro a medias no debe tumbar la lista entera: se ignora y el resto sigue
        Map<String, Object> roto = libroMapa("malo");
        roto.remove("titulo");

        Map<String, Object> doc = new HashMap<>();
        doc.put("nombre", "Mezcla");
        doc.put("libros", Arrays.asList(libroMapa("bueno"), roto));

        Lista lista = Lista.desdeMapa("id", doc);
        assertEquals(1, lista.getLibros().size());
        assertEquals("bueno", lista.getLibros().get(0).getId());
    }

    @Test
    public void desdeMapa_devuelveNullSinNombre() {
        assertNull(Lista.desdeMapa("id", new HashMap<String, Object>()));
        assertNull(Lista.desdeMapa("id", null));
    }

    @Test
    public void desdeMapa_toleraQueNoHayaLibros() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("nombre", "Vacia");
        Lista lista = Lista.desdeMapa("id", doc);
        assertTrue(lista.getLibros().isEmpty());
    }

    @Test
    public void lasListasFijasSeReconocenPorSuId() {
        //el icono y el permiso de borrado dependen de esto; antes se comparaba el
        //NOMBRE con cadenas magicas, que se rompia en cuanto alguien lo cambiaba
        assertTrue(new Lista(Lista.ID_FAVORITOS, Lista.NOMBRE_FAVORITOS, null).esImborrable());
        assertTrue(new Lista(Lista.ID_LEIDOS, Lista.NOMBRE_LEIDOS, null).esImborrable());
        assertFalse(new Lista("cualquier-id", "Mi lista", null).esImborrable());
    }

    @Test
    public void unaListaLlamadaComoLaFijaNoEsImborrable() {
        //si el usuario crea una lista llamada "Libros Favoritos", debe poder borrarla:
        //lo que manda es el id, no la etiqueta
        Lista impostora = new Lista("id-normal", Lista.NOMBRE_FAVORITOS, null);
        assertFalse(impostora.esImborrable());
    }

    @Test
    public void librosComoMapas_permiteGuardarYVolverALeer() {
        List<Libro> libros = new ArrayList<>();
        libros.add(Libro.desdeMapa(libroMapa("x")));
        Lista lista = new Lista("id", "Ida y vuelta", libros);

        Map<String, Object> doc = new HashMap<>();
        doc.put("nombre", lista.getNombre());
        doc.put("libros", lista.librosComoMapas());

        Lista recuperada = Lista.desdeMapa("id", doc);
        assertEquals(1, recuperada.getLibros().size());
        assertEquals("x", recuperada.getLibros().get(0).getId());
    }
}
