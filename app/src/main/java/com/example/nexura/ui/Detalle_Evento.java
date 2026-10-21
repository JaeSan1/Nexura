package com.example.nexura.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nexura.R;
import com.example.nexura.model.Comentario;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evento);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        eventoActual = (Evento) getIntent().getSerializableExtra("EVENTO_SELECCIONADO");

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
        Button btnClaimGps = findViewById(R.id.btnClaimGps);

        TextView tabInfo = findViewById(R.id.tabInfo);
        TextView tabAvisos = findViewById(R.id.tabAvisos);
        TextView tabComentarios = findViewById(R.id.tabComentarios);

        LinearLayout containerTabInfo = findViewById(R.id.containerTabInfo);
        LinearLayout containerTabAvisos = findViewById(R.id.containerTabAvisos);
        LinearLayout containerTabComentarios = findViewById(R.id.containerTabComentarios);

        EditText etNewComment = findViewById(R.id.etNewComment);
        Button btnSendComment = findViewById(R.id.btnSendComment);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Guardar o eliminar en Favoritos (Mis Eventos)
        if (btnSaveFavorite != null) {
            btnSaveFavorite.setOnClickListener(v -> {
                if (!NetworkUtils.hayInternet(this)) {
                    Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (eventoActual == null || eventoActual.getId() == null) return;

                isFavorite = !isFavorite;
                if (isFavorite) {
                    btnSaveFavorite.setText("Guardado");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));

                    Map<String, Object> body = new HashMap<>();
                    body.put("usuario_id", sessionManager.getUserId());
                    body.put("evento_id", eventoActual.getId());
                    body.put("asistio", false);

                    api.agregarAsistencia(body).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Toast.makeText(Detalle_Evento.this, "Guardado en Mis Eventos", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {}
                    });
                } else {
                    btnSaveFavorite.setText("Guardar");
                    btnSaveFavorite.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

                    api.eliminarAsistencia("eq." + sessionManager.getUserId(), "eq." + eventoActual.getId())
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                                    Toast.makeText(Detalle_Evento.this, "Eliminado de Mis Eventos", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });
                }
            });
        }

        tabInfo.setOnClickListener(v -> activarTab(tabInfo, containerTabInfo, tabAvisos, containerTabAvisos, tabComentarios, containerTabComentarios));
        tabAvisos.setOnClickListener(v -> activarTab(tabAvisos, containerTabAvisos, tabInfo, containerTabInfo, tabComentarios, containerTabComentarios));
        tabComentarios.setOnClickListener(v -> {
            activarTab(tabComentarios, containerTabComentarios, tabInfo, containerTabInfo, tabAvisos, containerTabAvisos);
            cargarComentarios();
        });

        // Enviar Comentario
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                if (!NetworkUtils.hayInternet(this)) {
                    Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                String texto = etNewComment.getText().toString().trim();
                if (texto.isEmpty()) {
                    etNewComment.setError("Escribe un mensaje");
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
                                etNewComment.setText("");
                                cargarComentarios();
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {}
                    });
                }
            });
        }

        // Reclamar asistencia por GPS
        if (btnClaimGps != null) {
            btnClaimGps.setOnClickListener(v -> verificarPermisosYObtenerUbicacion());
        }
    }

    private void verificarPermisosYObtenerUbicacion() {
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
            confirmarYOtorgarXp();
        } else {
            int distanciaInt = Math.round(distanciaMetros);
            new AlertDialog.Builder(this)
                    .setTitle("Fuera de Rango GPS")
                    .setMessage("Te encuentras a " + distanciaInt + " metros del evento. Debes estar a menos de " + (int) RADIO_PERMITIDO_METROS + "m para reclamar tu XP.")
                    .setPositiveButton("Entendido", null)
                    .show();
        }
    }

    private void confirmarYOtorgarXp() {
        int xpPremio = (eventoActual != null) ? eventoActual.getXpRecompensa() : 250;
        String userId = sessionManager.getUserId();
        String eventoId = (eventoActual != null) ? eventoActual.getId() : "";

        Map<String, Object> bodyAsistencia = new HashMap<>();
        bodyAsistencia.put("asistio", true);

        api.validarAsistenciaGps("eq." + userId, "eq." + eventoId, bodyAsistencia).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Map<String, Object> bodyXp = new HashMap<>();
                bodyXp.put("xp", xpPremio);

                api.actualizarXpUsuario("eq." + userId, bodyXp).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        yaReclamado = true;
                        Button btnClaimGps = findViewById(R.id.btnClaimGps);
                        if (btnClaimGps != null) {
                            btnClaimGps.setText("Asistencia Validada");
                            btnClaimGps.setEnabled(false);
                            btnClaimGps.setAlpha(0.5f);
                        }
                        Toast.makeText(Detalle_Evento.this, "¡Ubicación confirmada! +" + xpPremio + " XP acreditados.", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onFailure(Call<Void> call, Throwable t) {}
                });
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void cargarComentarios() {
        if (!NetworkUtils.hayInternet(this) || eventoActual == null || eventoActual.getId() == null) return;

        api.obtenerComentariosPorEvento("eq." + eventoActual.getId()).enqueue(new Callback<List<Comentario>>() {
            @Override public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {}
            @Override public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    private void activarTab(TextView activa, View contActivo, TextView t2, View c2, TextView t3, View c3) {
        activa.setBackgroundResource(R.drawable.categoria);
        activa.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
        t2.setBackground(null);
        t2.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        t3.setBackground(null);
        t3.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

        contActivo.setVisibility(View.VISIBLE);
        c2.setVisibility(View.GONE);
        c3.setVisibility(View.GONE);
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