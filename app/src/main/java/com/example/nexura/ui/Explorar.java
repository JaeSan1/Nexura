package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nexura.R;
import com.example.nexura.adapter.EventoFeedAdapter;
import com.example.nexura.model.Evento;
import java.util.ArrayList;
import java.util.List;

public class Explorar extends AppCompatActivity {

    private RecyclerView rvExplorarFeed;
    private EventoFeedAdapter adapter;
    private List<Evento> listaEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorar_mapa);

        rvExplorarFeed = findViewById(R.id.rvExplorarFeed);
        rvExplorarFeed.setLayoutManager(new LinearLayoutManager(this));

        TextView btnOpenFilters = findViewById(R.id.btnOpenFilters);
        if (btnOpenFilters != null) {
            btnOpenFilters.setOnClickListener(v -> {
                Toast.makeText(this, "Filtros activos próximamente", Toast.LENGTH_SHORT).show();
            });
        }

        configurarNavegacionInferior();
        cargarDatosPrueba();
    }

    private void cargarDatosPrueba() {
        listaEventos = new ArrayList<>();
        listaEventos.add(new Evento("1", "Fiesta de la Longaniza 2026", "Vuelve la fiesta gastronómica de Ñuble! Música folclórica en vivo y artesanías.", "Festivales", "Sáb, 22 Oct", "Plaza de Armas, Chillán", "MuniChillan_Cultura", null, 450, 1.2));
        listaEventos.add(new Evento("2", "Neon Beats Festival 2026", "Los mejores DJs de música electrónica del circuito nacional con zona VIP.", "Música", "Vie, 14 Nov", "Parque Central", "Beats_Underground", null, 600, 3.5));
        listaEventos.add(new Evento("3", "Torneo Gamer Ñuble Arena", "Competencia de Esports y torneos retro con medallas y premios.", "Gaming", "Dom, 05 Dic", "Gimnasio Municipal", "GamingClub_CL", null, 300, 0.8));

        adapter = new EventoFeedAdapter(this, listaEventos, evento -> {
            Intent intent = new Intent(Explorar.this, Detalle_Evento.class);
            intent.putExtra("EVENTO_SELECCIONADO", evento);
            startActivity(intent);
        });

        rvExplorarFeed.setAdapter(adapter);
    }

    private void configurarNavegacionInferior() {
        TextView navHome = findViewById(R.id.navHome);
        TextView navEvents = findViewById(R.id.navEvents);
        TextView navProfile = findViewById(R.id.navProfile);

        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            finish();
        });
        if (navEvents != null) navEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MisEventos.class));
            finish();
        });
        if (navProfile != null) navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, Perfil.class));
            finish();
        });
    }
}