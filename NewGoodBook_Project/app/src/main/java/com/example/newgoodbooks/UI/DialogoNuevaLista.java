package com.example.newgoodbooks.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

//Crear una lista. Vive aqui porque se abre desde dos sitios: la pantalla de Listas y
//el dialogo de anadir a lista. Estaba solo en la primera, asi que la segunda no tenia
//forma de crear una y se limitaba a decir donde ir.
public final class DialogoNuevaLista {

    public interface AlCrear {
        void conNombre(String nombre);
    }

    private DialogoNuevaLista() {
    }

    public static void mostrar(Context contexto, AlCrear alCrear) {
        if (contexto == null || alCrear == null) {
            return;
        }
        View contenido = LayoutInflater.from(contexto)
                .inflate(R.layout.dialogo_nueva_lista, null);
        TextInputLayout capa = contenido.findViewById(R.id.capaNombreLista);
        TextInputEditText campo = contenido.findViewById(R.id.campoNombreLista);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(contexto)
                .setTitle(R.string.nueva_lista)
                .setView(contenido)
                .setPositiveButton(R.string.crear, null)
                .setNegativeButton(R.string.cancelar, null)
                .create();

        //El boton se engancha DESPUES de mostrar el dialogo: asi se puede validar sin
        //que se cierre, que es lo que hace que el error se pueda leer y corregir.
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombre = String.valueOf(campo.getText()).trim();
                    String error = validarNombre(contexto, nombre);
                    if (error != null) {
                        capa.setError(error);
                        return;
                    }
                    capa.setError(null);
                    alCrear.conNombre(nombre);
                    dialogo.dismiss();
                }));
        dialogo.show();
    }

    //Devuelve el motivo por el que el nombre no vale, o null si esta bien.
    public static String validarNombre(Context contexto, String nombre) {
        if (nombre.isEmpty()) {
            return contexto.getString(R.string.lista_sin_nombre);
        }
        if (nombre.length() > 40) {
            return contexto.getString(R.string.lista_nombre_largo);
        }
        //tambien contra los nombres traducidos: con la aplicacion en ingles se podia
        //crear una lista llamada igual que una de las fijas
        if (nombre.equalsIgnoreCase(Lista.NOMBRE_FAVORITOS)
                || nombre.equalsIgnoreCase(Lista.NOMBRE_LEIDOS)
                || nombre.equalsIgnoreCase(contexto.getString(R.string.lista_favoritos))
                || nombre.equalsIgnoreCase(contexto.getString(R.string.lista_leidos))) {
            return contexto.getString(R.string.lista_nombre_reservado);
        }
        for (String existente : RepositorioUsuario.get().getNombresListasPersonales()) {
            if (existente.equalsIgnoreCase(nombre)) {
                return contexto.getString(R.string.lista_nombre_existente);
            }
        }
        return null;
    }
}
