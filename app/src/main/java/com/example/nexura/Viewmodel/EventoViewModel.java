package com.example.nexura.Viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nexura.Repository.EventoRepository;
import com.example.nexura.model.Evento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventoViewModel extends ViewModel {

    private final EventoRepository eventoRepository;

    private final MutableLiveData<List<Evento>> eventosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Evento>> eventosFiltradosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Map<String, Object>>> misEventosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargandoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private List<Evento> listaEventosCache = new ArrayList<>();

    public EventoViewModel() {
        this.eventoRepository = new EventoRepository();
    }

    public LiveData<List<Evento>> getEventos() {
        return eventosFiltradosLiveData;
    }

    public LiveData<List<Map<String, Object>>> getMisEventos() {
        return misEventosLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // Cargar todos los eventos desde Supabase mediante el Repositorio
    public void cargarEventos() {
        cargandoLiveData.setValue(true);
        eventoRepository.obtenerTodosLosEventos(new EventoRepository.ApiCallback<List<Evento>>() {
            @Override
            public void onSuccess(List<Evento> result) {
                cargandoLiveData.setValue(false);
                listaEventosCache = result != null ? result : new ArrayList<>();
                eventosLiveData.setValue(listaEventosCache);
                eventosFiltradosLiveData.setValue(listaEventosCache);
            }

            @Override
            public void onError(String mensajeError) {
                cargandoLiveData.setValue(false);
                errorLiveData.setValue(mensajeError);
                eventosFiltradosLiveData.setValue(new ArrayList<>());
            }
        });
    }

    // Filtrar eventos por categoría en el Home de forma local
    public void filtrarPorCategoria(String categoria) {
        if (categoria == null || categoria.equalsIgnoreCase("Todos")) {
            eventosFiltradosLiveData.setValue(listaEventosCache);
            return;
        }

        List<Evento> filtrada = new ArrayList<>();
        for (Evento evento : listaEventosCache) {
            if (evento.getCategoria() != null && evento.getCategoria().equalsIgnoreCase(categoria)) {
                filtrada.add(evento);
            }
        }
        eventosFiltradosLiveData.setValue(filtrada);
    }

    // Cargar Mis Eventos (Guardados vs Historial Asistidos) desde Supabase
    public void cargarMisEventos(String userId, boolean esHistorial) {
        cargandoLiveData.setValue(true);
        eventoRepository.obtenerMisEventos(userId, esHistorial, new EventoRepository.ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                cargandoLiveData.setValue(false);
                misEventosLiveData.setValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String mensajeError) {
                cargandoLiveData.setValue(false);
                misEventosLiveData.setValue(new ArrayList<>());
            }
        });
    }
}