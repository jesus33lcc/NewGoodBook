package com.example.newgoodbooks.Fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Estado de la eleccion de gustos. Sin esto, girar la pantalla en el segundo paso
//tiraba los libros ya traidos y volvia a pedirlos: entre tres y cuatro peticiones de
//red repetidas, y el usuario perdia lo que ya hubiera marcado.
public class GustosViewModel extends ViewModel {
    private static final int POR_GENERO = 6;
    private static final int TOPE = 18;

    private final MutableLiveData<List<Libro>> libros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean yaPedidos;

    public LiveData<List<Libro>> getLibros() {
        return libros;
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    //Una consulta por genero elegido, para que la rejilla no salga toda del mismo.
    //Idempotente: al recrearse la pantalla no se vuelve a pedir nada.
    public void traerLibros(List<String> consultas) {
        if (yaPedidos) {
            return;
        }
        yaPedidos = true;
        cargando.setValue(true);
        executor.execute(() -> {
            List<Libro> traidos = new ArrayList<>();
            for (String consulta : consultas) {
                if (traidos.size() >= TOPE) {
                    break;
                }
                for (Libro libro : ClienteFunciones.librosAleatorios(POR_GENERO, consulta)) {
                    if (!traidos.contains(libro)) {
                        traidos.add(libro);
                    }
                }
            }
            libros.postValue(traidos);
            cargando.postValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
