package com.example.nexura.Repository;

import com.example.nexura.model.Logro;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioRepository {

    private final SupabaseApi api;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String mensajeError);
    }

    public UsuarioRepository() {
        this.api = SupabaseCliente.getClient().create(SupabaseApi.class);
    }

    // Método 1: Nombre esperado por HomeViewModel y PerfilViewModel
    public void obtenerPerfilPorId(String userId, ApiCallback<Usuario> callback) {
        api.obtenerPerfilPorId("eq." + userId).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("No se encontró el perfil de usuario");
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                callback.onError("Error de conexión al obtener el perfil");
            }
        });
    }

    // Método 2: Alias para compatibilidad con otras partes del proyecto
    public void obtenerUsuario(String userId, ApiCallback<Usuario> callback) {
        obtenerPerfilPorId(userId, callback);
    }

    // Obtener catálogo de logros para la pantalla de Perfil
    public void obtenerLogros(ApiCallback<List<Logro>> callback) {
        api.obtenerLogros().enqueue(new Callback<List<Logro>>() {
            @Override
            public void onResponse(Call<List<Logro>> call, Response<List<Logro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener la lista de logros");
                }
            }

            @Override
            public void onFailure(Call<List<Logro>> call, Throwable t) {
                callback.onError("Error de red al cargar logros");
            }
        });
    }

    // Eliminar cuenta de usuario (Exigido por políticas de Google Play Store)
    public void eliminarCuenta(String userId, ApiCallback<Void> callback) {
        api.eliminarUsuario("eq." + userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error al eliminar la cuenta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de conexión al intentar eliminar la cuenta");
            }
        });
    }
}