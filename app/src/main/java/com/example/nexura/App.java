package com.example.nexura;

import android.app.Application;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Carga la configuración global de OSMDroid y asigna el User-Agent con el nombre de tu app
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}