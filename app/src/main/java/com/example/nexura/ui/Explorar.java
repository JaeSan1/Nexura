package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.adapter.EventoFeedAdapter;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Explorar extends AppCompatActivity {

    private RecyclerView rvFeed;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorar_mapa);

        // 1. Inicializar lista
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

        // 2. Traer los eventos desde Supabase
        cargarEventosDesdeSupabase();

        // 3. Barra de Navegación Inferior
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
                    adapter.notifyDataSetChanged();
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
}