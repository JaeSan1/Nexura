package com.example.nexura.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "NexuraSession";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_GAMERTAG = "USER_GAMERTAG";
    private static final String KEY_EMAIL = "USER_EMAIL";
    private static final String KEY_IS_LOGGED = "IS_LOGGED";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(String id, String gamertag, String email) {
        prefs.edit()
                .putString(KEY_USER_ID, id)
                .putString(KEY_GAMERTAG, gamertag)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED, true)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getGamertag() {
        return prefs.getString(KEY_GAMERTAG, "Usuario");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public boolean isLogged() {
        return prefs.getBoolean(KEY_IS_LOGGED, false);
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}