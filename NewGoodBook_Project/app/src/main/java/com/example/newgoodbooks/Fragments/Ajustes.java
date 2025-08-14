package com.example.newgoodbooks.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import android.content.pm.PackageManager;

import android.widget.Toast;

import com.example.newgoodbooks.App;
import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Inicio;
import com.example.newgoodbooks.Onboarding;
import com.example.newgoodbooks.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

//Pestania de ajustes. Ocupa el sitio de la antigua pestania de cerrar sesion, que
//cerraba la sesion nada mas rozarla. Aqui salir es una accion deliberada y con
//confirmacion, y ademas hay sitio para el tema, el idioma y los creditos.
public class Ajustes extends Fragment {

    //el orden de los tres modos tiene que coincidir con el de las etiquetas del dialogo
    private static final int[] MODOS_TEMA = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
    };
    //cadena vacia = seguir al sistema, que es como lo representa LocaleListCompat
    private static final String[] IDIOMAS = {"", "es", "en"};

    private View filaTema;
    private View filaIdioma;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup contenedor, Bundle estado) {
        return inflater.inflate(R.layout.fragment_ajustes, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);

        pintarCuenta(vista);

        filaTema = vista.findViewById(R.id.filaTema);
        filaIdioma = vista.findViewById(R.id.filaIdioma);
        filaTema.setOnClickListener(v -> elegirTema());
        filaIdioma.setOnClickListener(v -> elegirIdioma());
        refrescarApariencia();

        //informativas: no se pulsan, solo cuentan de que esta hecha la aplicacion
        rellenar(vista.findViewById(R.id.filaVersion),
                getString(R.string.acerca_version), versionInstalada());
        rellenar(vista.findViewById(R.id.filaCreditos),
                getString(R.string.acerca_hecha_por), getString(R.string.acerca_hecha_por_detalle));
        rellenar(vista.findViewById(R.id.filaLicencia),
                getString(R.string.acerca_licencia), getString(R.string.acerca_licencia_detalle));
        rellenar(vista.findViewById(R.id.filaDatos),
                getString(R.string.acerca_datos), getString(R.string.acerca_datos_detalle));

        //Rehacer la eleccion de gustos: ademas de util, es la unica forma de llegar al
        //onboarding cuando la cuenta ya existe.
        View filaGustos = vista.findViewById(R.id.filaGustos);
        rellenar(filaGustos, getString(R.string.ajuste_gustos),
                getString(R.string.ajuste_gustos_detalle));
        filaGustos.setOnClickListener(v -> startActivity(
                new android.content.Intent(requireContext(), Onboarding.class)));

        vista.findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> confirmarCierre());
        vista.findViewById(R.id.btnBorrarCuenta).setOnClickListener(v -> confirmarBorradoCuenta());
    }

    private void pintarCuenta(View vista) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        TextView nombre = vista.findViewById(R.id.nombreCuenta);
        TextView correo = vista.findViewById(R.id.correoCuenta);
        if (usuario == null) {
            nombre.setText(R.string.sesion_sin_nombre);
            correo.setText("");
            return;
        }
        String suNombre = usuario.getDisplayName();
        nombre.setText(suNombre == null || suNombre.trim().isEmpty()
                ? getString(R.string.sesion_sin_nombre) : suNombre);
        correo.setText(usuario.getEmail() == null ? "" : usuario.getEmail());
    }

    //Del PackageManager y no de BuildConfig, que en AGP 8 no se genera salvo que se
    //active a proposito; asi el numero sale del APK instalado y no hay que tocar Gradle.
    private String versionInstalada() {
        try {
            String version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            return version == null ? "" : version;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private static void rellenar(View fila, String titulo, String valor) {
        ((TextView) fila.findViewById(R.id.tituloAjuste)).setText(titulo);
        ((TextView) fila.findViewById(R.id.valorAjuste)).setText(valor);
    }

    //deja las dos filas mostrando lo que hay elegido ahora mismo
    private void refrescarApariencia() {
        String[] temas = {getString(R.string.tema_sistema), getString(R.string.tema_claro),
                getString(R.string.tema_oscuro)};
        rellenar(filaTema, getString(R.string.ajuste_tema), temas[indiceTema()]);

        String[] idiomas = {getString(R.string.idioma_sistema), getString(R.string.idioma_es),
                getString(R.string.idioma_en)};
        rellenar(filaIdioma, getString(R.string.ajuste_idioma), idiomas[indiceIdioma()]);
    }

    private int indiceTema() {
        int guardado = App.temaGuardado(requireContext());
        for (int i = 0; i < MODOS_TEMA.length; i++) {
            if (MODOS_TEMA[i] == guardado) {
                return i;
            }
        }
        return 0;
    }

    private int indiceIdioma() {
        LocaleListCompat elegido = AppCompatDelegate.getApplicationLocales();
        if (elegido.isEmpty()) {
            return 0;
        }
        String etiqueta = elegido.get(0).getLanguage();
        for (int i = 1; i < IDIOMAS.length; i++) {
            if (IDIOMAS[i].equals(etiqueta)) {
                return i;
            }
        }
        return 0;
    }

    private void elegirTema() {
        String[] opciones = {getString(R.string.tema_sistema), getString(R.string.tema_claro),
                getString(R.string.tema_oscuro)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ajuste_tema)
                .setSingleChoiceItems(opciones, indiceTema(), (dialogo, cual) -> {
                    dialogo.dismiss();
                    //recrea las actividades por su cuenta; no hay que reiniciar nada a mano
                    App.guardarTema(requireContext(), MODOS_TEMA[cual]);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void elegirIdioma() {
        String[] opciones = {getString(R.string.idioma_sistema), getString(R.string.idioma_es),
                getString(R.string.idioma_en)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ajuste_idioma)
                .setSingleChoiceItems(opciones, indiceIdioma(), (dialogo, cual) -> {
                    dialogo.dismiss();
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(IDIOMAS[cual]));
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void confirmarCierre() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_cerrar_sesion)
                .setMessage(R.string.cerrar_sesion_mensaje)
                .setPositiveButton(R.string.title_cerrar_sesion, (dialogo, boton) -> cerrarSesion())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    //Play exige poder borrar la cuenta desde la propia aplicacion. Se avisa de todo lo
    //que se pierde y se pide una segunda confirmacion explicita: no es un boton mas.
    private void confirmarBorradoCuenta() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ajuste_borrar_cuenta)
                .setMessage(R.string.borrar_cuenta_aviso)
                .setPositiveButton(R.string.borrar_cuenta_confirmar, (d, w) -> borrarCuenta())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void borrarCuenta() {
        Toast.makeText(requireContext(), R.string.borrando_cuenta, Toast.LENGTH_SHORT).show();
        //El borrado lo hace el servidor: el movil no puede vaciar subcolecciones ni
        //borrar el usuario si la sesion no es reciente.
        new Thread(() -> {
            final boolean hecho = ClienteFunciones.borrarCuenta();
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!hecho) {
                    Toast.makeText(requireContext(), R.string.borrar_cuenta_ko,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(requireContext(), R.string.borrar_cuenta_ok,
                        Toast.LENGTH_LONG).show();
                //la cuenta ya no existe: solo queda soltar la sesion local y salir
                salirDeLaSesion();
            });
        }).start();
    }

    private void cerrarSesion() {
        if (!isAdded()) {
            return;
        }
        salirDeLaSesion();
    }

    //Suelta escuchas, sesion y cache local, y vuelve a la pantalla de bienvenida.
    //Lo comparten cerrar sesion y borrar la cuenta.
    private void salirDeLaSesion() {
        //los datos viven en Firestore bajo el uid, asi que ya no se mezclan entre cuentas.
        //Aun asi hay que soltar las escuchas y vaciar la cache local del dispositivo.
        RepositorioUsuario.get().desconectar();
        FirebaseAuth.getInstance().signOut();
        FirebaseFirestore.getInstance().clearPersistence();

        Intent intent = new Intent(requireContext(), Inicio.class);
        //vacia la pila: sin esto se puede volver atras a la app ya sin sesion
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
