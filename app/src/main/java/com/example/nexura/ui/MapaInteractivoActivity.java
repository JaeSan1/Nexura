package com.example.nexura.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapaInteractivoActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de OSMDroid
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid_pref", MODE_PRIVATE));

        setContentView(R.layout.activity_mapa_interactivo);

        TextView btnBack = findViewById(R.id.btnBackMapa); // Revisa el ID en tu activity_mapa_interactivo.xml
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        mapView = findViewById(R.id.mapViewFull); // Revisa el ID en tu activity_mapa_interactivo.xml
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            // Coordenadas centradas en Chillán, Chile
            GeoPoint chillanPoint = new GeoPoint(-36.6063, -72.1023);
            mapView.getController().setZoom(14.5);
            mapView.getController().setCenter(chillanPoint);
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