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

public class Detalle_Evento extends AppCompatActivity {

    private boolean yaReclamado = false;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evento);

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
        TextView tvGpsStatus = findViewById(R.id.tvGpsStatus);
        Button btnClaimGps = findViewById(R.id.btnClaimGps);

        // Pestañas (Tabs)
        TextView tabInfo = findViewById(R.id.tabInfo);
        TextView tabAvisos = findViewById(R.id.tabAvisos);
        TextView tabComentarios = findViewById(R.id.tabComentarios);

        // Contenedores
        LinearLayout containerTabInfo = findViewById(R.id.containerTabInfo);
        LinearLayout containerTabAvisos = findViewById(R.id.containerTabAvisos);
        LinearLayout containerTabComentarios = findViewById(R.id.containerTabComentarios);

        // Comentarios
        EditText etNewComment = findViewById(R.id.etNewComment);
        Button btnSendComment = findViewById(R.id.btnSendComment);

        // 1. Flecha Volver
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Guardar en Mis Eventos (Favorito)
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

        // 3. Lógica de Pestañas (Tabs)
        tabInfo.setOnClickListener(v -> {
            activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios);
        });

        tabAvisos.setOnClickListener(v -> {
            activarTab(tabAvisos, containerTabAvisos, tabInfo, containerTabInfo, tabComentarios, containerTabComentarios);
        });

        tabComentarios.setOnClickListener(v -> {
            activarTab(tabComentarios, containerTabComentarios, tabInfo, containerTabInfo, tabAvisos, containerTabAvisos);
        });

        // 4. Enviar Comentario
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                String comentario = etNewComment.getText().toString().trim();
                if (!comentario.isEmpty()) {
                    Toast.makeText(this, "Comentario publicado en la comunidad", Toast.LENGTH_SHORT).show();
                    etNewComment.setText("");
                } else {
                    etNewComment.setError("Escribe algo antes de enviar");
                }
            });
        }

        // 5. Reclamar Asistencia GPS (+XP)
        if (btnClaimGps != null) {
            btnClaimGps.setOnClickListener(v -> {
                if (!yaReclamado) {
                    new AlertDialog.Builder(Detalle_Evento.this)
                            .setTitle("¡Asistencia Confirmada! 🏅")
                            .setMessage("Has validado tu permanencia física en la Fiesta de la Longaniza.\n\n+450 XP acreditados a tu vitrina.")
                            .setPositiveButton("Reclamar Recompensa", (dialog, which) -> {
                                yaReclamado = true;
                                btnClaimGps.setText("✓ Asistencia Validada");
                                btnClaimGps.setEnabled(false);
                                btnClaimGps.setAlpha(0.5f);
                                if (tvGpsStatus != null) {
                                    tvGpsStatus.setText("🎉 Recompensa reclamada con éxito (+450 XP)");
                                }
                                Toast.makeText(Detalle_Evento.this, "+450 XP ganados", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });
        }
    }

    // Método auxiliar para alternar estilos de pestañas
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