package com.example.nexura.Viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nexura.Repository.EventoRepository;
import com.example.nexura.model.Evento;

import java.util.ArrayList;
import java.util.List;

public class ExplorarViewModel extends ViewModel {

    private final EventoRepository eventoRepository;

    private final MutableLiveData<List<Evento>> eventosFeedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargandoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public ExplorarViewModel() {
        this.eventoRepository = new EventoRepository();
    }

    public LiveData<List<Evento>> getEventosFeed() {
        return eventosFeedLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarEventosFeed() {
        cargandoLiveData.setValue(true);
        eventoRepository.obtenerTodosLosEventos(new EventoRepository.ApiCallback<List<Evento>>() {
            @Override
            public void onSuccess(List<Evento> result) {
                cargandoLiveData.setValue(false);
                eventosFeedLiveData.setValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String mensajeError) {
                cargandoLiveData.setValue(false);
                errorLiveData.setValue(mensajeError);
                eventosFeedLiveData.setValue(new ArrayList<>());
            }
        });
    }
}