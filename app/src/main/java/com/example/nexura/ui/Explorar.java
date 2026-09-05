package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.adapter.EventoFeedAdapter;
import com.example.nexura.model.Evento;

import java.util.ArrayList;
import java.util.List;

public class Explorar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorar_mapa);

        // 1. Configurar RecyclerView y Adaptador
        RecyclerView rvFeed = findViewById(R.id.rvFeedEventos);
        if (rvFeed != null) {
            rvFeed.setLayoutManager(new LinearLayoutManager(this));

            List<Evento> lista = new ArrayList<>();
            lista.add(new Evento(
                    "1",
                    "Fiesta de la Longaniza 2026",
                    "El festival gastronómico y cultural más grande de la región. Música en vivo, gastronomía típica y puntos de experiencia Nexura verificados.",
                    "Festival",
                    "Sáb, 22 Octubre - 20:00",
                    "Chillán, Plaza de Armas",
                    "Municipalidad de Chillán",
                    "",
                    450,
                    1.2
            ));
            lista.add(new Evento(
                    "2",
                    "Neon Beats Electronic Festival",
                    "La fiesta de música electrónica más esperada de la zona centro sur. DJs en vivo, visuales inmersivas y recompensas exclusivas.",
                    "Festival",
                    "Vie, 14 Noviembre - 22:00",
                    "Parque Central",
                    "Productora Nova",
                    "",
                    600,
                    3.5
            ));
            lista.add(new Evento(
                    "3",
                    "Feria del Libro & Cómic Chillán",
                    "Stands de editoriales independientes, ilustradores, charlas de autores y torneos de cosplay con subida de nivel garantizada.",
                    "Cultura",
                    "Dom, 30 Octubre - 11:00",
                    "Centro Cultural Municipal",
                    "Cultura Ñuble",
                    "",
                    350,
                    0.8
            ));

            EventoFeedAdapter adapter = new EventoFeedAdapter(this, lista, evento -> {
                Intent intent = new Intent(Explorar.this, Detalle_Evento.class);
                intent.putExtra("EVENTO_SELECCIONADO", evento);
                startActivity(intent);
            });

            rvFeed.setAdapter(adapter);
        }

        // 2. Barra de Navegación Inferior (Navbar)
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
}