package com.example.nexura.Viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventoViewModel extends AndroidViewModel {

    private final SupabaseApi api;

    // LiveData para Mis Eventos
    private final MutableLiveData<List<Object>> misEventosLiveData = new MutableLiveData<>();

    // LiveData para el Home tipado como List<Evento>
    private final MutableLiveData<List<Evento>> eventosLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public EventoViewModel(@NonNull Application application) {
        super(application);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
    }

    // --- Métodos para MisEventos ---
    public LiveData<List<Object>> getMisEventos() {
        return misEventosLiveData;
    }

    public void cargarMisEventos(String userId, boolean esHistorial) {
        if (userId == null || userId.isEmpty()) return;

        api.obtenerAsistenciasUsuario("eq." + userId).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> listaFiltrada = new ArrayList<>();

                    for (Object obj : response.body()) {
                        if (obj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> reg = (Map<String, Object>) obj;

                            if (esHistorial) {
                                Object gpsObj = reg.get("validado_gps");
                                if (gpsObj != null && Boolean.parseBoolean(gpsObj.toString())) {
                                    listaFiltrada.add(reg);
                                }
                            } else {
                                Object favObj = reg.get("es_favorito");
                                if (favObj != null && Boolean.parseBoolean(favObj.toString())) {
                                    listaFiltrada.add(reg);
                                }
                            }
                        }
                    }
                    misEventosLiveData.setValue(listaFiltrada);
                } else {
                    misEventosLiveData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {
                misEventosLiveData.setValue(new ArrayList<>());
            }
        });
    }

    // --- Métodos para Home y Explorar ---
    public LiveData<List<Evento>> getEventos() {
        return eventosLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarEventos() {
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventosLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Error al cargar los eventos");
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                errorLiveData.setValue(t.getMessage());
            }
        });
    }

    public void filtrarPorCategoria(String categoria) {
        if (categoria == null || categoria.isEmpty()) {
            cargarEventos();
            return;
        }

        api.obtenerEventosPorCategoria("eq." + categoria).enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventosLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Error al filtrar por categoría");
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                errorLiveData.setValue(t.getMessage());
            }
        });
    }
}