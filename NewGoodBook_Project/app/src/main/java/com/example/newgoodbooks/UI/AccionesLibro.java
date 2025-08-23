package com.example.newgoodbooks.UI;

import android.content.Context;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

//Acciones sobre un libro que comparten la pantalla Home y la ficha del libro.
//Antes estaban copiadas literalmente en ambas, asi que cualquier cambio habia que
//hacerlo dos veces o se desincronizaban.
public final class AccionesLibro {

    private AccionesLibro() {
    }

    //Dialogo para decidir en que listas esta el libro.
    //
    //Es de seleccion multiple y viene marcado con las listas que YA lo contienen, que
    //es la pregunta que se hace uno al abrirlo. Antes era de seleccion unica y sin
    //marcar: no se veia donde estaba el libro, anadirlo a dos listas obligaba a abrir
    //el dialogo dos veces, y quitarlo de una habia que ir a buscarlo a la lista.
    public static void mostrarDialogoAnadirALista(Context contexto, Libro libro) {
        if (contexto == null || libro == null) {
            return;
        }
        final RepositorioUsuario repo = RepositorioUsuario.get();
        final List<Lista> listas = repo.getListasPersonales();

        //Sin ninguna lista el dialogo no tendria nada que ensenar. Se abre directamente
        //la creacion, con el libro dentro; antes salia un aviso que solo decia a donde ir.
        if (listas.isEmpty()) {
            DialogoNuevaLista.mostrar(contexto,
                    nombre -> crearConLibro(contexto, repo, nombre, libro));
            return;
        }

        final String[] nombres = new String[listas.size()];
        final boolean[] estaba = new boolean[listas.size()];
        final boolean[] marcado = new boolean[listas.size()];
        for (int i = 0; i < listas.size(); i++) {
            nombres[i] = listas.get(i).getNombreVisible(contexto);
            estaba[i] = listas.get(i).getLibros().contains(libro);
            marcado[i] = estaba[i];
        }

        new MaterialAlertDialogBuilder(contexto)
                .setTitle(R.string.anadir_a_lista)
                //nada se guarda hasta "Listo", asi que un toque por error se deshace
                //cerrando el dialogo
                .setMultiChoiceItems(nombres, marcado,
                        (dialogo, indice, activo) -> marcado[indice] = activo)
                .setPositiveButton(R.string.listo, (dialogo, boton) ->
                        aplicar(contexto, repo, listas, estaba, marcado, libro))
                .setNeutralButton(R.string.nueva_lista, (dialogo, boton) ->
                        DialogoNuevaLista.mostrar(contexto,
                                nombre -> crearConLibro(contexto, repo, nombre, libro)))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    //Guarda solo lo que ha cambiado de verdad. Escribir las marcadas sin comparar
    //reescribiria en Firestore listas que nadie ha tocado.
    private static void aplicar(Context contexto, RepositorioUsuario repo,
                                List<Lista> listas, boolean[] estaba, boolean[] marcado,
                                Libro libro) {
        int anadidas = 0;
        int quitadas = 0;
        String ultima = null;
        for (int i = 0; i < listas.size(); i++) {
            if (marcado[i] == estaba[i]) {
                continue;
            }
            Lista lista = listas.get(i);
            ultima = lista.getNombreVisible(contexto);
            if (marcado[i]) {
                repo.anadirLibroALista(lista.getId(), libro);
                anadidas++;
            } else {
                repo.quitarLibroDeLista(lista.getId(), libro);
                quitadas++;
            }
        }
        avisar(contexto, anadidas, quitadas, ultima);
    }

    //Un solo cambio se nombra; varios a la vez se resumen, porque encadenar avisos
    //deja al ultimo tapando a los anteriores.
    private static void avisar(Context contexto, int anadidas, int quitadas, String ultima) {
        int total = anadidas + quitadas;
        if (total == 0) {
            return;
        }
        String texto;
        if (total == 1) {
            texto = contexto.getString(
                    anadidas == 1 ? R.string.libro_anadido : R.string.libro_quitado, ultima);
        } else {
            texto = contexto.getString(R.string.listas_actualizadas, total);
        }
        Toast.makeText(contexto, texto, Toast.LENGTH_SHORT).show();
    }

    private static void crearConLibro(Context contexto, RepositorioUsuario repo,
                                      String nombre, Libro libro) {
        repo.crearLista(nombre, libro);
        Toast.makeText(contexto, contexto.getString(R.string.libro_anadido, nombre),
                Toast.LENGTH_SHORT).show();
    }
}
