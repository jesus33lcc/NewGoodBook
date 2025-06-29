package com.example.newgoodbooks.Fragments.HomeIU;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.ManejoFicheros.AccesoFicheros;
import com.example.newgoodbooks.ManejoFicheros.Datos;
import com.example.newgoodbooks.Modelos.Libro;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends ViewModel {
    //cuantos libros intentamos tener en cola y a partir de cuantos volvemos a rellenar
    private static final int OBJETIVO_COLA = 20;
    private static final int MINIMO_COLA = 4;

    //volatile: se lee y escribe desde el hilo principal y desde el executor
    private volatile Libro libroMostrado;
    private final LinkedList<Libro> listaLibrosMostrar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean cargando;

    private final MutableLiveData<String> titulo = new MutableLiveData<>();
    private final MutableLiveData<String> autor = new MutableLiveData<>();
    private final MutableLiveData<String> numPag = new MutableLiveData<>();
    private final MutableLiveData<String> fechaPublicacion = new MutableLiveData<>();
    private final MutableLiveData<String> generos = new MutableLiveData<>();
    private final MutableLiveData<String> descripcion = new MutableLiveData<>();
    private final MutableLiveData<String> linkImagen = new MutableLiveData<>();
    private final MutableLiveData<Boolean> estadoTBtnFav = new MutableLiveData<>();
    private final MutableLiveData<Boolean> estadoTBtnCheck = new MutableLiveData<>();
    //false mientras no haya libro que mostrar: la pantalla usa esto para pasar a modo "Reintentar"
    private final MutableLiveData<Boolean> hayLibro = new MutableLiveData<>();

    public HomeViewModel() {
        listaLibrosMostrar = new LinkedList<>(Datos.DatosComunes.getListaRecomendar());
        libroMostrado = Datos.DatosComunes.getLibroRecomendar();
        //valor inicial sincrono: si no, al volver a la pestana el boton parpadea a "Reintentar"
        hayLibro.setValue(libroMostrado != null);
        if (libroMostrado != null) {
            cambiarVistaLibro();
        } else {
            mostrarMensaje("Cargando...", "Buscando recomendaciones para ti.");
        }
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

    //metodo que actualiza los datos del fragment por el libro seleccionado
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
        estadoTBtnFav.postValue(Datos.DatosComunes.getListasUsuario().getLibrosLike().contains(libro));
        estadoTBtnCheck.postValue(Datos.DatosComunes.getListasUsuario().getLibrosCheck().contains(libro));
        hayLibro.postValue(true);
    }

    //deja la pantalla en un estado informativo cuando no hay ningun libro que ensenar
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

    //pide libros a la api en segundo plano. si falla lo dice por pantalla, nunca tumba la app.
    public void cargarRecomendaciones(Context contexto) {
        if (cargando) {
            return;
        }
        cargando = true;
        final Context appContext = contexto.getApplicationContext();
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
                    guardar(appContext);
                }
            } finally {
                cargando = false;
            }
        });
    }

    //metodo que cambia el libro por el siguiente de la cola y la vuelve a rellenar por detras
    public void cambioLibro(Context contexto) {
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
            cargarRecomendaciones(contexto);
            return;
        }
        libroMostrado = siguiente;
        cambiarVistaLibro();

        final Context appContext = contexto.getApplicationContext();
        executor.execute(() -> {
            List<Libro> historial = Datos.DatosComunes.getHistorialLibros();
            historial.add(0, anterior);
            while (historial.size() > 10) {
                historial.remove(historial.size() - 1);
            }
            new AccesoFicheros(appContext).setHistorial(historial);

            int enCola;
            synchronized (listaLibrosMostrar) {
                enCola = listaLibrosMostrar.size();
            }
            if (enCola < MINIMO_COLA) {
                rellenarCola();
            }
            guardar(appContext);
        });
    }

    //rellena la cola con UNA sola llamada al servidor, que devuelve el lote entero.
    //(el diseno anterior gastaba una peticion a la API por cada libro)
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

    //persiste el estado en disco y tambien en memoria, para que al volver de otra pestana
    //la pantalla siga por el libro donde estaba
    private void guardar(Context appContext) {
        List<Libro> copia;
        synchronized (listaLibrosMostrar) {
            copia = new LinkedList<>(listaLibrosMostrar);
        }
        Datos.DatosComunes.setPrincipal(libroMostrado, copia);
        new AccesoFicheros(appContext).setPrincipal(libroMostrado, copia);
    }

    public void setEstadoTBtnFav(boolean bool) {
        estadoTBtnFav.setValue(bool);
    }

    public void setEstadoTBtnCheck(boolean bool) {
        estadoTBtnCheck.setValue(bool);
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
