package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.SessionManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaInteractivoActivity extends AppCompatActivity {

    private MapView mapView;
    private SupabaseApi api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // CLAVE PARA SOLUCIONAR EL ERROR 403:
        // Cargar la configuración e indicar el User-Agent ANTES de setContentView(...)
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_interactivo);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        sessionManager = new SessionManager(this);

        mapView = findViewById(R.id.mapView);
        TextView btnBack = findViewById(R.id.btnBackMap);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        configurarMapa();
        cargarEventosEnMapa();
    }

    private void configurarMapa() {
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            // Coordenadas centradas por defecto (Chillán)
            GeoPoint puntoChillan = new GeoPoint(-36.6066, -72.1033);
            mapView.getController().setZoom(14.5);
            mapView.getController().setCenter(puntoChillan);
        }
    }

    private void cargarEventosEnMapa() {
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null && mapView != null) {
                    mapView.getOverlays().clear();

                    for (Evento evento : response.body()) {
                        if (evento.getLatitud() != 0 && evento.getLongitud() != 0) {
                            GeoPoint pos = new GeoPoint(evento.getLatitud(), evento.getLongitud());
                            Marker marker = new Marker(mapView);
                            marker.setPosition(pos);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle(evento.getTitulo());
                            marker.setSnippet(evento.getUbicacion() + " | +" + evento.getXpRecompensa() + " XP");

                            // Al presionar el marcador, abrir la vista detallada del evento
                            marker.setOnMarkerClickListener((m, mapView1) -> {
                                m.showInfoWindow();
                                Intent intent = new Intent(MapaInteractivoActivity.this, Detalle_Evento.class);
                                intent.putExtra("EVENTO_SELECCIONADO", evento);
                                startActivity(intent);
                                return true;
                            });

                            mapView.getOverlays().add(marker);
                        }
                    }

                    mapView.invalidate(); // Refrescar la capa gráfica del mapa
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(MapaInteractivoActivity.this, "Error al cargar pines en el mapa", Toast.LENGTH_SHORT).show();
            }
        });
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