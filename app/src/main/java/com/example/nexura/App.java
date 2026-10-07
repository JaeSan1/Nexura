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
            Configuration.getInstance().setUserAgentValue("NexuraCommunityApp/2.0 (contacto: nexura.chillan@gmail.com)");

            File osmDir = new File(ctx.getFilesDir(), "osmdroid_cache");
            if (!osmDir.exists()) {
                osmDir.mkdirs();
            }
            Configuration.getInstance().setOsmdroidBasePath(osmDir);
            Configuration.getInstance().setOsmdroidTileCache(new File(osmDir, "tiles"));
        } catch (Exception ignored) {}
    }
}