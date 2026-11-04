package com.example.nexura.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.BuildConfig;
import com.example.nexura.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaInteractivoActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración obligatoria de OSMDroid antes de setContentView
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid_pref", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName() + "/" + BuildConfig.VERSION_NAME);
        Configuration.getInstance().setOsmdroidBasePath(getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(getCacheDir());

        setContentView(R.layout.activity_mapa_interactivo);

        mapView = findViewById(R.id.mapViewFull);
        if (mapView != null) {
            mapView.getTileProvider().clearTileCache();

            // Forzar servidores HTTPS oficiales de OSM
            org.osmdroid.tileprovider.tilesource.XYTileSource explicitHttpsMap =
                    new org.osmdroid.tileprovider.tilesource.XYTileSource(
                            "OSM_HTTPS",
                            0, 19, 256, ".png",
                            new String[]{
                                    "https://a.tile.openstreetmap.org/",
                                    "https://b.tile.openstreetmap.org/",
                                    "https://c.tile.openstreetmap.org/"
                            }
                    );

            mapView.setTileSource(explicitHttpsMap);
            mapView.setMultiTouchControls(true);

            GeoPoint startPoint = new GeoPoint(-36.6018, -72.0782);
            mapView.getController().setCenter(startPoint);
            mapView.getController().setZoom(15.0);

            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle("Fiesta de la Longaniza 2026");
            mapView.getOverlays().add(startMarker);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
    }
}