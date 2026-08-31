package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.nexura.R;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView ivAvatarHeader = findViewById(R.id.ivAvatarHeader);
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);

        // Tarjetas del Carrusel
        CardView card1 = findViewById(R.id.cardFeaturedEvent1);
        CardView card2 = findViewById(R.id.cardFeaturedEvent2);
        CardView card3 = findViewById(R.id.cardFeaturedEvent3);

        // Navegación Navbar
        TextView navExplore = findViewById(R.id.navExplore);
        TextView navMyEvents = findViewById(R.id.navMyEvents);
        TextView navProfile = findViewById(R.id.navProfile);

        // 1. Clics en los eventos destacados del carrusel -> Detalle_Evento
        if (card1 != null) {
            card1.setOnClickListener(v -> startActivity(new Intent(Home.this, Detalle_Evento.class)));
        }
        if (card2 != null) {
            card2.setOnClickListener(v -> startActivity(new Intent(Home.this, Detalle_Evento.class)));
        }
        if (card3 != null) {
            card3.setOnClickListener(v -> startActivity(new Intent(Home.this, Detalle_Evento.class)));
        }

        // 2. Navegación a Perfil (desde Avatar o Navbar)
        if (ivAvatarHeader != null) {
            ivAvatarHeader.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }

        // 3. Navegación a Explorar (Mapa)
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> startActivity(new Intent(Home.this, Explorar.class)));
        }

        // 4. Navegación a Mis Eventos
        if (navMyEvents != null) {
            navMyEvents.setOnClickListener(v -> startActivity(new Intent(Home.this, MisEventos.class)));
        }

        // 5. Botón Flotante Creador (+) -> Crear Evento
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> startActivity(new Intent(Home.this, CrearEvento.class)));
        }

        // 6. Buscador rápido
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    Toast.makeText(Home.this, "Buscando: " + query, Toast.LENGTH_SHORT).show();
                }
                return false;
            });
        }
    }
}