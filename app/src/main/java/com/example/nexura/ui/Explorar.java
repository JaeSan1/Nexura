package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.adapter.EventoFeedAdapter;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Explorar extends AppCompatActivity {

    private RecyclerView rvFeed;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;
    private MapView mapPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inicialización de configuración OpenStreetMap y User-Agent obligatorio
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_explorar_mapa);

        CardView cardOpenFullMap = findViewById(R.id.cardOpenFullMap);
        mapPreviewView = findViewById(R.id.mapPreviewView);

        // 2. Vista previa estática de Chillán
        if (mapPreviewView != null) {
            mapPreviewView.setTileSource(TileSourceFactory.MAPNIK);
            mapPreviewView.setMultiTouchControls(false); // Desactiva gestos para que funcione como una vista previa fija
            mapPreviewView.getController().setZoom(14.0);
            GeoPoint puntoChillan = new GeoPoint(-36.6067, -72.1034);
            mapPreviewView.getController().setCenter(puntoChillan);
        }

        // 3. Abrir la pantalla completa del mapa al hacer clic en la tarjeta
        if (cardOpenFullMap != null) {
            cardOpenFullMap.setOnClickListener(v -> {
                Intent intent = new Intent(Explorar.this, MapaInteractivoActivity.class);
                startActivity(intent);
            });
        }

        // 4. Inicializar RecyclerView del Feed
        rvFeed = findViewById(R.id.rvFeedEventos);
        if (rvFeed != null) {
            rvFeed.setLayoutManager(new LinearLayoutManager(this));
            listaEventos = new ArrayList<>();
            adapter = new EventoFeedAdapter(this, listaEventos, evento -> {
                Intent intent = new Intent(Explorar.this, Detalle_Evento.class);
                intent.putExtra("EVENTO_SELECCIONADO", evento);
                startActivity(intent);
            });
            rvFeed.setAdapter(adapter);
        }

        // 5. Cargar eventos desde Supabase
        cargarEventosDesdeSupabase();

        // 6. Barra de Navegación Inferior
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

    private void cargarEventosDesdeSupabase() {
        SupabaseApi api = SupabaseCliente.getClient().create(SupabaseApi.class);

        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEventos.clear();
                    listaEventos.addAll(response.body());
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(Explorar.this, "Error al cargar: código " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(Explorar.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Ciclo de vida obligatorio para que MapView maneje bien los recursos
    @Override
    protected void onResume() {
        super.onResume();
        if (mapPreviewView != null) {
            mapPreviewView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapPreviewView != null) {
            mapPreviewView.onPause();
        }
    }
}