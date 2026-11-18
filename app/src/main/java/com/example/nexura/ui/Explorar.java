package com.example.nexura.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.Viewmodel.ExplorarViewModel;
import com.example.nexura.model.Evento;
import com.example.nexura.ui.adapter.EventoFeedAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;
import java.util.List;

public class Explorar extends AppCompatActivity {

    private ExplorarViewModel explorarViewModel;
    private RecyclerView rvExplorarEventos;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;
    private MapView mapPreview;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint ubicacionActualUsuario = null;

    private final ActivityResultLauncher<String[]> requestLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    centrarVistaPreviaEnMiUbicacion();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorar_mapa);

        explorarViewModel = new ViewModelProvider(this).get(ExplorarViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapPreview = findViewById(R.id.mapPreview);
        if (mapPreview != null) {
            ITileSource freeTileSource = new XYTileSource(
                    "OSM_HOT_DARK",
                    4,
                    18,
                    256,
                    ".png",
                    new String[]{
                            "https://a.tile.openstreetmap.fr/hot/",
                            "https://b.tile.openstreetmap.fr/hot/",
                            "https://c.tile.openstreetmap.fr/hot/"
                    }
            );

            mapPreview.setTileSource(freeTileSource);
            mapPreview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mapPreview.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
            mapPreview.setTilesScaledToDpi(true);
            mapPreview.setMultiTouchControls(false);

            // Centro temporal de respaldo mientras responde el GPS
            GeoPoint respaldoChillan = new GeoPoint(-36.6063, -72.1023);
            mapPreview.getController().setZoom(14.5);
            mapPreview.getController().setCenter(respaldoChillan);

            verificarYCentrarUbicacion();
        }

        View btnVerMapaCompleto = findViewById(R.id.btnVerMapaCompleto);
        if (btnVerMapaCompleto != null) {
            btnVerMapaCompleto.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, MapaInteractivoActivity.class);
                if (ubicacionActualUsuario != null) {
                    intent.putExtra("LAT_INICIAL", ubicacionActualUsuario.getLatitude());
                    intent.putExtra("LNG_INICIAL", ubicacionActualUsuario.getLongitude());
                }
                startActivity(intent);
            });
        }

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

        explorarViewModel.getEventosFeed().observe(this, eventos -> {
            if (eventos != null) {
                listaEventos.clear();
                listaEventos.addAll(eventos);
                if (adapter != null) adapter.notifyDataSetChanged();
                actualizarPinesVistaPrevia(eventos);
            }
        });

        explorarViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(Explorar.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        explorarViewModel.cargarEventosFeed();
        configurarNavegacionInferior();
    }

    private void verificarYCentrarUbicacion() {
        boolean permisoFino = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean permisoAprox = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (permisoFino || permisoAprox) {
            centrarVistaPreviaEnMiUbicacion();
        } else {
            requestLocationLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void centrarVistaPreviaEnMiUbicacion() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mapPreview != null) {
                ubicacionActualUsuario = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapPreview.getController().setZoom(15.0);
                mapPreview.getController().setCenter(ubicacionActualUsuario);
                mapPreview.invalidate();
            }
        });
    }

    private void actualizarPinesVistaPrevia(List<Evento> eventos) {
        if (mapPreview == null) return;
        mapPreview.getOverlays().clear();

        for (Evento ev : eventos) {
            if (ev.getLatitud() != 0 && ev.getLongitud() != 0) {
                Marker marker = new Marker(mapPreview);
                marker.setPosition(new GeoPoint(ev.getLatitud(), ev.getLongitud()));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(ev.getTitulo());
                mapPreview.getOverlays().add(marker);
            }
        }
        mapPreview.invalidate();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapPreview != null) mapPreview.onDetach();
    }
}