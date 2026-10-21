package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.ui.adapter.EventoFeedAdapter;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.Viewmodel.ExplorarViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class Explorar extends AppCompatActivity {

    private EventoFeedAdapter adapter;
    private MapView mapPreviewView;
    private ExplorarViewModel viewModel;
    private View layoutLoadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue("NexuraCommunityApp/2.0 (contacto: nexura.chillan@gmail.com)");

        setContentView(R.layout.activity_explorar_mapa);

        CardView cardOpenFullMap = findViewById(R.id.cardOpenFullMap);
        mapPreviewView = findViewById(R.id.mapPreviewView);
        layoutLoadingOverlay = findViewById(R.id.layoutLoadingOverlay);

        // Mapa de OpenStreetMap
        if (mapPreviewView != null) {
            XYTileSource OSM_PUBLICO = new XYTileSource(
                    "OSM_Publico",
                    0, 19, 256, ".png",
                    new String[] {
                            "https://a.tile.openstreetmap.fr/osmfr/",
                            "https://b.tile.openstreetmap.fr/osmfr/",
                            "https://c.tile.openstreetmap.fr/osmfr/"
                    }
            );
            mapPreviewView.setTileSource(OSM_PUBLICO);
            mapPreviewView.setMultiTouchControls(false);
            mapPreviewView.getController().setZoom(14.0);
            GeoPoint puntoChillan = new GeoPoint(-36.6067, -72.1034);
            mapPreviewView.getController().setCenter(puntoChillan);
        }

        if (cardOpenFullMap != null) {
            cardOpenFullMap.setOnClickListener(v -> startActivity(new Intent(Explorar.this, MapaInteractivoActivity.class)));
        }

        RecyclerView rvFeed = findViewById(R.id.rvFeedEventos);
        if (rvFeed != null) {
            rvFeed.setLayoutManager(new LinearLayoutManager(this));
            adapter = new EventoFeedAdapter(this, new ArrayList<>(), evento -> {
                Intent intent = new Intent(Explorar.this, Detalle_Evento.class);
                intent.putExtra("EVENTO_SELECCIONADO", evento);
                startActivity(intent);
            });
            rvFeed.setAdapter(adapter);
        }

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ExplorarViewModel.class);

        // Observar estado de carga (Overlay)
        viewModel.getCargando().observe(this, cargando -> {
            if (layoutLoadingOverlay != null) {
                layoutLoadingOverlay.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE);
            }
        });

        // Observar la lista de eventos desde el ViewModel
        viewModel.getEventosFeed().observe(this, eventos -> {
            if (eventos != null && adapter != null) {
                adapter.actualizarLista(eventos);
            }
        });

        // Observar errores de red
        viewModel.getError().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(Explorar.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar datos
        if (NetworkUtils.hayInternet(this)) {
            viewModel.cargarEventosFeed();
        } else {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
        }

        // Navegación Inferior
        TextView navHome = findViewById(R.id.navHome);
        TextView navMyEvents = findViewById(R.id.navMyEvents);
        TextView navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(Explorar.this, Home.class));
                finish();
            });
        }
        if (navMyEvents != null) {
            navMyEvents.setOnClickListener(v -> {
                startActivity(new Intent(Explorar.this, MisEventos.class));
                finish();
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(Explorar.this, Perfil.class));
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapPreviewView != null) mapPreviewView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapPreviewView != null) mapPreviewView.onPause();
    }
}