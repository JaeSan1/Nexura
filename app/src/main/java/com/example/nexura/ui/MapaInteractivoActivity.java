package com.example.nexura.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
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

        // OBLIGATORIO: Evita el bloqueo HTTP 403 de OpenStreetMap identificando la app
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_mapa_interactivo);

        mapView = findViewById(R.id.mapViewFull);
        ImageButton btnBack = findViewById(R.id.btnBackMap);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            // Coordenadas iniciales (Chillán / Universidad del Bío-Bío)
            GeoPoint startPoint = new GeoPoint(-36.6018, -72.0782);
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(startPoint);

            // Marcador de ejemplo
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
}