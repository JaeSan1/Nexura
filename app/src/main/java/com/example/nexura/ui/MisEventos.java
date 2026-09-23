package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class MisEventos extends AppCompatActivity {

    private RecyclerView rvMyEvents;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;
    private SupabaseApi api;

    private TextView tabSaved, tabHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_eventos);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackMyEvents);
        tabSaved = findViewById(R.id.tabSavedEvents);
        tabHistory = findViewById(R.id.tabAttendedHistory);
        rvMyEvents = findViewById(R.id.rvMyEvents);

        // Barra de navegación inferior
        TextView navHome = findViewById(R.id.navHome);
        TextView navExplore = findViewById(R.id.navExplore);
        TextView navProfile = findViewById(R.id.navProfile);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Inicializar lista
        if (rvMyEvents != null) {
            rvMyEvents.setLayoutManager(new LinearLayoutManager(this));
            listaEventos = new ArrayList<>();
            adapter = new EventoFeedAdapter(this, listaEventos, evento -> {
                Intent intent = new Intent(MisEventos.this, Detalle_Evento.class);
                intent.putExtra("EVENTO_SELECCIONADO", evento);
                startActivity(intent);
            });
            rvMyEvents.setAdapter(adapter);
        }

        // Cargar eventos desde Supabase
        cargarEventosUsuario(true);

        // Manejo de Pestañas
        if (tabSaved != null && tabHistory != null) {
            tabSaved.setOnClickListener(v -> {
                cambiarPestana(true);
                cargarEventosUsuario(true);
            });

            tabHistory.setOnClickListener(v -> {
                cambiarPestana(false);
                cargarEventosUsuario(false);
            });
        }

        // Navbar Clics
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(MisEventos.this, Home.class));
                finish();
            });
        }
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                startActivity(new Intent(MisEventos.this, Explorar.class));
                finish();
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(MisEventos.this, Perfil.class));
                finish();
            });
        }
    }

    private void cargarEventosUsuario(boolean soloGuardados) {
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEventos.clear();
                    List<Evento> todos = response.body();

                    // Simulación de filtro por pestaña entre los eventos disponibles
                    if (soloGuardados) {
                        // Muestra los primeros eventos guardados
                        for (int i = 0; i < Math.min(todos.size(), 2); i++) {
                            listaEventos.add(todos.get(i));
                        }
                    } else {
                        // Muestra el resto como historial
                        if (todos.size() > 2) {
                            for (int i = 2; i < todos.size(); i++) {
                                listaEventos.add(todos.get(i));
                            }
                        } else {
                            listaEventos.addAll(todos);
                        }
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(MisEventos.this, "No se pudieron obtener eventos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(MisEventos.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cambiarPestana(boolean esGuardados) {
        if (esGuardados) {
            tabSaved.setBackgroundResource(R.drawable.categoria);
            tabSaved.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            tabHistory.setBackground(null);
            tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        } else {
            tabHistory.setBackgroundResource(R.drawable.categoria);
            tabHistory.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            tabSaved.setBackground(null);
            tabSaved.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        }
    }
}