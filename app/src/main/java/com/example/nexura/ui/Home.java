package com.example.nexura.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

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
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.extractor.DefaultExtractorsFactory;

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

    // Reproductor Multimedia Nativo (ExoPlayer)
    private ExoPlayer exoPlayer;
    private PlayerView playerViewHome;

    // Efectos de Sonido (SoundPool)
    private SoundPool soundPool;
    private int sonidoClickId;
    private boolean sonidoCargado = false;

    private final List<Evento> listaCompletaEventos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        eventoViewModel = new ViewModelProvider(this).get(EventoViewModel.class);

        // 0. Inicializar efectos de sonido
        configurarSonidos();

        // 1. Header
        btnAvisosHeader = findViewById(R.id.btnAvisosHeader);
        ivAvatarHeader = findViewById(R.id.ivAvatarHeader);

        if (btnAvisosHeader != null) {
            btnAvisosHeader.setOnClickListener(v -> {
                reproducirSonidoClick(v);
                startActivity(new Intent(Home.this, Notificaciones.class));
            });
        }

        if (ivAvatarHeader != null) {
            ivAvatarHeader.setOnClickListener(v -> {
                reproducirSonidoClick(v);
                startActivity(new Intent(Home.this, Perfil.class));
            });
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
            btnCreateEvent.setOnClickListener(v -> {
                reproducirSonidoClick(v);
                startActivity(new Intent(Home.this, CrearEvento.class));
            });
        }

        // 6. Navegación Inferior
        navExplore = findViewById(R.id.navExplore);
        navMyEvents = findViewById(R.id.navMyEvents);
        navProfile = findViewById(R.id.navProfile);
        configurarNavegacionInferior();

        // 7. Reproductor Multimedia Nativo
        playerViewHome = findViewById(R.id.playerViewHome);
        inicializarVideoHome();

        // 8. Observar LiveData del ViewModel
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

    private void configurarSonidos() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                sonidoCargado = true;
            }
        });

        sonidoClickId = soundPool.load(this, R.raw.click_boton, 1);
    }

    private void reproducirSonidoClick(View view) {
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        if (soundPool != null && sonidoCargado) {
            soundPool.play(sonidoClickId, 1f, 1f, 1, 0, 1f);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(markerClass = UnstableApi.class)
    private void inicializarVideoHome() {
        if (playerViewHome == null) {
            Toast.makeText(this, "Error: playerViewHome es null en el XML", Toast.LENGTH_LONG).show();
            return;
        }

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true)
                .setConstantBitrateSeekingAlwaysEnabled(true);

        // Saltos de 10 segundos (10000 ms) para retroceder y adelantar el video de 8 minutos
        exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        playerViewHome.setPlayer(exoPlayer);

        // Permite arrastrar la barra de tiempo nativa sin que el ScrollView mueva la pantalla
        playerViewHome.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(Home.this, "Error video: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.leon;
        MediaItem mediaItem = MediaItem.fromUri(videoPath);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventoViewModel.cargarEventos();
        cargarAvatarHeader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
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
        reproducirSonidoClick(categoriaPulsada);

        if (categoriaSeleccionadaActual == categoriaPulsada) {
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
            cardFeaturedEvent1.setOnClickListener(v -> {
                reproducirSonidoClick(v);
                abrirDetalleEvento(e1);
            });
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
                cardFeaturedEvent2.setOnClickListener(v -> {
                    reproducirSonidoClick(v);
                    abrirDetalleEvento(e2);
                });
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