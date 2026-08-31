package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;

public class Perfil extends AppCompatActivity {

    private TextView tvEquippedTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        TextView btnBack = findViewById(R.id.btnBackPerfil);
        tvEquippedTitle = findViewById(R.id.tvEquippedTitle);
        Button btnGoOrganizerPanel = findViewById(R.id.btnGoOrganizerPanel);
        Button btnEditCosmetics = findViewById(R.id.btnEditCosmetics);

        // Volver
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. ACCESO DIRECTO AL PANEL DE ORGANIZADOR
        if (btnGoOrganizerPanel != null) {
            btnGoOrganizerPanel.setOnClickListener(v -> {
                startActivity(new Intent(Perfil.this, PanelOrganizador.class));
            });
        }

        // 2. Ajustes / Menú de opciones
        if (btnEditCosmetics != null) {
            btnEditCosmetics.setOnClickListener(v -> {
                String[] opciones = {
                        "🏷️ Cambiar Título Equipado",
                        "🚪 Cerrar Sesión"
                };

                new AlertDialog.Builder(Perfil.this)
                        .setTitle("Ajustes de Perfil")
                        .setItems(opciones, (dialog, which) -> {
                            if (which == 0) {
                                abrirSelectorTitulos();
                            } else if (which == 1) {
                                cerrarSesion();
                            }
                        })
                        .show();
            });
        }
    }

    private void abrirSelectorTitulos() {
        String[] titulosDesbloqueados = {
                "FRIKI",
                "FIESTERO NOCTURNO",
                "CONQUISTADOR DE FESTIVALES"
        };

        new AlertDialog.Builder(Perfil.this)
                .setTitle("Equipar Título")
                .setItems(titulosDesbloqueados, (dialog, which) -> {
                    tvEquippedTitle.setText(" " + titulosDesbloqueados[which]);
                    Toast.makeText(this, "Título equipado", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void cerrarSesion() {
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}