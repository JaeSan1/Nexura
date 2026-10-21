package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.nexura.R;
import com.example.nexura.Viewmodel.EventoViewModel;
import com.example.nexura.model.Evento;
import com.example.nexura.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class Home extends AppCompatActivity {

    private SessionManager sessionManager;
    private EventoViewModel eventoViewModel;

    // DECLARACIÓN DE VARIABLES
    private ImageButton btnAvisosHeader;
    private ShapeableImageView ivAvatarHeader;
    private EditText etSearch;

    private TextView btnCategoryFestivales, btnCategoryConferencias, btnCategoryDeportes;
    private TextView categoriaSeleccionadaActual;

    private CardView cardFeaturedEvent1, cardFeaturedEvent2;
    private TextView tvTitleEvent1, tvLocationEvent1, tvXpEvent1;
    private TextView tvTitleEvent2, tvLocationEvent2, tvXpEvent2;

    private FloatingActionButton btnCreateEvent;
    private TextView navExplore, navMyEvents, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        eventoViewModel = new ViewModelProvider(this).get(EventoViewModel.class);

        // 1. Vincular Header
        btnAvisosHeader = findViewById(R.id.btnAvisosHeader);
        ivAvatarHeader = findViewById(R.id.ivAvatarHeader);

        if (btnAvisosHeader != null) {
            btnAvisosHeader.setOnClickListener(v -> {
                startActivity(new Intent(Home.this, Notificaciones.class));
            });
        }

        if (ivAvatarHeader != null) {
            ivAvatarHeader.setOnClickListener(v -> {
                startActivity(new Intent(Home.this, Perfil.class));
            });
        }

        // 2. Buscador Principal
        etSearch = findViewById(R.id.etSearch);

        // 3. Vincular Categorías
        btnCategoryFestivales = findViewById(R.id.btnCategoryFestivales);
        btnCategoryConferencias = findViewById(R.id.btnCategoryConferencias);
        btnCategoryDeportes = findViewById(R.id.btnCategoryDeportes);

        categoriaSeleccionadaActual = btnCategoryFestivales;
        configurarFiltrosCategorias();

        // 4. Vincular Tarjetas Destacadas
        cardFeaturedEvent1 = findViewById(R.id.cardFeaturedEvent1);
        tvTitleEvent1 = findViewById(R.id.tvTitleEvent1);
        tvLocationEvent1 = findViewById(R.id.tvLocationEvent1);
        tvXpEvent1 = findViewById(R.id.tvXpEvent1);

        cardFeaturedEvent2 = findViewById(R.id.cardFeaturedEvent2);
        tvTitleEvent2 = findViewById(R.id.tvTitleEvent2);
        tvLocationEvent2 = findViewById(R.id.tvLocationEvent2);
        tvXpEvent2 = findViewById(R.id.tvXpEvent2);

        // 5. Botón Flotante para Crear Evento
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> {
                startActivity(new Intent(Home.this, CrearEvento.class));
            });
        }

        // 6. Navegación Inferior
        navExplore = findViewById(R.id.navExplore);
        navMyEvents = findViewById(R.id.navMyEvents);
        navProfile = findViewById(R.id.navProfile);
        configurarNavegacionInferior();

        // 7. Suscribirse a LiveData para actualizar la UI con datos de Supabase
        eventoViewModel.getEventos().observe(this, this::actualizarEventosDestacados);

        // Cargar eventos desde el ViewModel
        eventoViewModel.cargarEventos();
    }

    private void configurarFiltrosCategorias() {
        if (btnCategoryFestivales != null) {
            btnCategoryFestivales.setOnClickListener(v -> seleccionarCategoria(btnCategoryFestivales, "Festivales"));
        }
        if (btnCategoryConferencias != null) {
            btnCategoryConferencias.setOnClickListener(v -> seleccionarCategoria(btnCategoryConferencias, "Conferencias"));
        }
        if (btnCategoryDeportes != null) {
            btnCategoryDeportes.setOnClickListener(v -> seleccionarCategoria(btnCategoryDeportes, "Deportes"));
        }
    }

    private void seleccionarCategoria(TextView categoriaPulsada, String nombreCategoria) {
        if (categoriaSeleccionadaActual != null) {
            categoriaSeleccionadaActual.setBackgroundResource(R.drawable.categoria);
            categoriaSeleccionadaActual.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        }

        categoriaPulsada.setBackgroundResource(R.drawable.badge);
        categoriaPulsada.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
        categoriaSeleccionadaActual = categoriaPulsada;

        eventoViewModel.filtrarPorCategoria(nombreCategoria);
    }

    private void actualizarEventosDestacados(List<Evento> lista) {
        if (lista == null || lista.isEmpty()) return;

        if (lista.size() >= 1 && cardFeaturedEvent1 != null) {
            Evento e1 = lista.get(0);
            if (tvTitleEvent1 != null) tvTitleEvent1.setText(e1.getTitulo());
            if (tvLocationEvent1 != null) tvLocationEvent1.setText(e1.getUbicacion());

            cardFeaturedEvent1.setOnClickListener(v -> abrirDetalleEvento(e1));
        }

        if (lista.size() >= 2 && cardFeaturedEvent2 != null) {
            Evento e2 = lista.get(1);
            if (tvTitleEvent2 != null) tvTitleEvent2.setText(e2.getTitulo());
            if (tvLocationEvent2 != null) tvLocationEvent2.setText(e2.getUbicacion());

            cardFeaturedEvent2.setOnClickListener(v -> abrirDetalleEvento(e2));
        }
    }

    private void abrirDetalleEvento(Evento evento) {
        Intent intent = new Intent(Home.this, Detalle_Evento.class);
        intent.putExtra("EVENTO_SELECCIONADO", evento);
        startActivity(intent);
    }

    private void configurarNavegacionInferior() {
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> startActivity(new Intent(Home.this, Explorar.class)));
        }
        if (navMyEvents != null) {
            navMyEvents.setOnClickListener(v -> startActivity(new Intent(Home.this, MisEventos.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }
    }
}