package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.Viewmodel.EventoViewModel;
import com.example.nexura.model.Evento;
import com.example.nexura.ui.adapter.EventoFeedAdapter;
import com.example.nexura.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MisEventos extends AppCompatActivity {

    private RecyclerView rvMyEvents;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;

    private TextView tabSaved, tabHistory;
    private EventoViewModel eventoViewModel;
    private SessionManager sessionManager;
    private boolean mostrandoGuardados = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_eventos);

        sessionManager = new SessionManager(this);
        eventoViewModel = new ViewModelProvider(this).get(EventoViewModel.class);

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

        // Inicializar RecyclerView y Adapter
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

        // Manejo de Pestañas
        if (tabSaved != null && tabHistory != null) {
            tabSaved.setOnClickListener(v -> {
                mostrandoGuardados = true;
                cambiarPestana(true);
                consultarMisEventos();
            });

            tabHistory.setOnClickListener(v -> {
                mostrandoGuardados = false;
                cambiarPestana(false);
                consultarMisEventos();
            });
        }

        // Observar la lista de asistencias/eventos desde el ViewModel
        eventoViewModel.getMisEventos().observe(this, this::procesarRespuestaEventos);

        // Cargar eventos guardados al iniciar
        consultarMisEventos();

        // NAVEGACIÓN NAVBAR INFERIOR
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar automáticamente al volver a la pantalla
        consultarMisEventos();
    }

    private void consultarMisEventos() {
        String userId = sessionManager.getUserId();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si mostrandoGuardados es true -> esHistorial = false (Guardados)
        // Si mostrandoGuardados es false -> esHistorial = true (Asistidos)
        eventoViewModel.cargarMisEventos(userId, !mostrandoGuardados);
    }

    @SuppressWarnings("unchecked")
    private void procesarRespuestaEventos(List<Map<String, Object>> listaAsistencias) {
        if (listaEventos == null) {
            listaEventos = new ArrayList<>();
        } else {
            listaEventos.clear();
        }

        if (listaAsistencias != null && !listaAsistencias.isEmpty()) {
            for (Map<String, Object> fila : listaAsistencias) {
                // Mapear la respuesta anidada 'eventos(*)' de Supabase a objetos Evento
                Object objEvento = fila.get("eventos");
                if (objEvento instanceof Map) {
                    Map<String, Object> mapEv = (Map<String, Object>) objEvento;
                    Evento e = new Evento();

                    if (mapEv.get("id") != null) e.setId(mapEv.get("id").toString());
                    if (mapEv.get("titulo") != null) e.setTitulo(mapEv.get("titulo").toString());
                    if (mapEv.get("descripcion") != null) e.setDescripcion(mapEv.get("descripcion").toString());
                    if (mapEv.get("ubicacion") != null) e.setUbicacion(mapEv.get("ubicacion").toString());
                    if (mapEv.get("ciudad") != null) e.setCiudad(mapEv.get("ciudad").toString());
                    if (mapEv.get("fecha") != null) e.setFecha(mapEv.get("fecha").toString());
                    if (mapEv.get("imagen_url") != null) e.setImagenUrl(mapEv.get("imagen_url").toString());
                    if (mapEv.get("categoria") != null) e.setCategoria(mapEv.get("categoria").toString());
                    if (mapEv.get("organizador") != null) e.setOrganizador(mapEv.get("organizador").toString());

                    if (mapEv.get("xp_recompensa") != null) {
                        try {
                            e.setXpRecompensa(Integer.parseInt(mapEv.get("xp_recompensa").toString()));
                        } catch (NumberFormatException ignored) {}
                    }

                    if (mapEv.get("latitud") != null) {
                        try {
                            e.setLatitud(Double.parseDouble(mapEv.get("latitud").toString()));
                        } catch (NumberFormatException ignored) {}
                    }

                    if (mapEv.get("longitud") != null) {
                        try {
                            e.setLongitud(Double.parseDouble(mapEv.get("longitud").toString()));
                        } catch (NumberFormatException ignored) {}
                    }

                    listaEventos.add(e);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void cambiarPestana(boolean esGuardados) {
        if (tabSaved == null || tabHistory == null) return;

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