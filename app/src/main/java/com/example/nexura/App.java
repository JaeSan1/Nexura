package com.example.nexura;

import android.app.Application;
import android.content.Context;
import org.osmdroid.config.Configuration;
import java.io.File;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Context ctx = getApplicationContext();

            // 1. Cargar preferencias de osmdroid
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));

            // 2. Usar el nombre del paquete real para cumplir la política de OSM
            Configuration.getInstance().setUserAgentValue(getPackageName() + "/2.0 (contacto: nexura.chillan@gmail.com)");

            // 3. Cambiar el nombre de la carpeta de caché (ej: "osmdroid_cache_v2")
            // para descartar automáticamente todos los archivos 403 anteriores almacenados
            File osmDir = new File(ctx.getFilesDir(), "osmdroid_cache_v2");
            if (!osmDir.exists()) {
                osmDir.mkdirs();
            }
            Configuration.getInstance().setOsmdroidBasePath(osmDir);
            Configuration.getInstance().setOsmdroidTileCache(new File(osmDir, "tiles"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}