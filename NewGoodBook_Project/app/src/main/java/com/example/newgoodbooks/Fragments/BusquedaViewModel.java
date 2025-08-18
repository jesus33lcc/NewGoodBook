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

//Resultados de una busqueda. Existe por dos fallos concretos de tenerlo en la Activity:
//
//  · la busqueda salia de onCreate, asi que CADA giro de pantalla repetia la peticion
//    de red y volvia a empezar de cero;
//  · no habia estado de "buscando", asi que mientras iba la peticion la pantalla ya
//    ensenaba "Sin resultados", que es mentira y ademas alarma.
//
//El ViewModel sobrevive al giro, asi que la busqueda se hace UNA vez por consulta.
public class BusquedaViewModel extends ViewModel {

    private final MutableLiveData<List<Libro>> resultados = new MutableLiveData<>();
    private final MutableLiveData<Boolean> buscando = new MutableLiveData<>(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    //la consulta ya resuelta, para no repetirla al recrearse la pantalla
    private String consultaHecha;
    private String ambitoActual = ClienteFunciones.AMBITO_TODO;

    public LiveData<List<Libro>> getResultados() {
        return resultados;
    }

    public LiveData<Boolean> getBuscando() {
        return buscando;
    }

    public String getAmbito() {
        return ambitoActual;
    }

    //Idempotente a proposito: repetir la misma consulta con el mismo ambito no hace nada.
    public void buscar(String consulta, String ambito) {
        if (consulta == null || consulta.trim().isEmpty()) {
            resultados.setValue(new ArrayList<Libro>());
            return;
        }
        String clave = ambito + "|" + consulta;
        if (clave.equals(consultaHecha)) {
            return;
        }
        consultaHecha = clave;
        ambitoActual = ambito;
        buscando.setValue(true);
        executor.execute(() -> {
            List<Libro> encontrados = ClienteFunciones.buscar(consulta, ambito);
            resultados.postValue(encontrados);
            buscando.postValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
