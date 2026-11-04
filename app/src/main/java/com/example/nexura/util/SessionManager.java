package com.example.nexura.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "NexuraSession";
    private static final String KEY_IS_LOGGED = "isLogged";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_GAMERTAG = "gamertag";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Guarda la sesión localmente en el teléfono
    public void guardarSesion(String userId, String gamertag, String email) {
        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_GAMERTAG, gamertag);
        editor.putString(KEY_EMAIL, email);
        editor.apply(); // Guarda de forma asíncrona en el disco del teléfono
    }

    // Comprueba si la sesión sigue activa
    public boolean isLogged() {
        return pref.getBoolean(KEY_IS_LOGGED, false);
    }

    // Devuelve el ID del usuario actual (soluciona el error en Detalle_Evento, Perfil, etc.)
    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    // Para obtener los datos del usuario en cualquier Activity (Perfil, Home, etc.)
    public String getGamertag() {
        return pref.getString(KEY_GAMERTAG, "Usuario");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    // Método para cuando el usuario presione "Cerrar Sesión" en su Perfil
    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}