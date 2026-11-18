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

            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
            Configuration.getInstance().setUserAgentValue("NexuraAppAndroid/1.0");

            // 4 hilos y cola acotada a 40 para evitar saturación al hacer pinch-to-zoom
            Configuration.getInstance().setTileDownloadThreads((short) 4);
            Configuration.getInstance().setTileFileSystemThreads((short) 2);
            Configuration.getInstance().setTileDownloadMaxQueueSize((short) 40);

            // Caché persistente de 7 días
            Configuration.getInstance().setExpirationOverrideDuration(1000L * 60 * 60 * 24 * 7);

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