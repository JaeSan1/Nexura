package com.example.nexura.Repository;

import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventoRepository {

    private final SupabaseApi api;

    // Interfaz de Callback para comunicar datos de forma segura con los ViewModels
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String mensajeError);
    }

    public EventoRepository() {
        this.api = SupabaseCliente.getClient().create(SupabaseApi.class);
    }

    // 1. Obtener todos los eventos (Método principal)
    public void obtenerTodosLosEventos(ApiCallback<List<Evento>> callback) {
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener eventos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                callback.onError("Error de conexion: " + t.getMessage());
            }
        });
    }

    // Alias para compatibilidad con ExplorarViewModel
    public void obtenerEventos(ApiCallback<List<Evento>> callback) {
        obtenerTodosLosEventos(callback);
    }

    // Alias para compatibilidad con HomeViewModel[cite: 9]
    public void obtenerEventosDestacados(ApiCallback<List<Evento>> callback) {
        obtenerTodosLosEventos(callback);
    }

    // 2. Obtener Mis Eventos (Guardados vs Historial Asistidos)
    public void obtenerMisEventos(String userId, boolean esHistorial, ApiCallback<List<Map<String, Object>>> callback) {
        String filtroAsistio = esHistorial ? "eq.true" : "eq.false";
        api.obtenerAsistenciasUsuario("eq." + userId, filtroAsistio).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener mis eventos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError("Error de conexion: " + t.getMessage());
            }
        });
    }
}