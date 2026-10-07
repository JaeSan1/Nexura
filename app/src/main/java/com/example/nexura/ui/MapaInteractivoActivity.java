package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaInteractivoActivity extends AppCompatActivity {

    private MapView osmMapView;
    private final GeoPoint centroChillan = new GeoPoint(-36.6067, -72.1034);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue("NexuraCommunityApp/2.0 (contacto: nexura.chillan@gmail.com)");

        setContentView(R.layout.activity_mapa_interactivo);

        TextView btnBack = findViewById(R.id.btnBackMap);
        Button btnCenter = findViewById(R.id.btnCenterLocation);
        osmMapView = findViewById(R.id.osmMapView);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (osmMapView != null) {
            XYTileSource OSM_PUBLICO = new XYTileSource(
                    "OSM_Publico",
                    0, 19, 256, ".png",
                    new String[] {
                            "https://a.tile.openstreetmap.fr/osmfr/",
                            "https://b.tile.openstreetmap.fr/osmfr/",
                            "https://c.tile.openstreetmap.fr/osmfr/"
                    }
            );
            osmMapView.setTileSource(OSM_PUBLICO);
            osmMapView.setMultiTouchControls(true);
            osmMapView.getController().setZoom(15.0);
            osmMapView.getController().setCenter(centroChillan);
        }

        if (btnCenter != null) {
            btnCenter.setOnClickListener(v -> {
                if (osmMapView != null) {
                    osmMapView.getController().animateTo(centroChillan);
                }
            });
        }


        cargarMarcadoresEventos();
    }

    private void cargarMarcadoresEventos() {
        SupabaseApi api = SupabaseCliente.getClient().create(SupabaseApi.class);
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null && osmMapView != null) {
                    for (Evento evento : response.body()) {
                        if (evento.getLatitud() != 0 && evento.getLongitud() != 0) {
                            Marker marker = new Marker(osmMapView);
                            marker.setPosition(new GeoPoint(evento.getLatitud(), evento.getLongitud()));
                            marker.setTitle(evento.getTitulo());
                            marker.setSnippet("+" + evento.getXpRecompensa() + " XP • " + evento.getUbicacion());

                            marker.setOnMarkerClickListener((m, mapView) -> {
                                m.showInfoWindow();
                                Intent intent = new Intent(MapaInteractivoActivity.this, Detalle_Evento.class);
                                intent.putExtra("EVENTO_SELECCIONADO", evento);
                                startActivity(intent);
                                return true;
                            });

                            osmMapView.getOverlays().add(marker);
                        }
                    }
                    osmMapView.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(MapaInteractivoActivity.this, "Error de red al cargar pines", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (osmMapView != null) osmMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (osmMapView != null) osmMapView.onPause();
    }
}