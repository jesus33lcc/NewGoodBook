package com.example.newgoodbooks.Fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Fragments.AdapterList.AutorAdapter;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Estado de la eleccion de gustos. Sin esto, girar la pantalla en el segundo paso
//tiraba los libros ya traidos y volvia a pedirlos: entre tres y cuatro peticiones de
//red repetidas, y el usuario perdia lo que ya hubiera marcado.
public class GustosViewModel extends ViewModel {
    private static final int POR_GENERO = 6;
    private static final int TOPE = 18;
    private static final int MAX_AUTORES = 12;

    private final MutableLiveData<List<Libro>> libros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<List<AutorAdapter.Autor>> autores = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean yaPedidos;

    public LiveData<List<Libro>> getLibros() {
        return libros;
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<List<AutorAdapter.Autor>> getAutores() {
        return autores;
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
            autores.postValue(deducirAutores(traidos));
            cargando.postValue(false);
        });
    }

    //Los autores salen de los libros que ya se han traido, no de otra peticion: Google
    //Books no tiene API de autores y pedir aparte seria doblar la red por nada.
    //Se ordenan por cuantos libros aportan, que es la mejor senial de relevancia
    //que hay aqui, y cada uno se queda con una portada suya para ilustrarlo.
    private static List<AutorAdapter.Autor> deducirAutores(List<Libro> libros) {
        Map<String, Integer> cuenta = new LinkedHashMap<>();
        Map<String, String> portada = new LinkedHashMap<>();
        for (Libro libro : libros) {
            if (libro.getAutor() == null || libro.getAutor().isEmpty()) {
                continue;
            }
            String nombre = libro.getAutor().get(0).trim();
            if (nombre.isEmpty()) {
                continue;
            }
            cuenta.put(nombre, (cuenta.get(nombre) == null ? 0 : cuenta.get(nombre)) + 1);
            if (!portada.containsKey(nombre)) {
                portada.put(nombre, libro.getLinkImg());
            }
        }
        List<Map.Entry<String, Integer>> ordenados = new ArrayList<>(cuenta.entrySet());
        Collections.sort(ordenados, (a, b) -> b.getValue() - a.getValue());

        List<AutorAdapter.Autor> salida = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ordenados) {
            if (salida.size() >= MAX_AUTORES) {
                break;
            }
            salida.add(new AutorAdapter.Autor(e.getKey(), portada.get(e.getKey())));
        }
        return salida;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
