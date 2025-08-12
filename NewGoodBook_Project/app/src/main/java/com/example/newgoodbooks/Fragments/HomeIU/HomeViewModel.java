package com.example.newgoodbooks.Fragments.HomeIU;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newgoodbooks.Cliente.ClienteFunciones;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Este ViewModel se pide con scope de Activity (ver HomeFragment), asi que la cola de
//recomendaciones sobrevive al cambio de pestania y no hay que ir al servidor cada vez.
public class HomeViewModel extends AndroidViewModel {
    //cuantos libros intentamos tener en cola y a partir de cuantos volvemos a rellenar
    private static final int OBJETIVO_COLA = 20;
    private static final int MINIMO_COLA = 4;

    private final RepositorioUsuario repo = RepositorioUsuario.get();

    //volatile: se lee y escribe desde el hilo principal y desde el executor
    private volatile Libro libroMostrado;
    private final LinkedList<Libro> listaLibrosMostrar = new LinkedList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean cargando;
    //el arranque solo se resuelve una vez por ViewModel, aunque el fragment se recree
    private boolean arranqueResuelto;

    private final MutableLiveData<String> titulo = new MutableLiveData<>();
    private final MutableLiveData<String> autor = new MutableLiveData<>();
    private final MutableLiveData<String> numPag = new MutableLiveData<>();
    private final MutableLiveData<String> fechaPublicacion = new MutableLiveData<>();
    private final MutableLiveData<String> generos = new MutableLiveData<>();
    private final MutableLiveData<String> descripcion = new MutableLiveData<>();
    private final MutableLiveData<String> linkImagen = new MutableLiveData<>();
    //false mientras no haya libro que mostrar: la pantalla pasa a modo "Reintentar"
    private final MutableLiveData<Boolean> hayLibro = new MutableLiveData<>(false);
    //Mientras esta a true la pantalla no debe ofrecer "Reintentar": todavia esta buscando
    private final MutableLiveData<Boolean> estaCargando = new MutableLiveData<>(false);
    //El libro entero, no solo sus textos: la valoracion se pinta con el mismo ayudante
    //que la ficha, y ese recibe un Libro. Null cuando no hay nada que ensenar.
    private final MutableLiveData<Libro> libroEnPantalla = new MutableLiveData<>();

    //Los dos toggles se derivan de Firestore: si marcas un libro en el movil,
    //la tablet se entera sola. No hay que refrescarlos a mano en ningun sitio.
    private final MediatorLiveData<Boolean> estadoTBtnFav = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> estadoTBtnCheck = new MediatorLiveData<>();

    //AndroidViewModel para poder resolver los textos de estado desde strings.xml
    public HomeViewModel(@NonNull Application application) {
        super(application);
        estadoTBtnFav.addSource(repo.getFavoritos(), libros -> estadoTBtnFav.setValue(
                libroMostrado != null && libros != null && libros.contains(libroMostrado)));
        estadoTBtnCheck.addSource(repo.getLeidos(), libros -> estadoTBtnCheck.setValue(
                libroMostrado != null && libros != null && libros.contains(libroMostrado)));
        mostrarMensaje(texto(R.string.cargando), texto(R.string.cargando_detalle));
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

    public LiveData<Boolean> getEstaCargando() {
        return estaCargando;
    }

    public LiveData<Libro> getLibroEnPantalla() {
        return libroEnPantalla;
    }

    //vuelca en pantalla el libro que toca
    public void cambiarVistaLibro() {
        Libro libro = libroMostrado;
        if (libro == null) {
            return;
        }
        titulo.postValue(libro.getTitulo());
        autor.postValue(primero(libro.getAutor()));
        numPag.postValue(texto(R.string.formato_paginas, String.valueOf(libro.getNumPag())));
        fechaPublicacion.postValue(soloAnio(libro.getFechaPublicacion()));
        generos.postValue(primero(libro.getGeneros()));
        descripcion.postValue(libro.getDescripcion());
        linkImagen.postValue(libro.getLinkImg());
        estadoTBtnFav.postValue(repo.esFavorito(libro));
        estadoTBtnCheck.postValue(repo.esLeido(libro));
        libroEnPantalla.postValue(libro);
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
        libroEnPantalla.postValue(null);
        hayLibro.postValue(false);
    }

    private String texto(int idRecurso) {
        return getApplication().getString(idRecurso);
    }

    private String texto(int idRecurso, Object... args) {
        return getApplication().getString(idRecurso, args);
    }

    //Google devuelve la fecha como "2009-10-13" o como "2004". En la ficha solo
    //interesa el anio: la fecha completa no aporta y descuadra la pildora.
    private static String soloAnio(String fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.length() >= 4 ? fecha.substring(0, 4) : fecha;
    }

    private static String primero(List<String> lista) {
        return (lista == null || lista.isEmpty()) ? "" : lista.get(0);
    }

    //Punto de entrada al abrir Principal. Primero se mira si el usuario ya tenia un
    //libro recomendado y se le devuelve ese: solo se pide uno nuevo si no lo hay.
    //Antes se llamaba directamente a cargarRecomendaciones(), asi que cada arranque
    //estrenaba libro y el anterior se perdia sin haberlo llegado a decidir.
    public void restaurarOCargar() {
        if (arranqueResuelto) {
            return;
        }
        arranqueResuelto = true;
        if (libroMostrado != null) {
            cambiarVistaLibro();
            return;
        }
        estaCargando.postValue(true);
        repo.leerLibroActual(libro -> {
            if (libro == null) {
                //primera vez con esta cuenta: no hay nada guardado
                estaCargando.postValue(false);
                cargarRecomendaciones();
                return;
            }
            libroMostrado = libro;
            cambiarVistaLibro();
            estaCargando.postValue(false);
            //la cola se rellena por detras, sin tocar lo que se esta viendo
            executor.execute(this::rellenarCola);
        });
    }

    //pide libros al servidor en segundo plano; si falla lo dice por pantalla
    public void cargarRecomendaciones() {
        if (cargando) {
            return;
        }
        cargando = true;
        estaCargando.postValue(true);
        if (libroMostrado == null) {
            mostrarMensaje(texto(R.string.cargando), texto(R.string.cargando_detalle));
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
                    mostrarMensaje(texto(R.string.sin_conexion), texto(R.string.sin_conexion_detalle));
                } else {
                    cambiarVistaLibro();
                    fijarComoActual(libroMostrado);
                }
            } finally {
                cargando = false;
                estaCargando.postValue(false);
            }
        });
    }

    //pasa al siguiente libro de la cola y la rellena por detras
    public void cambioLibro() {
        Libro siguiente;
        synchronized (listaLibrosMostrar) {
            siguiente = listaLibrosMostrar.poll();
        }
        if (siguiente != null) {
            avanzarA(siguiente);
            executor.execute(() -> {
                int enCola;
                synchronized (listaLibrosMostrar) {
                    enCola = listaLibrosMostrar.size();
                }
                if (enCola < MINIMO_COLA) {
                    rellenarCola();
                }
            });
            return;
        }
        //Cola vacia: se pide un lote y se avanza en cuanto llega. Antes esto solo
        //rellenaba la cola y dejaba el mismo libro en pantalla, asi que el boton no
        //hacia nada visible y parecia roto. Se nota sobre todo nada mas abrir, que es
        //cuando la cola todavia se esta llenando por detras.
        if (cargando) {
            return;
        }
        cargando = true;
        estaCargando.postValue(true);
        executor.execute(() -> {
            try {
                rellenarCola();
                Libro nuevo;
                synchronized (listaLibrosMostrar) {
                    nuevo = listaLibrosMostrar.poll();
                }
                if (nuevo != null) {
                    avanzarA(nuevo);
                } else if (libroMostrado == null) {
                    mostrarMensaje(texto(R.string.sin_conexion), texto(R.string.sin_conexion_detalle));
                }
            } finally {
                cargando = false;
                estaCargando.postValue(false);
            }
        });
    }

    private void avanzarA(Libro libro) {
        libroMostrado = libro;
        cambiarVistaLibro();
        fijarComoActual(libro);
    }

    //Un libro pasa a ser el actual: se guarda para el proximo arranque y entra en el
    //historial. Antes al historial iba el que se dejaba atras, no el que se enseniaba,
    //asi que el ultimo libro visto nunca llegaba a aparecer en Explorar. Al restaurar
    //un libro guardado no se pasa por aqui: ya estaba registrado en su momento.
    private void fijarComoActual(Libro libro) {
        repo.guardarLibroActual(libro);
        repo.registrarVisita(libro);
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
        //Ya no se manda semilla: el servidor construye el perfil de gustos leyendo
        //favoritos, leidos y descartados, y decide a que preguntar. Mandarla desde
        //aqui cortocircuitaba ese perfil, porque el servidor la usaba como unica
        //consulta.
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

    //Descarta el libro que se ve y pasa al siguiente. El descarte viaja a Firestore
    //y el servidor lo usa para restar peso a sus materias y a su autor.
    public void descartarLibro() {
        Libro descartado = libroMostrado;
        if (descartado == null) {
            return;
        }
        repo.descartar(descartado);
        cambioLibro();
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
