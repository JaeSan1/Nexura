package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity {

    private CardView card1, card2, card3;
    private TextView tvTitle1, tvTitle2, tvTitle3;
    private TextView tvXp1, tvXp2, tvXp3;
    private List<Evento> listaDestacados;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        ImageView ivAvatarHeader = findViewById(R.id.ivAvatarHeader);
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);

        // Tarjetas del Carrusel
        card1 = findViewById(R.id.cardFeaturedEvent1);
        card2 = findViewById(R.id.cardFeaturedEvent2);
        card3 = findViewById(R.id.cardFeaturedEvent3);

        // Textos dentro de las tarjetas (IDs estándar en tu layout de Home)
        tvTitle1 = findViewById(R.id.tvTitleEvent1);
        tvTitle2 = findViewById(R.id.tvTitleEvent2);
        tvTitle3 = findViewById(R.id.tvTitleEvent3);
        tvXp1 = findViewById(R.id.tvXpEvent1);
        tvXp2 = findViewById(R.id.tvXpEvent2);
        tvXp3 = findViewById(R.id.tvXpEvent3);

        // Navegación Navbar
        TextView navExplore = findViewById(R.id.navExplore);
        TextView navMyEvents = findViewById(R.id.navMyEvents);
        TextView navProfile = findViewById(R.id.navProfile);

        // 1. Cargar datos reales desde Supabase
        cargarCarruselDestacados();

        // 2. Clics en tarjetas del carrusel con paso de datos al detalle
        if (card1 != null) {
            card1.setOnClickListener(v -> abrirDetalle(0));
        }
        if (card2 != null) {
            card2.setOnClickListener(v -> abrirDetalle(1));
        }
        if (card3 != null) {
            card3.setOnClickListener(v -> abrirDetalle(2));
        }

        // 3. Navegación a Perfil
        if (ivAvatarHeader != null) {
            ivAvatarHeader.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }

        // 4. Navegación a Explorar y Mis Eventos
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> startActivity(new Intent(Home.this, Explorar.class)));
        }
        if (navMyEvents != null) {
            navMyEvents.setOnClickListener(v -> startActivity(new Intent(Home.this, MisEventos.class)));
        }

        // 5. Botón Flotante Creador (+) -> Crear Evento
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> startActivity(new Intent(Home.this, CrearEvento.class)));
        }

        // 6. Buscador rápido conectado
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        Intent intent = new Intent(Home.this, Explorar.class);
                        intent.putExtra("FILTRO_BUSQUEDA", query);
                        startActivity(intent);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void cargarCarruselDestacados() {
        api.obtenerEventosDestacados().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaDestacados = response.body();
                    actualizarUIEventos(listaDestacados);
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(Home.this, "Aviso: Sin conexión a eventos destacados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUIEventos(List<Evento> eventos) {
        if (eventos.size() > 0 && tvTitle1 != null) {
            tvTitle1.setText(eventos.get(0).getTitulo());
            if (tvXp1 != null) tvXp1.setText("+" + eventos.get(0).getXpRecompensa() + " XP");
        }
        if (eventos.size() > 1 && tvTitle2 != null) {
            tvTitle2.setText(eventos.get(1).getTitulo());
            if (tvXp2 != null) tvXp2.setText("+" + eventos.get(1).getXpRecompensa() + " XP");
        }
        if (eventos.size() > 2 && tvTitle3 != null) {
            tvTitle3.setText(eventos.get(2).getTitulo());
            if (tvXp3 != null) tvXp3.setText("+" + eventos.get(2).getXpRecompensa() + " XP");
        }
    }

    private void abrirDetalle(int index) {
        if (listaDestacados != null && listaDestacados.size() > index) {
            Intent intent = new Intent(Home.this, Detalle_Evento.class);
            intent.putExtra("EVENTO_SELECCIONADO", listaDestacados.get(index));
            startActivity(intent);
        } else {
            // Si la red falló, navega al detalle genérico
            startActivity(new Intent(Home.this, Detalle_Evento.class));
        }
    }
}