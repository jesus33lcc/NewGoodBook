package com.example.newgoodbooks.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;

//Acciones sobre un libro que comparten la pantalla Home y la ficha del libro.
//Antes estaban copiadas literalmente en ambas, asi que cualquier cambio habia que
//hacerlo dos veces o se desincronizaban.
public final class AccionesLibro {

    private AccionesLibro() {
    }

    //Dialogo para elegir a que lista personalizada se anade el libro
    public static void mostrarDialogoAnadirALista(Context contexto, Libro libro) {
        if (contexto == null || libro == null) {
            return;
        }
        final RepositorioUsuario repo = RepositorioUsuario.get();
        final String[] nombres = repo.getNombresListasPersonales();

        if (nombres.length == 0) {
            Toast.makeText(contexto, contexto.getString(R.string.sin_listas),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(contexto)
                .setTitle(contexto.getString(R.string.anadir_a_lista))
                .setIcon(R.drawable.ic_listas)
                .setSingleChoiceItems(nombres, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int indice) {
                        dialogo.dismiss();
                        anadirALista(contexto, repo, indice, libro);
                    }
                })
                .setNeutralButton(contexto.getString(R.string.cancelar), null)
                .show();
    }

    private static void anadirALista(Context contexto, RepositorioUsuario repo,
                                     int indice, Libro libro) {
        Lista destino = repo.getListaPersonalPorIndice(indice);
        if (destino == null) {
            return;
        }
        if (destino.getLibros().contains(libro)) {
            Toast.makeText(contexto, contexto.getString(R.string.libro_ya_en_lista, destino.getNombre()),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        repo.anadirLibroALista(destino.getId(), libro);
        Toast.makeText(contexto, contexto.getString(R.string.libro_anadido, destino.getNombre()),
                Toast.LENGTH_SHORT).show();
    }
}
