package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.nexura.R;

public class MisEventos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_eventos);

        TextView tabPorAsistir = findViewById(R.id.tabPorAsistir);
        TextView tabHistorial = findViewById(R.id.tabHistorial);
        LinearLayout containerPorAsistir = findViewById(R.id.containerPorAsistir);
        LinearLayout containerHistorial = findViewById(R.id.containerHistorial);

        CardView cardSaved1 = findViewById(R.id.cardSaved1);
        CardView cardSaved2 = findViewById(R.id.cardSaved2);

        TextView navHome = findViewById(R.id.navHome);
        TextView navExplore = findViewById(R.id.navExplore);
        TextView navProfile = findViewById(R.id.navProfile);

        // 1. Alternar pestañas
        tabPorAsistir.setOnClickListener(v -> {
            tabPorAsistir.setBackgroundResource(R.drawable.categoria);
            tabPorAsistir.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            tabHistorial.setBackground(null);
            tabHistorial.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

            containerPorAsistir.setVisibility(View.VISIBLE);
            containerHistorial.setVisibility(View.GONE);
        });

        tabHistorial.setOnClickListener(v -> {
            tabHistorial.setBackgroundResource(R.drawable.categoria);
            tabHistorial.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            tabPorAsistir.setBackground(null);
            tabPorAsistir.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

            containerHistorial.setVisibility(View.VISIBLE);
            containerPorAsistir.setVisibility(View.GONE);
        });

        // 2. Clic en eventos guardados -> Detalle_Evento
        if (cardSaved1 != null) {
            cardSaved1.setOnClickListener(v -> startActivity(new Intent(MisEventos.this, Detalle_Evento.class)));
        }
        if (cardSaved2 != null) {
            cardSaved2.setOnClickListener(v -> startActivity(new Intent(MisEventos.this, Detalle_Evento.class)));
        }

        // 3. Navbar
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(MisEventos.this, Home.class));
                finish();
            });
        }
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                startActivity(new Intent(MisEventos.this, ExplorarMapa.class));
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
}