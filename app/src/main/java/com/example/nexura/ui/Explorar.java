package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.Viewmodel.ExplorarViewModel;
import com.example.nexura.model.Evento;
import com.example.nexura.ui.adapter.EventoFeedAdapter;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

public class Explorar extends AppCompatActivity {

    private ExplorarViewModel explorarViewModel;
    private RecyclerView rvExplorarEventos;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;
    private MapView mapPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar configuración de OSMDroid para la vista previa del mapa
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid_pref", MODE_PRIVATE));

        setContentView(R.layout.activity_explorar_mapa);

        explorarViewModel = new ViewModelProvider(this).get(ExplorarViewModel.class);

        // 1. Configurar Vista Previa del Mapa (mapPreview)
        mapPreview = findViewById(R.id.mapPreview);
        if (mapPreview != null) {
            mapPreview.setTileSource(TileSourceFactory.MAPNIK);
            mapPreview.setMultiTouchControls(false); // Deshabilitar gestos en la vista previa

            // Coordenadas centradas en Chillán
            GeoPoint chillanPoint = new GeoPoint(-36.6063, -72.1023);
            mapPreview.getController().setZoom(13.5);
            mapPreview.getController().setCenter(chillanPoint);
        }

        // 2. Botón para abrir el Mapa Completo
        View btnVerMapaCompleto = findViewById(R.id.btnVerMapaCompleto);
        if (btnVerMapaCompleto != null) {
            btnVerMapaCompleto.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, MapaInteractivoActivity.class);
                startActivity(intent);
            });
        }

        // 3. RecyclerView de Eventos
        rvExplorarEventos = findViewById(R.id.rvExplorarEventos);
        if (rvExplorarEventos != null) {
            rvExplorarEventos.setLayoutManager(new LinearLayoutManager(this));
            listaEventos = new ArrayList<>();
            adapter = new EventoFeedAdapter(this, listaEventos, evento -> {
                Intent intent = new Intent(Explorar.this, Detalle_Evento.class);
                intent.putExtra("EVENTO_SELECCIONADO", evento);
                startActivity(intent);
            });
            rvExplorarEventos.setAdapter(adapter);
        }

        // 4. Suscribirse a LiveData
        explorarViewModel.getEventosFeed().observe(this, eventos -> {
            if (eventos != null) {
                listaEventos.clear();
                listaEventos.addAll(eventos);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });

        explorarViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(Explorar.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar eventos desde Supabase
        explorarViewModel.cargarEventosFeed();

        // 5. Configurar Barra de Navegación Inferior
        configurarNavegacionInferior();
    }

    private void configurarNavegacionInferior() {
        TextView navHome = findViewById(R.id.navHome);
        TextView navMyEvents = findViewById(R.id.navMyEvents);
        TextView navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }

        if (navMyEvents != null) {
            navMyEvents.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, MisEventos.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, Perfil.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapPreview != null) mapPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapPreview != null) mapPreview.onPause();
    }
}