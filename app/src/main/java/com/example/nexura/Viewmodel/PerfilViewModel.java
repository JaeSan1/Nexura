package com.example.nexura.Viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nexura.model.Logro;
import com.example.nexura.model.Usuario;
import com.example.nexura.Repository.UsuarioRepository;

import java.util.List;

public class PerfilViewModel extends ViewModel {

    private final UsuarioRepository usuarioRepository;

    private final MutableLiveData<Usuario> usuarioLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Logro>> logrosLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public PerfilViewModel() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public LiveData<Usuario> getUsuario() {
        return usuarioLiveData;
    }

    public LiveData<List<Logro>> getLogros() {
        return logrosLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarPerfil(String userId) {
        usuarioRepository.obtenerPerfilPorId(userId, new UsuarioRepository.ApiCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioLiveData.setValue(usuario);
            }

            @Override
            public void onError(String mensajeError) {
                errorLiveData.setValue(mensajeError);
            }
        });
    }

    public void cargarLogros() {
        usuarioRepository.obtenerLogros(new UsuarioRepository.ApiCallback<List<Logro>>() {
            @Override
            public void onSuccess(List<Logro> lista) {
                logrosLiveData.setValue(lista);
            }

            @Override
            public void onError(String mensajeError) {
                errorLiveData.setValue(mensajeError);
            }
        });
    }
}