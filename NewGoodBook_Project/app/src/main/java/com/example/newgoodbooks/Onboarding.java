package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityOnboardingBinding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Fragments.AdapterList.PortadaAdapter;
import com.example.newgoodbooks.Fragments.GustosViewModel;
import com.example.newgoodbooks.Modelos.Libro;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Eleccion de gustos al empezar. Existe porque el recomendador de la pantalla principal
//se alimenta de lo que el usuario ha marcado, y una cuenta recien creada no ha marcado
//nada: sin esto las primeras recomendaciones son puro azar.
//
//Los libros que se eligen aqui se guardan como FAVORITOS, no en un sitio aparte. Asi
//alimentan el mismo perfil que ya usa el servidor y no hay dos verdades que mantener.
public class Onboarding extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private static final int MINIMO_GENEROS = 3;
    private static final int LIBROS_A_ENSENAR = 18;

    private ChipGroup grupoGeneros;
    private View pasoGeneros;
    private View pasoLibros;
    private RecyclerView rejilla;
    private CircularProgressIndicator cargando;
    private TextView avisoSinLibros;
    private TextView tituloPaso;
    private TextView detallePaso;
    private MaterialButton btnContinuar;

    private PortadaAdapter adaptador;
    private GustosViewModel modelo;
    private final List<String> consultas = new ArrayList<>();
    private boolean enPasoLibros;

    private final RepositorioUsuario repo = RepositorioUsuario.get();

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        grupoGeneros = binding.grupoGeneros;
        pasoGeneros = binding.pasoGeneros;
        pasoLibros = binding.pasoLibros;
        rejilla = binding.rejillaLibros;
        cargando = binding.cargandoLibros;
        avisoSinLibros = binding.avisoSinLibros;
        tituloPaso = binding.tituloPaso;
        detallePaso = binding.detallePaso;
        btnContinuar = binding.btnContinuar;

        montarGeneros();
        //la rejilla se adapta al ancho: 3 columnas en movil, 5 en tablet
        rejilla.setLayoutManager(new GridLayoutManager(this, columnas()));

        modelo = new ViewModelProvider(this).get(GustosViewModel.class);
        modelo.getCargando().observe(this, hay ->
                cargando.setVisibility(Boolean.TRUE.equals(hay) ? View.VISIBLE : View.GONE));
        modelo.getLibros().observe(this, this::mostrarLibros);

        btnContinuar.setOnClickListener(v -> avanzar());
        binding.btnOmitir.setOnClickListener(v -> {
            repo.marcarOnboardingHecho();
            irAPrincipal();
        });
    }

    private int columnas() {
        int anchoDp = getResources().getConfiguration().screenWidthDp;
        if (anchoDp >= 840) {
            return 6;
        }
        return anchoDp >= 600 ? 5 : 3;
    }

    private void montarGeneros() {
        String[] nombres = getResources().getStringArray(R.array.generos_nombres);
        String[] terminos = getResources().getStringArray(R.array.generos_consultas);
        int cuantos = Math.min(nombres.length, terminos.length);
        for (int i = 0; i < cuantos; i++) {
            Chip chip = new Chip(this);
            chip.setText(nombres[i]);
            chip.setCheckable(true);
            chip.setTag(terminos[i]);
            grupoGeneros.addView(chip);
        }
    }

    private List<String> generosElegidos() {
        List<String> elegidos = new ArrayList<>();
        for (int i = 0; i < grupoGeneros.getChildCount(); i++) {
            View hijo = grupoGeneros.getChildAt(i);
            if (hijo instanceof Chip && ((Chip) hijo).isChecked()) {
                elegidos.add(String.valueOf(hijo.getTag()));
            }
        }
        return elegidos;
    }

    private void avanzar() {
        if (!enPasoLibros) {
            List<String> generos = generosElegidos();
            if (generos.size() < MINIMO_GENEROS) {
                Toast.makeText(this, R.string.onboarding_pide_generos, Toast.LENGTH_SHORT).show();
                return;
            }
            repo.guardarGenerosPreferidos(generos);
            consultas.clear();
            for (String genero : generos) {
                consultas.add("subject:\"" + genero + "\"");
            }
            pasarALibros();
            return;
        }
        //segundo paso: lo elegido pasa a favoritos y de ahi al perfil del recomendador
        if (adaptador != null) {
            for (Libro libro : adaptador.getElegidos()) {
                repo.anadirFavorito(libro);
            }
        }
        repo.marcarOnboardingHecho();
        irAPrincipal();
    }

    private void pasarALibros() {
        enPasoLibros = true;
        tituloPaso.setText(R.string.onboarding_titulo_libros);
        detallePaso.setText(R.string.onboarding_detalle_libros);
        btnContinuar.setText(R.string.onboarding_empezar);
        pasoGeneros.setVisibility(View.GONE);
        pasoLibros.setVisibility(View.VISIBLE);
        modelo.traerLibros(consultas);
    }

    private void mostrarLibros(List<Libro> libros) {
        if (libros == null || libros.isEmpty()) {
            //sin conexion no se bloquea el registro: se puede seguir sin elegir nada
            avisoSinLibros.setVisibility(
                    Boolean.TRUE.equals(modelo.getCargando().getValue()) ? View.GONE : View.VISIBLE);
            return;
        }
        avisoSinLibros.setVisibility(View.GONE);
        adaptador = new PortadaAdapter(this, libros);
        rejilla.setAdapter(adaptador);
    }

    private void irAPrincipal() {
        Intent intent = new Intent(this, Principal.class);
        //sin esto se puede volver atras al onboarding con el gesto de retroceso
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
