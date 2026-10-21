package com.example.nexura.Viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nexura.Repository.EventoRepository;
import com.example.nexura.Repository.UsuarioRepository;
import com.example.nexura.model.Evento;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    private final MutableLiveData<List<Evento>> eventosDestacadosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> esCreadorAutorizadoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public HomeViewModel() {
        this.eventoRepository = new EventoRepository();
        this.usuarioRepository = new UsuarioRepository();
    }

    public LiveData<List<Evento>> getEventosDestacados() {
        return eventosDestacadosLiveData;
    }

    public LiveData<Boolean> getEsCreadorAutorizado() {
        return esCreadorAutorizadoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarEventosDestacados() {
        eventoRepository.obtenerEventosDestacados(new EventoRepository.ApiCallback<List<Evento>>() {
            @Override
            public void onSuccess(List<Evento> result) {
                eventosDestacadosLiveData.setValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String mensajeError) {
                errorLiveData.setValue(mensajeError);
                eventosDestacadosLiveData.setValue(new ArrayList<>());
            }
        });
    }
}