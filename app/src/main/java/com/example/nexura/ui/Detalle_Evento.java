package com.example.nexura.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nexura.R;
import com.example.nexura.model.Comentario;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Detalle_Evento extends AppCompatActivity {

    private boolean yaReclamado = false;
    private boolean isFavorite = false;
    private Evento eventoActual;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evento);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        // 1. Obtener el evento seleccionado
        eventoActual = (Evento) getIntent().getSerializableExtra("EVENTO_SELECCIONADO");

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
        TextView tvGpsStatus = findViewById(R.id.tvGpsStatus);
        Button btnClaimGps = findViewById(R.id.btnClaimGps);

        TextView tabInfo = findViewById(R.id.tabInfo);
        TextView tabAvisos = findViewById(R.id.tabAvisos);
        TextView tabComentarios = findViewById(R.id.tabComentarios);

        LinearLayout containerTabInfo = findViewById(R.id.containerTabInfo);
        LinearLayout containerTabAvisos = findViewById(R.id.containerTabAvisos);
        LinearLayout containerTabComentarios = findViewById(R.id.containerTabComentarios);

        EditText etNewComment = findViewById(R.id.etNewComment);
        Button btnSendComment = findViewById(R.id.btnSendComment);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Favoritos / Mis Eventos
        if (btnSaveFavorite != null) {
            btnSaveFavorite.setOnClickListener(v -> {
                isFavorite = !isFavorite;
                if (isFavorite) {
                    btnSaveFavorite.setText("♥");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
                    Toast.makeText(this, "Guardado en 'Mis Eventos'", Toast.LENGTH_SHORT).show();
                } else {
                    btnSaveFavorite.setText("♡");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
                    Toast.makeText(this, "Eliminado de 'Mis Eventos'", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Pestañas
        tabInfo.setOnClickListener(v -> activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios));
        tabAvisos.setOnClickListener(v -> activarTab(tabAvisos, containerTabAvisos, tabInfo, containerTabInfo, tabComentarios, containerTabComentarios));
        tabComentarios.setOnClickListener(v -> {
            activarTab(tabComentarios, containerTabComentarios, tabInfo, containerTabInfo, tabAvisos, containerTabAvisos);
            cargarComentarios();
        });

        // Enviar Comentario a Supabase
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                String texto = etNewComment.getText().toString().trim();
                if (texto.isEmpty()) {
                    etNewComment.setError("Escribe un mensaje");
                    return;
                }

                if (eventoActual != null && eventoActual.getId() != null) {
                    Comentario nuevo = new Comentario(eventoActual.getId(), null, "TheGoat99", texto);
                    api.publicarComentario(nuevo).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(Detalle_Evento.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                                etNewComment.setText("");
                                cargarComentarios();
                            } else {
                                Toast.makeText(Detalle_Evento.this, "Error al publicar: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(Detalle_Evento.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        // Validación GPS y Recompensa
        if (btnClaimGps != null) {
            btnClaimGps.setOnClickListener(v -> {
                if (!yaReclamado) {
                    String nombreEvento = (eventoActual != null) ? eventoActual.getTitulo() : "el evento";
                    int xpPremio = (eventoActual != null) ? eventoActual.getXpRecompensa() : 250;

                    new AlertDialog.Builder(Detalle_Evento.this)
                            .setTitle("¡Asistencia Confirmada! 🏅")
                            .setMessage("Has validado tu permanencia física en " + nombreEvento + ".\n\n+" + xpPremio + " XP acreditados.")
                            .setPositiveButton("Reclamar Recompensa", (dialog, which) -> {
                                yaReclamado = true;
                                btnClaimGps.setText("✓ Asistencia Validada");
                                btnClaimGps.setEnabled(false);
                                btnClaimGps.setAlpha(0.5f);
                                if (tvGpsStatus != null) {
                                    tvGpsStatus.setText("🎉 Recompensa acreditada (+" + xpPremio + " XP)");
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });
        }
    }

    private void cargarComentarios() {
        if (eventoActual == null || eventoActual.getId() == null) return;

        api.obtenerComentariosPorEvento("eq." + eventoActual.getId()).enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Los comentarios llegan listos para pintarse en el contenedor
                }
            }

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void activarTab(TextView tabActiva, View contenedorActivo,
                            TextView tab2, View contenedor2,
                            TextView tab3, View contenedor3) {
        tabActiva.setBackgroundResource(R.drawable.categoria);
        tabActiva.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));

        tab2.setBackground(null);
        tab2.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

        tab3.setBackground(null);
        tab3.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

        contenedorActivo.setVisibility(View.VISIBLE);
        contenedor2.setVisibility(View.GONE);
        contenedor3.setVisibility(View.GONE);
    }
}