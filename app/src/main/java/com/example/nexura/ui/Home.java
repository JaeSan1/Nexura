package com.example.nexura.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nexura.R;
import com.example.nexura.Viewmodel.EventoViewModel;
import com.example.nexura.model.Evento;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity {

    private SessionManager sessionManager;
    private EventoViewModel eventoViewModel;
    private SupabaseApi api;

    // Componentes de la UI
    private ImageButton btnAvisosHeader;
    private ShapeableImageView ivAvatarHeader;
    private EditText etSearch;

    private TextView btnCategoryFestivales, btnCategoryConferencias, btnCategoryDeportes;
    private TextView categoriaSeleccionadaActual = null;
    private String categoriaFiltroActual = "";
    private String textoBusquedaActual = "";

    private CardView cardFeaturedEvent1, cardFeaturedEvent2;
    private TextView tvTitleEvent1, tvLocationEvent1, tvXpEvent1;
    private TextView tvTitleEvent2, tvLocationEvent2, tvXpEvent2;

    private FloatingActionButton btnCreateEvent;
    private TextView navExplore, navMyEvents, navProfile;

    private final List<Evento> listaCompletaEventos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        eventoViewModel = new ViewModelProvider(this).get(EventoViewModel.class);

        // 1. Header
        btnAvisosHeader = findViewById(R.id.btnAvisosHeader);
        ivAvatarHeader = findViewById(R.id.ivAvatarHeader);

        if (btnAvisosHeader != null) {
            btnAvisosHeader.setOnClickListener(v -> startActivity(new Intent(Home.this, Notificaciones.class)));
        }

        if (ivAvatarHeader != null) {
            ivAvatarHeader.setOnClickListener(v -> startActivity(new Intent(Home.this, Perfil.class)));
        }

        // 2. Buscador en tiempo real
        etSearch = findViewById(R.id.etSearch);
        configurarBuscador();

        // 3. Categorías
        btnCategoryFestivales = findViewById(R.id.btnCategoryFestivales);
        btnCategoryConferencias = findViewById(R.id.btnCategoryConferencias);
        btnCategoryDeportes = findViewById(R.id.btnCategoryDeportes);
        configurarFiltrosCategorias();

        // 4. Tarjetas Destacadas
        cardFeaturedEvent1 = findViewById(R.id.cardFeaturedEvent1);
        tvTitleEvent1 = findViewById(R.id.tvTitleEvent1);
        tvLocationEvent1 = findViewById(R.id.tvLocationEvent1);
        tvXpEvent1 = findViewById(R.id.tvXpEvent1);

        cardFeaturedEvent2 = findViewById(R.id.cardFeaturedEvent2);
        tvTitleEvent2 = findViewById(R.id.tvTitleEvent2);
        tvLocationEvent2 = findViewById(R.id.tvLocationEvent2);
        tvXpEvent2 = findViewById(R.id.tvXpEvent2);

        // 5. Botón Flotante Crear Evento
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> startActivity(new Intent(Home.this, CrearEvento.class)));
        }

        // 6. Navegación Inferior
        navExplore = findViewById(R.id.navExplore);
        navMyEvents = findViewById(R.id.navMyEvents);
        navProfile = findViewById(R.id.navProfile);
        configurarNavegacionInferior();

        // 7. Observar LiveData del ViewModel
        eventoViewModel.getEventos().observe(this, eventos -> {
            listaCompletaEventos.clear();
            if (eventos != null) {
                listaCompletaEventos.addAll(eventos);
            }
            aplicarFiltrosLocales();
        });

        eventoViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(Home.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar eventos y avatar al volver de crear, editar, eliminar o editar perfil
        eventoViewModel.cargarEventos();
        cargarAvatarHeader();
    }

    private void cargarAvatarHeader() {
        if (ivAvatarHeader == null) return;
        String userId = sessionManager.getUserId();
        if (TextUtils.isEmpty(userId)) return;

        api.obtenerPerfilPorId("eq." + userId).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String urlAvatar = response.body().get(0).getFotoPerfilUrl();
                    if (!TextUtils.isEmpty(urlAvatar)) {
                        Glide.with(Home.this)
                                .load(urlAvatar)
                                .placeholder(R.drawable.ic_logo_nexura)
                                .error(R.drawable.ic_logo_nexura)
                                .circleCrop()
                                .into(ivAvatarHeader);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {}
        });
    }

    private void configurarBuscador() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusquedaActual = s != null ? s.toString().trim().toLowerCase(Locale.ROOT) : "";
                aplicarFiltrosLocales();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarFiltrosCategorias() {
        resetearEstilosCategorias();

        if (btnCategoryFestivales != null) {
            btnCategoryFestivales.setOnClickListener(v -> alternarCategoria(btnCategoryFestivales, "Festivales"));
        }
        if (btnCategoryConferencias != null) {
            btnCategoryConferencias.setOnClickListener(v -> alternarCategoria(btnCategoryConferencias, "Conferencias"));
        }
        if (btnCategoryDeportes != null) {
            btnCategoryDeportes.setOnClickListener(v -> alternarCategoria(btnCategoryDeportes, "Deportes"));
        }
    }

    private void resetearEstilosCategorias() {
        TextView[] botones = {btnCategoryFestivales, btnCategoryConferencias, btnCategoryDeportes};
        for (TextView btn : botones) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.categoria);
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            }
        }
    }

    private void alternarCategoria(TextView categoriaPulsada, String nombreCategoria) {
        if (categoriaSeleccionadaActual == categoriaPulsada) {
            // Si toca la misma categoría activa, desactivar filtro para mostrar todos
            resetearEstilosCategorias();
            categoriaSeleccionadaActual = null;
            categoriaFiltroActual = "";
        } else {
            resetearEstilosCategorias();
            categoriaPulsada.setBackgroundResource(R.drawable.badge);
            categoriaPulsada.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            categoriaSeleccionadaActual = categoriaPulsada;
            categoriaFiltroActual = nombreCategoria.toLowerCase(Locale.ROOT);
        }

        aplicarFiltrosLocales();
    }

    private void aplicarFiltrosLocales() {
        List<Evento> filtrados = new ArrayList<>();

        for (Evento ev : listaCompletaEventos) {
            boolean coincideCategoria = true;
            if (!TextUtils.isEmpty(categoriaFiltroActual)) {
                String catEv = ev.getCategoria() != null ? ev.getCategoria().toLowerCase(Locale.ROOT) : "";
                coincideCategoria = catEv.contains(categoriaFiltroActual);
            }

            boolean coincideBusqueda = true;
            if (!TextUtils.isEmpty(textoBusquedaActual)) {
                String titulo = ev.getTitulo() != null ? ev.getTitulo().toLowerCase(Locale.ROOT) : "";
                String ubicacion = ev.getUbicacion() != null ? ev.getUbicacion().toLowerCase(Locale.ROOT) : "";
                String ciudad = ev.getCiudad() != null ? ev.getCiudad().toLowerCase(Locale.ROOT) : "";
                String org = ev.getOrganizadorNombre() != null ? ev.getOrganizadorNombre().toLowerCase(Locale.ROOT) : "";

                coincideBusqueda = titulo.contains(textoBusquedaActual)
                        || ubicacion.contains(textoBusquedaActual)
                        || ciudad.contains(textoBusquedaActual)
                        || org.contains(textoBusquedaActual);
            }

            if (coincideCategoria && coincideBusqueda) {
                filtrados.add(ev);
            }
        }

        actualizarEventosDestacados(filtrados);
    }

    private void actualizarEventosDestacados(List<Evento> lista) {
        if (lista == null || lista.isEmpty()) {
            if (cardFeaturedEvent1 != null) cardFeaturedEvent1.setVisibility(View.GONE);
            if (cardFeaturedEvent2 != null) cardFeaturedEvent2.setVisibility(View.GONE);
            return;
        }

        if (cardFeaturedEvent1 != null) {
            cardFeaturedEvent1.setVisibility(View.VISIBLE);
            Evento e1 = lista.get(0);
            if (tvTitleEvent1 != null) {
                tvTitleEvent1.setText(e1.getTitulo());
                tvTitleEvent1.setShadowLayer(6f, 0f, 2f, Color.BLACK);
            }
            if (tvLocationEvent1 != null) {
                tvLocationEvent1.setText(e1.getUbicacion());
                tvLocationEvent1.setShadowLayer(6f, 0f, 2f, Color.BLACK);
            }
            if (tvXpEvent1 != null) tvXpEvent1.setText("+" + e1.getXpRecompensa() + " XP");
            cargarPortadaEnCard(cardFeaturedEvent1, e1.getPortadaUrl());
            cardFeaturedEvent1.setOnClickListener(v -> abrirDetalleEvento(e1));
        }

        if (cardFeaturedEvent2 != null) {
            if (lista.size() >= 2) {
                cardFeaturedEvent2.setVisibility(View.VISIBLE);
                Evento e2 = lista.get(1);
                if (tvTitleEvent2 != null) {
                    tvTitleEvent2.setText(e2.getTitulo());
                    tvTitleEvent2.setShadowLayer(6f, 0f, 2f, Color.BLACK);
                }
                if (tvLocationEvent2 != null) {
                    tvLocationEvent2.setText(e2.getUbicacion());
                    tvLocationEvent2.setShadowLayer(6f, 0f, 2f, Color.BLACK);
                }
                if (tvXpEvent2 != null) tvXpEvent2.setText("+" + e2.getXpRecompensa() + " XP");
                cargarPortadaEnCard(cardFeaturedEvent2, e2.getPortadaUrl());
                cardFeaturedEvent2.setOnClickListener(v -> abrirDetalleEvento(e2));
            } else {
                cardFeaturedEvent2.setVisibility(View.GONE);
            }
        }
    }

    private void cargarPortadaEnCard(CardView card, String urlPortada) {
        card.setPreventCornerOverlap(false);

        ImageView ivFondo = card.findViewWithTag("bg_portada_full");

        if (ivFondo == null) {
            ivFondo = new ImageView(this);
            ivFondo.setTag("bg_portada_full");
            ivFondo.setScaleType(ImageView.ScaleType.CENTER_CROP);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            // Insertar directamente en el CardView (índice 0) para ocupar todo el cuadro sin márgenes
            card.addView(ivFondo, 0, params);
        }

        if (!TextUtils.isEmpty(urlPortada)) {
            ivFondo.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(urlPortada)
                    .centerCrop()
                    .into(ivFondo);
        } else {
            ivFondo.setVisibility(View.GONE);
        }
    }

    private ImageView buscarImageViewEnCard(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ImageView) {
                return (ImageView) child;
            } else if (child instanceof ViewGroup) {
                ImageView encontrada = buscarImageViewEnCard((ViewGroup) child);
                if (encontrada != null) return encontrada;
            }
        }
        return null;
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