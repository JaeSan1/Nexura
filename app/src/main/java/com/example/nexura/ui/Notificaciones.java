package com.example.nexura.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.adapter.NotificacionAdapter;
import com.example.nexura.model.Notificacion;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notificaciones extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private NotificacionAdapter adapter;
    private List<Notificacion> listaNotificaciones;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackNotificaciones);
        rvNotificaciones = findViewById(R.id.rvNotificaciones);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Configurar RecyclerView
        if (rvNotificaciones != null) {
            rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
            listaNotificaciones = new ArrayList<>();
            adapter = new NotificacionAdapter(this, listaNotificaciones, notificacion -> {
                Toast.makeText(this, notificacion.getTitulo(), Toast.LENGTH_SHORT).show();
            });
            rvNotificaciones.setAdapter(adapter);
        }

        // 2. Cargar avisos y alertas desde Supabase
        cargarNotificacionesDesdeNube();
    }

    private void cargarNotificacionesDesdeNube() {
        api.obtenerNotificaciones().enqueue(new Callback<List<Notificacion>>() {
            @Override
            public void onResponse(Call<List<Notificacion>> call, Response<List<Notificacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaNotificaciones.clear();
                    listaNotificaciones.addAll(response.body());
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(Notificaciones.this, "No se pudieron obtener las alertas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notificacion>> call, Throwable t) {
                Toast.makeText(Notificaciones.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}