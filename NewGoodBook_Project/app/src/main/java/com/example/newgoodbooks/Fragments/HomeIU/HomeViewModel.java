package com.example.newgoodbooks.Fragments.HomeIU;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Este ViewModel se pide con scope de Activity (ver HomeFragment), asi que la cola de
//recomendaciones sobrevive al cambio de pestania y no hay que ir al servidor cada vez.
public class HomeViewModel extends ViewModel {
    //cuantos libros intentamos tener en cola y a partir de cuantos volvemos a rellenar
    private static final int OBJETIVO_COLA = 20;
    private static final int MINIMO_COLA = 4;

    private final RepositorioUsuario repo = RepositorioUsuario.get();

    //volatile: se lee y escribe desde el hilo principal y desde el executor
    private volatile Libro libroMostrado;
    private final LinkedList<Libro> listaLibrosMostrar = new LinkedList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean cargando;

    private final MutableLiveData<String> titulo = new MutableLiveData<>();
    private final MutableLiveData<String> autor = new MutableLiveData<>();
    private final MutableLiveData<String> numPag = new MutableLiveData<>();
    private final MutableLiveData<String> fechaPublicacion = new MutableLiveData<>();
    private final MutableLiveData<String> generos = new MutableLiveData<>();
    private final MutableLiveData<String> descripcion = new MutableLiveData<>();
    private final MutableLiveData<String> linkImagen = new MutableLiveData<>();
    //false mientras no haya libro que mostrar: la pantalla pasa a modo "Reintentar"
    private final MutableLiveData<Boolean> hayLibro = new MutableLiveData<>(false);

    //Los dos toggles se derivan de Firestore: si marcas un libro en el movil,
    //la tablet se entera sola. No hay que refrescarlos a mano en ningun sitio.
    private final MediatorLiveData<Boolean> estadoTBtnFav = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> estadoTBtnCheck = new MediatorLiveData<>();

    public HomeViewModel() {
        estadoTBtnFav.addSource(repo.getFavoritos(), libros -> estadoTBtnFav.setValue(
                libroMostrado != null && libros != null && libros.contains(libroMostrado)));
        estadoTBtnCheck.addSource(repo.getLeidos(), libros -> estadoTBtnCheck.setValue(
                libroMostrado != null && libros != null && libros.contains(libroMostrado)));
        mostrarMensaje("Cargando...", "Buscando recomendaciones para ti.");
    }

    public LiveData<String> getTitulo() {
        return titulo;
    }
    public LiveData<String> getAutor() {
        return autor;
    }
    public LiveData<String> getNumPag() {
        return numPag;
    }
    public LiveData<String> getFechaPublicacion() {
        return fechaPublicacion;
    }
    public LiveData<String> getGeneros() {
        return generos;
    }
    public LiveData<String> getDescripcion() {
        return descripcion;
    }
    public LiveData<String> getLinkImagen() {
        return linkImagen;
    }
    public LiveData<Boolean> getEstadoTBtnFav() {
        return estadoTBtnFav;
    }
    public LiveData<Boolean> getEstadoTBtnCheck() {
        return estadoTBtnCheck;
    }
    public LiveData<Boolean> getHayLibro() {
        return hayLibro;
    }

    //vuelca en pantalla el libro que toca
    public void cambiarVistaLibro() {
        Libro libro = libroMostrado;
        if (libro == null) {
            return;
        }
        titulo.postValue(libro.getTitulo());
        autor.postValue(primero(libro.getAutor()));
        numPag.postValue(String.valueOf(libro.getNumPag()));
        fechaPublicacion.postValue(libro.getFechaPublicacion());
        generos.postValue(primero(libro.getGeneros()));
        descripcion.postValue(libro.getDescripcion());
        linkImagen.postValue(libro.getLinkImg());
        estadoTBtnFav.postValue(repo.esFavorito(libro));
        estadoTBtnCheck.postValue(repo.esLeido(libro));
        hayLibro.postValue(true);
    }

    //estado informativo cuando no hay ningun libro que ensenar
    private void mostrarMensaje(String tit, String texto) {
        titulo.postValue(tit);
        descripcion.postValue(texto);
        autor.postValue("");
        numPag.postValue("");
        fechaPublicacion.postValue("");
        generos.postValue("");
        linkImagen.postValue(null);
        estadoTBtnFav.postValue(false);
        estadoTBtnCheck.postValue(false);
        hayLibro.postValue(false);
    }

    private static String primero(List<String> lista) {
        return (lista == null || lista.isEmpty()) ? "" : lista.get(0);
    }

    //pide libros al servidor en segundo plano; si falla lo dice por pantalla
    public void cargarRecomendaciones() {
        if (cargando) {
            return;
        }
        cargando = true;
        if (libroMostrado == null) {
            mostrarMensaje("Cargando...", "Buscando recomendaciones para ti.");
        }
        executor.execute(() -> {
            try {
                rellenarCola();
                if (libroMostrado == null) {
                    synchronized (listaLibrosMostrar) {
                        libroMostrado = listaLibrosMostrar.poll();
                    }
                }
                if (libroMostrado == null) {
                    mostrarMensaje("Sin conexion",
                            "No se han podido cargar libros. Comprueba tu conexion y pulsa Reintentar.");
                } else {
                    cambiarVistaLibro();
                }
            } finally {
                cargando = false;
            }
        });
    }

    //pasa al siguiente libro de la cola y la rellena por detras
    public void cambioLibro() {
        final Libro anterior = libroMostrado;
        Libro siguiente;
        synchronized (listaLibrosMostrar) {
            siguiente = listaLibrosMostrar.poll();
        }
        //sin libro actual o sin cola, el boton hace de "Reintentar"
        if (anterior == null || siguiente == null) {
            if (siguiente != null) {
                libroMostrado = siguiente;
                cambiarVistaLibro();
            }
            cargarRecomendaciones();
            return;
        }
        libroMostrado = siguiente;
        cambiarVistaLibro();
        //el libro que se deja atras pasa al historial (pestania Explorar)
        repo.registrarVisita(anterior);

        executor.execute(() -> {
            int enCola;
            synchronized (listaLibrosMostrar) {
                enCola = listaLibrosMostrar.size();
            }
            if (enCola < MINIMO_COLA) {
                rellenarCola();
            }
        });
    }

    //rellena la cola con UNA sola llamada al servidor, que devuelve el lote entero
    private void rellenarCola() {
        int faltan;
        synchronized (listaLibrosMostrar) {
            faltan = OBJETIVO_COLA - listaLibrosMostrar.size();
        }
        if (faltan <= 0) {
            return;
        }
        List<Libro> nuevos = ClienteFunciones.librosAleatorios(faltan);
        if (nuevos.isEmpty()) {
            return;
        }
        synchronized (listaLibrosMostrar) {
            for (Libro libro : nuevos) {
                //Libro compara por id, asi evitamos repetir el que ya se ve o los de la cola
                if (!libro.equals(libroMostrado) && !listaLibrosMostrar.contains(libro)) {
                    listaLibrosMostrar.add(libro);
                }
            }
        }
    }

    public void alternarFavorito() {
        if (libroMostrado != null) {
            repo.alternarFavorito(libroMostrado);
        }
    }

    public void alternarLeido() {
        if (libroMostrado != null) {
            repo.alternarLeido(libroMostrado);
        }
    }

    public Libro getLibroMostrado() {
        return libroMostrado;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
