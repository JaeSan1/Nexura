package com.example.nexura;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        CardView cardFeatured = findViewById(R.id.cardFeaturedEvent);
        if (cardFeatured != null) {
            cardFeatured.setOnClickListener(v -> {
                Intent intentDetalle = new Intent(Home.this, Detalle_Evento.class);
                startActivity(intentDetalle);
            });
        }

        TextView navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intentPerfil = new Intent(Home.this, Perfil.class);
                startActivity(intentPerfil);
            });
        }
    }
}