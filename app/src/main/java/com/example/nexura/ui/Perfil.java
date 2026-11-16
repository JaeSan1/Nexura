package com.example.nexura.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nexura.R;
import com.example.nexura.model.Logro;
import com.example.nexura.model.Notificacion;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends AppCompatActivity {

    private ImageView ivAvatar, ivBanner;
    private TextView tvGamertag, tvTitle, tvBio, tvCity;
    private TextView tvLevel, tvXpText, tvFollowers, tvFollowing, tvLikes, tvLogrosCount, tvEmptyLogros;
    private ProgressBar pbXp;

    private Button btnEditProfile, btnOrganizerPanel, btnCerrarSesion, btnEliminarCuenta;
    private Button btnSeguirVisitante, btnLikeVisitante;

    private RecyclerView rvLogros;
    private final List<Logro> listaLogros = new ArrayList<>();

    private SupabaseApi api;
    private SessionManager sessionManager;

    private String userIdMostrado;
    private boolean esMiPropioPerfil = true;
    private Usuario usuarioCargado;

    private boolean yaLoSigo = false;
    private boolean yaDiLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        String miId = sessionManager.getUserId();
        String idExterno = getIntent().getStringExtra("USER_ID_PERFIL");

        if (!TextUtils.isEmpty(idExterno) && !idExterno.equals(miId)) {
            esMiPropioPerfil = false;
            userIdMostrado = idExterno;
        } else {
            esMiPropioPerfil = true;
            userIdMostrado = miId;
        }

        ivAvatar = findViewById(R.id.ivProfileAvatar);
        ivBanner = findViewById(R.id.ivProfileBanner);
        tvGamertag = findViewById(R.id.tvProfileGamertag);
        tvTitle = findViewById(R.id.tvProfileTitle);
        tvBio = findViewById(R.id.tvProfileBio);
        tvCity = findViewById(R.id.tvProfileCity);
        tvLevel = findViewById(R.id.tvProfileLevel);
        tvXpText = findViewById(R.id.tvProfileXpText);
        tvFollowers = findViewById(R.id.tvProfileFollowersCount);
        tvFollowing = findViewById(R.id.tvProfileFollowingCount);
        tvLikes = findViewById(R.id.tvProfileLikesCount);
        tvLogrosCount = findViewById(R.id.tvLogrosCount);
        tvEmptyLogros = findViewById(R.id.tvEmptyLogros);
        pbXp = findViewById(R.id.pbProfileXp);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnOrganizerPanel = findViewById(R.id.btnOrganizerPanel);
        rvLogros = findViewById(R.id.rvLogrosPerfil);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        TextView btnBack = findViewById(R.id.btnBackProfile);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (rvLogros != null) {
            rvLogros.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        configurarModoPerfil();
    }

    private void configurarModoPerfil() {
        if (esMiPropioPerfil) {
            if (btnEditProfile != null) {
                btnEditProfile.setVisibility(View.VISIBLE);
                btnEditProfile.setOnClickListener(v -> startActivity(new Intent(Perfil.this, EditarPerfil.class)));
            }
            if (btnOrganizerPanel != null) {
                btnOrganizerPanel.setOnClickListener(v -> startActivity(new Intent(Perfil.this, PanelOrganizador.class)));
            }
            if (btnCerrarSesion != null) {
                btnCerrarSesion.setVisibility(View.VISIBLE);
                btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
            }
            if (btnEliminarCuenta != null) {
                btnEliminarCuenta.setVisibility(View.VISIBLE);
                btnEliminarCuenta.setOnClickListener(v -> confirmarEliminarCuenta());
            }
        } else {
            if (btnEditProfile != null) btnEditProfile.setVisibility(View.GONE);
            if (btnOrganizerPanel != null) btnOrganizerPanel.setVisibility(View.GONE);
            if (btnCerrarSesion != null) btnCerrarSesion.setVisibility(View.GONE);
            if (btnEliminarCuenta != null) btnEliminarCuenta.setVisibility(View.GONE);

            if (btnEditProfile != null && btnEditProfile.getParent() instanceof ViewGroup) {
                ViewGroup contenedor = (ViewGroup) btnEditProfile.getParent();
                int indice = contenedor.indexOfChild(btnEditProfile);

                LinearLayout filaAccionesVisitante = new LinearLayout(this);
                filaAccionesVisitante.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 16, 0, 16);
                filaAccionesVisitante.setLayoutParams(rowParams);

                // Botón Seguir / Siguiendo
                btnSeguirVisitante = crearBotonAccionVisitante("+ Seguir", "#00E5FF", "#1600E5FF");
                LinearLayout.LayoutParams pSeguir = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                pSeguir.setMargins(0, 0, 8, 0);
                btnSeguirVisitante.setLayoutParams(pSeguir);
                btnSeguirVisitante.setOnClickListener(v -> alternarSeguirUsuario());

                // Botón Like / Quitar Like
                btnLikeVisitante = crearBotonAccionVisitante("♥ Dar Like", "#FF4081", "#16FF4081");
                LinearLayout.LayoutParams pLike = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                pLike.setMargins(8, 0, 0, 0);
                btnLikeVisitante.setLayoutParams(pLike);
                btnLikeVisitante.setOnClickListener(v -> alternarLikePerfil());

                filaAccionesVisitante.addView(btnSeguirVisitante);
                filaAccionesVisitante.addView(btnLikeVisitante);
                contenedor.addView(filaAccionesVisitante, indice);

                verificarEstadosVisitante();
            }
        }
    }

    private Button crearBotonAccionVisitante(String texto, String colorHex, String fondoHex) {
        Button btn = new Button(this);
        btn.setText(texto);
        btn.setTextColor(Color.parseColor(colorHex));
        btn.setTextSize(13f);
        btn.setAllCaps(false);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setShape(GradientDrawable.RECTANGLE);
        fondo.setCornerRadius(24f);
        fondo.setColor(Color.parseColor(fondoHex));
        fondo.setStroke(2, Color.parseColor(colorHex));
        btn.setBackground(fondo);
        return btn;
    }

    private void actualizarEstiloBoton(Button btn, String texto, String colorHex, String fondoHex) {
        if (btn == null) return;
        btn.setText(texto);
        btn.setTextColor(Color.parseColor(colorHex));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setShape(GradientDrawable.RECTANGLE);
        fondo.setCornerRadius(24f);
        fondo.setColor(Color.parseColor(fondoHex));
        fondo.setStroke(2, Color.parseColor(colorHex));
        btn.setBackground(fondo);
    }

    private void verificarEstadosVisitante() {
        String miId = sessionManager.getUserId();
        if (TextUtils.isEmpty(miId) || TextUtils.isEmpty(userIdMostrado)) return;

        // 1. Verificar si ya lo sigo
        api.verificarSeguimiento("eq." + miId, "eq." + userIdMostrado).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    yaLoSigo = true;
                    actualizarEstiloBoton(btnSeguirVisitante, "✓ Siguiendo", "#00E5FF", "#3300E5FF");
                }
            }
            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {}
        });

        // 2. Verificar si ya le di Like
        api.verificarLikePerfil("eq." + miId, "eq." + userIdMostrado).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    yaDiLike = true;
                    actualizarEstiloBoton(btnLikeVisitante, "♥ Te gusta", "#FF4081", "#33FF4081");
                }
            }
            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {}
        });
    }

    private void alternarSeguirUsuario() {
        if (usuarioCargado == null) return;
        String miId = sessionManager.getUserId();
        if (TextUtils.isEmpty(miId)) return;

        if (btnSeguirVisitante != null) btnSeguirVisitante.setEnabled(false);

        if (!yaLoSigo) {
            Map<String, Object> body = new HashMap<>();
            body.put("seguidor_id", miId);
            body.put("seguido_id", userIdMostrado);

            api.seguirUsuario(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (btnSeguirVisitante != null) btnSeguirVisitante.setEnabled(true);
                    if (response.isSuccessful()) {
                        yaLoSigo = true;
                        int nuevosSeguidores = usuarioCargado.getSeguidores() + 1;
                        usuarioCargado.setSeguidores(nuevosSeguidores);
                        if (tvFollowers != null) tvFollowers.setText(String.valueOf(nuevosSeguidores));
                        actualizarEstiloBoton(btnSeguirVisitante, "✓ Siguiendo", "#00E5FF", "#3300E5FF");

                        actualizarContadorPerfil(userIdMostrado, "seguidores", nuevosSeguidores);
                        enviarNotificacionSocial(
                                userIdMostrado,
                                "Nuevo seguidor",
                                "@" + sessionManager.getGamertag() + " comenzó a seguirte.",
                                "seguidor"
                        );
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (btnSeguirVisitante != null) btnSeguirVisitante.setEnabled(true);
                }
            });
        } else {
            api.dejarDeSeguirUsuario("eq." + miId, "eq." + userIdMostrado).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (btnSeguirVisitante != null) btnSeguirVisitante.setEnabled(true);
                    if (response.isSuccessful()) {
                        yaLoSigo = false;
                        int nuevosSeguidores = Math.max(0, usuarioCargado.getSeguidores() - 1);
                        usuarioCargado.setSeguidores(nuevosSeguidores);
                        if (tvFollowers != null) tvFollowers.setText(String.valueOf(nuevosSeguidores));
                        actualizarEstiloBoton(btnSeguirVisitante, "+ Seguir", "#00E5FF", "#1600E5FF");

                        actualizarContadorPerfil(userIdMostrado, "seguidores", nuevosSeguidores);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (btnSeguirVisitante != null) btnSeguirVisitante.setEnabled(true);
                }
            });
        }
    }

    private void alternarLikePerfil() {
        if (usuarioCargado == null) return;
        String miId = sessionManager.getUserId();
        if (TextUtils.isEmpty(miId)) return;

        if (btnLikeVisitante != null) btnLikeVisitante.setEnabled(false);

        if (!yaDiLike) {
            Map<String, Object> bodyLike = new HashMap<>();
            bodyLike.put("usuario_id", miId);
            bodyLike.put("perfil_destinatario_id", userIdMostrado);

            api.darLikePerfil(bodyLike).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (btnLikeVisitante != null) btnLikeVisitante.setEnabled(true);
                    if (response.isSuccessful()) {
                        yaDiLike = true;
                        int nuevosLikes = usuarioCargado.getReputacionLikes() + 1;
                        usuarioCargado.setReputacionLikes(nuevosLikes);
                        if (tvLikes != null) tvLikes.setText(String.valueOf(nuevosLikes));
                        actualizarEstiloBoton(btnLikeVisitante, "♥ Te gusta", "#FF4081", "#33FF4081");

                        actualizarContadorPerfil(userIdMostrado, "reputacion_likes", nuevosLikes);
                        enviarNotificacionSocial(
                                userIdMostrado,
                                "¡Nuevo Like en tu perfil!",
                                "A @" + sessionManager.getGamertag() + " le gustó tu perfil.",
                                "like"
                        );
                        Toast.makeText(Perfil.this, "¡Diste Like a este perfil!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (btnLikeVisitante != null) btnLikeVisitante.setEnabled(true);
                }
            });
        } else {
            api.quitarLikePerfil("eq." + miId, "eq." + userIdMostrado).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (btnLikeVisitante != null) btnLikeVisitante.setEnabled(true);
                    if (response.isSuccessful()) {
                        yaDiLike = false;
                        int nuevosLikes = Math.max(0, usuarioCargado.getReputacionLikes() - 1);
                        usuarioCargado.setReputacionLikes(nuevosLikes);
                        if (tvLikes != null) tvLikes.setText(String.valueOf(nuevosLikes));
                        actualizarEstiloBoton(btnLikeVisitante, "♥ Dar Like", "#FF4081", "#16FF4081");

                        actualizarContadorPerfil(userIdMostrado, "reputacion_likes", nuevosLikes);
                        Toast.makeText(Perfil.this, "Like retirado", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (btnLikeVisitante != null) btnLikeVisitante.setEnabled(true);
                }
            });
        }
    }

    private void actualizarContadorPerfil(String perfilId, String columna, int nuevoValor) {
        Map<String, Object> body = new HashMap<>();
        body.put(columna, nuevoValor);
        api.actualizarPerfil("eq." + perfilId, body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void enviarNotificacionSocial(String destinoId, String titulo, String mensaje, String tipo) {
        Notificacion notif = new Notificacion(destinoId, titulo, mensaje, tipo);
        api.emitirAvisoOrganizador(notif).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPerfilDesdeSupabase();
        cargarLogrosUsuario();
    }

    private void cargarPerfilDesdeSupabase() {
        if (TextUtils.isEmpty(userIdMostrado)) return;

        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Modo sin conexión: mostrando datos guardados", Toast.LENGTH_SHORT).show();
            return;
        }

        api.obtenerPerfilPorId("eq." + userIdMostrado).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    usuarioCargado = response.body().get(0);
                    actualizarInterfaz(usuarioCargado);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {}
        });
    }

    private void cargarLogrosUsuario() {
        api.obtenerLogros().enqueue(new Callback<List<Logro>>() {
            @Override
            public void onResponse(Call<List<Logro>> call, Response<List<Logro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaLogros.clear();
                    listaLogros.addAll(response.body());

                    if (tvLogrosCount != null) {
                        tvLogrosCount.setText(listaLogros.size() + " Logros Disponibles");
                    }

                    if (tvEmptyLogros != null) {
                        tvEmptyLogros.setVisibility(listaLogros.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Logro>> call, Throwable t) {}
        });
    }

    private void actualizarInterfaz(Usuario user) {
        if (user == null) return;

        if (tvGamertag != null) tvGamertag.setText(user.getGamertag());
        if (tvTitle != null) tvTitle.setText(user.getTituloEquipado() != null ? user.getTituloEquipado() : "AVENTURERO NOVATO");
        if (tvBio != null) tvBio.setText(user.getBiografia() != null ? user.getBiografia() : "¡Nuevo jugador en Nexura!");
        if (tvCity != null) tvCity.setText(user.getCiudad() != null ? user.getCiudad() : "Chillán");

        if (tvLevel != null) tvLevel.setText("NIVEL " + user.getNivel());
        if (tvXpText != null) tvXpText.setText(user.getXpActual() + " / " + user.getXpMeta() + " XP");

        if (pbXp != null) {
            pbXp.setMax(user.getXpMeta() > 0 ? user.getXpMeta() : 500);
            pbXp.setProgress(user.getXpActual());
        }

        if (tvFollowers != null) tvFollowers.setText(String.valueOf(user.getSeguidores()));
        if (tvFollowing != null) tvFollowing.setText(String.valueOf(user.getSeguidos()));
        if (tvLikes != null) tvLikes.setText(String.valueOf(user.getReputacionLikes()));

        if (btnOrganizerPanel != null && esMiPropioPerfil) {
            btnOrganizerPanel.setVisibility(user.isEsOrganizador() ? View.VISIBLE : View.GONE);
        }

        if (ivAvatar != null && !TextUtils.isEmpty(user.getFotoPerfilUrl())) {
            Glide.with(this)
                    .load(user.getFotoPerfilUrl())
                    .placeholder(R.drawable.ic_logo_nexura)
                    .error(R.drawable.ic_logo_nexura)
                    .centerCrop()
                    .into(ivAvatar);
        }

        if (ivBanner != null && !TextUtils.isEmpty(user.getBannerUrl())) {
            Glide.with(this)
                    .load(user.getBannerUrl())
                    .placeholder(R.drawable.ic_logo_nexura)
                    .error(R.drawable.ic_logo_nexura)
                    .centerCrop()
                    .into(ivBanner);
        }
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Perderás todo tu progreso y eventos. Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> procesarEliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void procesarEliminarCuenta() {
        String userId = sessionManager.getUserId();

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Error: No se encontró el ID de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        api.eliminarUsuario("eq." + userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Perfil.this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                    cerrarSesion();
                } else {
                    Toast.makeText(Perfil.this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Perfil.this, "Fallo de conexión al eliminar cuenta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}