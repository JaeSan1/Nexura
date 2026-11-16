package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.ui.adapter.EventoFeedAdapter;
import com.example.nexura.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanelOrganizador extends AppCompatActivity {

    private static final int REQUEST_CODE_CREAR_EDITAR = 2001;

    private RecyclerView rvMisEventos;
    private EventoFeedAdapter adapter;
    private List<Evento> listaMisEventos = new ArrayList<>();
    private SupabaseApi api;
    private SessionManager sessionManager;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_organizador);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackPanel);
        Button btnCrearNuevo = findViewById(R.id.btnCreateNewEventPanel);
        rvMisEventos = findViewById(R.id.rvMisEventosOrganizados);
        tvEmptyState = findViewById(R.id.tvEmptyStateOrganizador);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnCrearNuevo != null) {
            btnCrearNuevo.setOnClickListener(v -> {
                Intent intent = new Intent(PanelOrganizador.this, CrearEvento.class);
                startActivityForResult(intent, REQUEST_CODE_CREAR_EDITAR);
            });
        }

        setupRecyclerView();
        cargarMisEventosOrganizados();
    }

    private void setupRecyclerView() {
        if (rvMisEventos != null) {
            rvMisEventos.setLayoutManager(new LinearLayoutManager(this));

            // Reutilizamos el adapter asignando click largo o callbacks para Editar / Eliminar
            adapter = new EventoFeedAdapter(this, listaMisEventos, evento -> {
                mostrarOpcionesEvento(evento);
            });
            rvMisEventos.setAdapter(adapter);
        }
    }

    private void cargarMisEventosOrganizados() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        api.obtenerEventosPorOrganizador("eq." + userId).enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMisEventos.clear();
                    listaMisEventos.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (tvEmptyState != null) {
                        tvEmptyState.setVisibility(listaMisEventos.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(PanelOrganizador.this, "Error de red al cargar eventos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarOpcionesEvento(Evento evento) {
        String[] opciones = {"Editar Evento", "Eliminar Evento"};

        new AlertDialog.Builder(this)
                .setTitle(evento.getTitulo())
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        // Editar
                        Intent intent = new Intent(PanelOrganizador.this, CrearEvento.class);
                        intent.putExtra("EVENTO_A_EDITAR", evento);
                        startActivityForResult(intent, REQUEST_CODE_CREAR_EDITAR);
                    } else if (which == 1) {
                        // Eliminar
                        confirmarEliminacion(evento);
                    }
                })
                .show();
    }

    private void confirmarEliminacion(Evento evento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Evento")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + evento.getTitulo() + "\"? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarBorrado(evento))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarBorrado(Evento evento) {
        String userId = sessionManager.getUserId();

        api.eliminarEvento("eq." + evento.getId(), "eq." + userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PanelOrganizador.this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                    cargarMisEventosOrganizados();
                } else {
                    Toast.makeText(PanelOrganizador.this, "No se pudo eliminar el evento: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PanelOrganizador.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREAR_EDITAR && resultCode == RESULT_OK) {
            cargarMisEventosOrganizados();
        }
    }
}