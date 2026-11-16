package com.example.nexura.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nexura.R;
import com.example.nexura.model.Comentario;
import com.example.nexura.model.Evento;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.ui.adapter.ComentarioAdapter;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Detalle_Evento extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final float RADIO_PERMITIDO_METROS = 150.0f;

    private boolean yaReclamado = false;
    private boolean isFavorite = false;
    private Evento eventoActual;
    private SupabaseApi api;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationClient;

    private RecyclerView rvComentarios;
    private ComentarioAdapter comentarioAdapter;
    private final List<Comentario> listaComentarios = new ArrayList<>();

    private TextView btnSaveFavorite;
    private Button btnClaimGps;
    private EditText etNewComment;

    private final ActivityResultLauncher<Intent> editarEventoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    recargarEventoActualizado();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evento);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        eventoActual = (Evento) getIntent().getSerializableExtra("EVENTO_SELECCIONADO");

        TextView btnBack = findViewById(R.id.btnBack);
        btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
        btnClaimGps = findViewById(R.id.btnClaimGps);

        TextView tabInfo = findViewById(R.id.tabInfo);
        TextView tabAvisos = findViewById(R.id.tabAvisos);
        TextView tabComentarios = findViewById(R.id.tabComentarios);

        LinearLayout containerTabInfo = findViewById(R.id.containerTabInfo);
        LinearLayout containerTabAvisos = findViewById(R.id.containerTabAvisos);
        LinearLayout containerTabComentarios = findViewById(R.id.containerTabComentarios);

        etNewComment = findViewById(R.id.etNewComment);
        Button btnSendComment = findViewById(R.id.btnSendComment);
        rvComentarios = findViewById(R.id.rvComentarios);

        if (rvComentarios != null) {
            rvComentarios.setLayoutManager(new LinearLayoutManager(this));
            comentarioAdapter = new ComentarioAdapter(
                    listaComentarios,
                    sessionManager.getUserId(),
                    this::confirmarEliminarComentario
            );
            rvComentarios.setAdapter(comentarioAdapter);
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        poblarDatosEvento();
        configurarAccionesOrganizador();
        verificarEstadoGuardadoYAsistencia();

        if (btnSaveFavorite != null) {
            aplicarAnimacionRebote(btnSaveFavorite);
            btnSaveFavorite.setOnClickListener(v -> toggleFavorito());
        }

        if (tabInfo != null && tabAvisos != null && tabComentarios != null) {
            activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios);

            tabInfo.setOnClickListener(v -> activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios));
            tabAvisos.setOnClickListener(v -> activarTab(tabAvisos, containerTabAvisos, tabInfo, containerTabInfo, tabComentarios, containerTabComentarios));
            tabComentarios.setOnClickListener(v -> {
                activarTab(tabComentarios, containerTabComentarios, tabInfo, containerTabInfo, tabAvisos, containerTabAvisos);
                cargarComentarios();
            });
        }

        if (btnSendComment != null) {
            aplicarAnimacionRebote(btnSendComment);
            btnSendComment.setOnClickListener(v -> publicarNuevoComentario());
        }

        if (btnClaimGps != null) {
            aplicarAnimacionRebote(btnClaimGps);
            btnClaimGps.setOnClickListener(v -> verificarPermisosYObtenerUbicacion());
        }
    }

    private void poblarDatosEvento() {
        if (eventoActual == null) return;

        TextView tvTitle = findViewById(R.id.tvEventTitle);
        TextView tvDate = findViewById(R.id.tvEventDate);
        TextView tvLocation = findViewById(R.id.tvEventLocation);
        TextView tvDescription = findViewById(R.id.tvEventDescription);
        TextView tvXpBadge = findViewById(R.id.tvXpBadge);
        ImageView ivCover = findViewById(R.id.ivEventBanner);

        LinearLayout layoutOrganizador = findViewById(R.id.layoutOrganizadorDetalle);
        ImageView ivOrgAvatar = findViewById(R.id.ivDetailOrganizerAvatar);
        TextView tvOrgName = findViewById(R.id.tvDetailOrganizerName);

        if (tvTitle != null && eventoActual.getTitulo() != null) tvTitle.setText(eventoActual.getTitulo());
        if (tvDate != null && eventoActual.getFecha() != null) tvDate.setText(eventoActual.getFecha());
        if (tvLocation != null && eventoActual.getUbicacion() != null) tvLocation.setText(eventoActual.getUbicacion());
        if (tvDescription != null && eventoActual.getDescripcion() != null) tvDescription.setText(eventoActual.getDescripcion());
        if (tvXpBadge != null) tvXpBadge.setText("+" + eventoActual.getXpRecompensa() + " XP");

        if (tvOrgName != null) {
            String nombreOrg = !TextUtils.isEmpty(eventoActual.getOrganizadorNombre())
                    ? eventoActual.getOrganizadorNombre() : "Organizador Nexura";
            tvOrgName.setText(nombreOrg);
        }

        if (ivOrgAvatar != null) {
            String avatarOrg = eventoActual.getOrganizadorAvatarUrl();
            if (!TextUtils.isEmpty(avatarOrg)) {
                Glide.with(this)
                        .load(avatarOrg)
                        .placeholder(R.drawable.ic_logo_nexura)
                        .error(R.drawable.ic_logo_nexura)
                        .circleCrop()
                        .into(ivOrgAvatar);
            } else {
                ivOrgAvatar.setImageResource(R.drawable.ic_logo_nexura);
            }
        }

        if (layoutOrganizador != null) {
            aplicarAnimacionRebote(layoutOrganizador);
            layoutOrganizador.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(eventoActual.getOrganizadorId())) {
                    Intent intent = new Intent(Detalle_Evento.this, Perfil.class);
                    intent.putExtra("USER_ID_PERFIL", eventoActual.getOrganizadorId());
                    startActivity(intent);
                }
            });
        }

        if (ivCover != null && !TextUtils.isEmpty(eventoActual.getPortadaUrl())) {
            Glide.with(this)
                    .load(eventoActual.getPortadaUrl())
                    .placeholder(R.drawable.ic_logo_nexura)
                    .error(R.drawable.ic_logo_nexura)
                    .centerCrop()
                    .into(ivCover);
        }
    }

    private void configurarAccionesOrganizador() {
        LinearLayout layoutAcciones = findViewById(R.id.layoutAccionesOrganizador);
        Button btnEditar = findViewById(R.id.btnEditarEvento);
        Button btnEliminar = findViewById(R.id.btnEliminarEvento);

        if (eventoActual == null || layoutAcciones == null) return;

        String miId = sessionManager.getUserId();
        boolean soyOrganizador = !TextUtils.isEmpty(miId) && miId.equals(eventoActual.getOrganizadorId());

        if (soyOrganizador) {
            layoutAcciones.setAlpha(0f);
            layoutAcciones.setTranslationY(24f);
            layoutAcciones.setVisibility(View.VISIBLE);
            layoutAcciones.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();

            if (btnEditar != null) {
                btnEditar.setBackground(crearFondoNeonConRipple("#1600E5FF", "#00E5FF", "#5500E5FF"));
                aplicarAnimacionRebote(btnEditar);
                btnEditar.setOnClickListener(v -> {
                    Intent intent = new Intent(Detalle_Evento.this, CrearEvento.class);
                    intent.putExtra("EVENTO_A_EDITAR", eventoActual);
                    editarEventoLauncher.launch(intent);
                });
            }

            if (btnEliminar != null) {
                btnEliminar.setBackground(crearFondoNeonConRipple("#16FF5252", "#FF5252", "#55FF5252"));
                aplicarAnimacionRebote(btnEliminar);
                btnEliminar.setOnClickListener(v -> confirmarEliminacionEvento(miId, btnEliminar));
            }
        } else {
            layoutAcciones.setVisibility(View.GONE);
        }
    }

    private Drawable crearFondoNeonConRipple(String colorFondoHex, String colorBordeHex, String colorOndaHex) {
        GradientDrawable baseShape = new GradientDrawable();
        baseShape.setShape(GradientDrawable.RECTANGLE);
        baseShape.setCornerRadius(24f);
        baseShape.setColor(Color.parseColor(colorFondoHex));
        baseShape.setStroke(2, Color.parseColor(colorBordeHex));

        GradientDrawable maskShape = new GradientDrawable();
        maskShape.setShape(GradientDrawable.RECTANGLE);
        maskShape.setCornerRadius(24f);
        maskShape.setColor(Color.WHITE);

        ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor(colorOndaHex));
        return new RippleDrawable(rippleColor, baseShape, maskShape);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void aplicarAnimacionRebote(View boton) {
        boton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .setDuration(100)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(220)
                            .setInterpolator(new OvershootInterpolator(2.0f))
                            .start();
                    break;
            }
            return false;
        });
    }

    private void confirmarEliminacionEvento(String miId, Button btnEliminar) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("¿Seguro que deseas eliminar \"" + eventoActual.getTitulo() + "\"? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminacionEvento(miId, btnEliminar))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacionEvento(String miId, Button btnEliminar) {
        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnEliminar != null) btnEliminar.setEnabled(false);

        api.eliminarEvento("eq." + eventoActual.getId(), "eq." + miId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (btnEliminar != null) btnEliminar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(Detalle_Evento.this, "Evento eliminado correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e("SUPABASE_DELETE", "Error eliminando evento: " + response.code());
                    Toast.makeText(Detalle_Evento.this, "No se pudo eliminar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (btnEliminar != null) btnEliminar.setEnabled(true);
                Toast.makeText(Detalle_Evento.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recargarEventoActualizado() {
        if (eventoActual == null || eventoActual.getId() == null) {
            finish();
            return;
        }

        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Evento ev : response.body()) {
                        if (eventoActual.getId().equals(ev.getId())) {
                            eventoActual = ev;
                            poblarDatosEvento();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {}
        });
    }

    private void verificarEstadoGuardadoYAsistencia() {
        if (eventoActual == null || eventoActual.getId() == null) return;
        String userId = sessionManager.getUserId();
        if (TextUtils.isEmpty(userId)) return;

        api.obtenerAsistenciasUsuario("eq." + userId).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Object obj : response.body()) {
                        if (obj instanceof Map) {
                            Map<?, ?> reg = (Map<?, ?>) obj;
                            Object evIdObj = reg.get("evento_id");
                            if (evIdObj != null && evIdObj.toString().equals(eventoActual.getId())) {

                                Object favObj = reg.get("es_favorito");
                                if (favObj != null && Boolean.parseBoolean(favObj.toString())) {
                                    isFavorite = true;
                                    if (btnSaveFavorite != null) {
                                        btnSaveFavorite.setText("Guardado");
                                        btnSaveFavorite.setTextColor(ContextCompat.getColor(Detalle_Evento.this, R.color.neon_cyan));
                                    }
                                }

                                Object gpsObj = reg.get("validado_gps");
                                if (gpsObj != null && Boolean.parseBoolean(gpsObj.toString())) {
                                    yaReclamado = true;
                                    if (btnClaimGps != null) {
                                        btnClaimGps.setText("Asistencia Validada");
                                        btnClaimGps.setEnabled(false);
                                        btnClaimGps.setAlpha(0.5f);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {}
        });
    }

    private void toggleFavorito() {
        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventoActual == null || eventoActual.getId() == null) return;

        isFavorite = !isFavorite;
        if (btnSaveFavorite != null) {
            btnSaveFavorite.setText(isFavorite ? "Guardado" : "Guardar");
            btnSaveFavorite.setTextColor(ContextCompat.getColor(this, isFavorite ? R.color.neon_cyan : R.color.text_gray));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("usuario_id", sessionManager.getUserId());
        body.put("evento_id", eventoActual.getId());
        body.put("es_favorito", isFavorite);

        api.agregarAsistencia(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                actualizarContadorLikesEvento(isFavorite);
                Toast.makeText(Detalle_Evento.this, isFavorite ? "Guardado en Mis Eventos" : "Eliminado de Mis Eventos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void actualizarContadorLikesEvento(boolean incremento) {
        if (eventoActual == null || eventoActual.getId() == null) return;
        int likesActuales = eventoActual.getContadorLikes();
        int nuevosLikes = incremento ? likesActuales + 1 : Math.max(0, likesActuales - 1);
        eventoActual.setContadorLikes(nuevosLikes);

        Map<String, Object> body = new HashMap<>();
        body.put("contador_likes", nuevosLikes);

        api.actualizarEvento("eq." + eventoActual.getId(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void publicarNuevoComentario() {
        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String texto = etNewComment != null ? etNewComment.getText().toString().trim() : "";
        if (TextUtils.isEmpty(texto)) {
            if (etNewComment != null) etNewComment.setError("Escribe un mensaje");
            return;
        }

        if (eventoActual != null && eventoActual.getId() != null) {
            Comentario nuevo = new Comentario(
                    eventoActual.getId(),
                    sessionManager.getUserId(),
                    sessionManager.getGamertag(),
                    texto
            );

            api.publicarComentario(nuevo).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(Detalle_Evento.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                        if (etNewComment != null) etNewComment.setText("");
                        cargarComentarios();
                    } else {
                        Toast.makeText(Detalle_Evento.this, "Error al publicar comentario: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(Detalle_Evento.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmarEliminarComentario(Comentario comentario, int position) {
        if (comentario == null || TextUtils.isEmpty(comentario.getId())) {
            Toast.makeText(this, "Recarga los comentarios antes de eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar comentario")
                .setMessage("¿Deseas eliminar tu comentario?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String miId = sessionManager.getUserId();
                    api.eliminarComentario("eq." + comentario.getId(), "eq." + miId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                if (position >= 0 && position < listaComentarios.size()) {
                                    listaComentarios.remove(position);
                                    comentarioAdapter.notifyItemRemoved(position);
                                } else {
                                    cargarComentarios();
                                }
                                Toast.makeText(Detalle_Evento.this, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Detalle_Evento.this, "No se pudo eliminar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(Detalle_Evento.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verificarPermisosYObtenerUbicacion() {
        if (yaReclamado) return;

        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            obtenerUbicacionYValidarDistancia();
        }
    }

    private void obtenerUbicacionYValidarDistancia() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        Toast.makeText(this, "Obteniendo señal GPS...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                validarCercania(location);
            } else {
                Toast.makeText(Detalle_Evento.this, "Activa el GPS de tu dispositivo para validar la ubicación", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validarCercania(Location ubicacionUsuario) {
        if (eventoActual == null || eventoActual.getLatitud() == 0) {
            Toast.makeText(this, "El evento no posee coordenadas GPS configuradas.", Toast.LENGTH_SHORT).show();
            return;
        }

        float[] resultados = new float[1];
        Location.distanceBetween(
                ubicacionUsuario.getLatitude(),
                ubicacionUsuario.getLongitude(),
                eventoActual.getLatitud(),
                eventoActual.getLongitud(),
                resultados
        );

        float distanciaMetros = resultados[0];

        if (distanciaMetros <= RADIO_PERMITIDO_METROS) {
            iniciarTransaccionXP();
        } else {
            int distanciaInt = Math.round(distanciaMetros);
            new AlertDialog.Builder(this)
                    .setTitle("Fuera de Rango GPS")
                    .setMessage("Te encuentras a " + distanciaInt + " metros del evento. Debes estar a menos de " + (int) RADIO_PERMITIDO_METROS + "m para reclamar tu XP.")
                    .setPositiveButton("Entendido", null)
                    .show();
        }
    }

    private void iniciarTransaccionXP() {
        if (btnClaimGps != null) btnClaimGps.setEnabled(false);
        String userId = sessionManager.getUserId();

        api.obtenerPerfilPorId("eq." + userId).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    calcularLevelUp(response.body().get(0));
                } else {
                    if (btnClaimGps != null) btnClaimGps.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                if (btnClaimGps != null) btnClaimGps.setEnabled(true);
            }
        });
    }

    private void calcularLevelUp(Usuario user) {
        int xpGanada = (eventoActual != null) ? eventoActual.getXpRecompensa() : 250;
        int nuevaXp = user.getXpActual() + xpGanada;
        int nuevoNivel = user.getNivel();
        int nuevaMeta = user.getXpMeta();
        boolean subioNivel = false;

        if (nuevaXp >= nuevaMeta) {
            nuevaXp -= nuevaMeta;
            nuevoNivel++;
            nuevaMeta = (int) (nuevaMeta * 1.5);
            subioNivel = true;
        }

        Map<String, Object> perfilBody = new HashMap<>();
        perfilBody.put("xp_actual", nuevaXp);
        perfilBody.put("nivel", nuevoNivel);
        perfilBody.put("xp_meta", nuevaMeta);

        boolean esNivelUp = subioNivel;
        int nivelFinal = nuevoNivel;

        api.actualizarPerfil("eq." + user.getId(), perfilBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    confirmarAsistenciaGPS(xpGanada, esNivelUp, nivelFinal);
                } else {
                    if (btnClaimGps != null) btnClaimGps.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (btnClaimGps != null) btnClaimGps.setEnabled(true);
            }
        });
    }

    private void confirmarAsistenciaGPS(int xpGanada, boolean subioNivel, int nuevoNivel) {
        Map<String, Object> asistenciaBody = new HashMap<>();
        asistenciaBody.put("usuario_id", sessionManager.getUserId());
        asistenciaBody.put("evento_id", eventoActual.getId());
        asistenciaBody.put("validado_gps", true);
        asistenciaBody.put("xp_otorgada", xpGanada);

        api.agregarAsistencia(asistenciaBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                yaReclamado = true;
                if (btnClaimGps != null) {
                    btnClaimGps.setText("Asistencia Validada");
                    btnClaimGps.setEnabled(false);
                    btnClaimGps.setAlpha(0.5f);
                }

                incrementarContadorAsistentesEvento();

                if (subioNivel) {
                    new AlertDialog.Builder(Detalle_Evento.this)
                            .setTitle("¡NIVEL " + nuevoNivel + " ALCANZADO!")
                            .setMessage("¡Felicidades! Completaste tu barra de experiencia.")
                            .setPositiveButton("¡Genial!", null)
                            .show();
                } else {
                    Toast.makeText(Detalle_Evento.this, "¡+" + xpGanada + " XP reclamados con éxito!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (btnClaimGps != null) btnClaimGps.setEnabled(true);
            }
        });
    }

    private void incrementarContadorAsistentesEvento() {
        if (eventoActual == null || eventoActual.getId() == null) return;
        int asistentesActuales = eventoActual.getContadorAsistentes();
        int nuevosAsistentes = asistentesActuales + 1;
        eventoActual.setContadorAsistentes(nuevosAsistentes);

        Map<String, Object> body = new HashMap<>();
        body.put("contador_asistentes", nuevosAsistentes);

        api.actualizarEvento("eq." + eventoActual.getId(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void cargarComentarios() {
        if (!NetworkUtils.hayInternet(this) || eventoActual == null || eventoActual.getId() == null) return;

        api.obtenerComentariosPorEvento("eq." + eventoActual.getId()).enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaComentarios.clear();
                    listaComentarios.addAll(response.body());
                    if (comentarioAdapter != null) {
                        comentarioAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void activarTab(TextView activa, View contActivo, TextView t2, View c2, TextView t3, View c3) {
        if (activa == null || contActivo == null) return;

        activa.setBackgroundResource(R.drawable.categoria);
        activa.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
        if (t2 != null) {
            t2.setBackground(null);
            t2.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        }
        if (t3 != null) {
            t3.setBackground(null);
            t3.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        }

        contActivo.setVisibility(View.VISIBLE);
        if (c2 != null) c2.setVisibility(View.GONE);
        if (c3 != null) c3.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYValidarDistancia();
            } else {
                Toast.makeText(this, "Permiso de GPS denegado. No se puede validar tu asistencia.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}