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
import com.example.nexura.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Detalle_Evento extends AppCompatActivity {

    private boolean yaReclamado = false;
    private boolean isFavorite = false;
    private Evento eventoActual;
    private SupabaseApi api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evento);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
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

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Guardar o eliminar en Mis Eventos de Supabase
        if (btnSaveFavorite != null) {
            btnSaveFavorite.setOnClickListener(v -> {
                if (eventoActual == null || eventoActual.getId() == null) return;

                isFavorite = !isFavorite;
                if (isFavorite) {
                    btnSaveFavorite.setText("Guardado");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));

                    Map<String, Object> body = new HashMap<>();
                    body.put("usuario_id", sessionManager.getUserId());
                    body.put("evento_id", eventoActual.getId());
                    body.put("asistio", false);

                    api.agregarAsistencia(body).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Toast.makeText(Detalle_Evento.this, "Guardado en Mis Eventos", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                } else {
                    btnSaveFavorite.setText("Guardar");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

                    api.eliminarAsistencia("eq." + sessionManager.getUserId(), "eq." + eventoActual.getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Toast.makeText(Detalle_Evento.this, "Eliminado de Mis Eventos", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {}
                            });
                }
            });
        }

        tabInfo.setOnClickListener(v -> activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios));
        tabAvisos.setOnClickListener(v -> activarTab(tabAvisos, containerTabAvisos, tabInfo, containerTabInfo, tabComentarios, containerTabComentarios));
        tabComentarios.setOnClickListener(v -> {
            activarTab(tabComentarios, containerTabComentarios, tabInfo, containerTabInfo, tabAvisos, containerTabAvisos);
            cargarComentarios();
        });

        // Comentario vinculado a la cuenta logueada
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                String texto = etNewComment.getText().toString().trim();
                if (texto.isEmpty()) {
                    etNewComment.setError("Escribe un mensaje");
                    return;
                }

                if (eventoActual != null && eventoActual.getId() != null) {
                    Comentario nuevo = new Comentario(
                            eventoActual.getId(),
                            sessionManager.getUserId(),
                            sessionManager.getGamertag(),
                            texto
                    );

                    api.publicarComentario(nuevo).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(Detalle_Evento.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                                etNewComment.setText("");
                                cargarComentarios();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                }
            });
        }

        // Validación GPS
        if (btnClaimGps != null) {
            btnClaimGps.setOnClickListener(v -> {
                if (!yaReclamado) {
                    int xpPremio = (eventoActual != null) ? eventoActual.getXpRecompensa() : 250;

                    new AlertDialog.Builder(Detalle_Evento.this)
                            .setTitle("Asistencia Confirmada")
                            .setMessage("Has validado tu permanencia física. +" + xpPremio + " XP acreditados.")
                            .setPositiveButton("Reclamar", (dialog, which) -> {
                                yaReclamado = true;
                                btnClaimGps.setText("Asistencia Validada");
                                btnClaimGps.setEnabled(false);
                                btnClaimGps.setAlpha(0.5f);
                                if (tvGpsStatus != null) {
                                    tvGpsStatus.setText("Recompensa acreditada (+" + xpPremio + " XP)");
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
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {}

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void activarTab(TextView activa, View contActivo, TextView t2, View c2, TextView t3, View c3) {
        activa.setBackgroundResource(R.drawable.categoria);
        activa.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
        t2.setBackground(null);
        t2.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        t3.setBackground(null);
        t3.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

        contActivo.setVisibility(View.VISIBLE);
        c2.setVisibility(View.GONE);
        c3.setVisibility(View.GONE);
    }
}