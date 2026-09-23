package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends AppCompatActivity {

    // Vistas Perfil
    private TextView tvGamertag, tvUserLevel, tvEquippedTitle, tvXpProgress, tvCityLocation;
    private TextView tvFollowersCount, tvFollowingCount, tvLikesCount;
    private ProgressBar pbXpBar;

    private SupabaseApi api;
    private Usuario usuarioActual;
    private final String gamertagSesion = "TheGoat99"; // Usuario activo por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);


        TextView btnBack = findViewById(R.id.btnBackPerfil);
        tvGamertag = findViewById(R.id.tvProfileGamertag);
        tvUserLevel = findViewById(R.id.tvProfileLevel);
        tvEquippedTitle = findViewById(R.id.tvEquippedTitle);
        tvXpProgress = findViewById(R.id.tvXpProgressText);
        tvCityLocation = findViewById(R.id.tvProfileLocation);
        pbXpBar = findViewById(R.id.pbProfileXp);

        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvLikesCount = findViewById(R.id.tvReputationLikesCount);

        Button btnGoOrganizerPanel = findViewById(R.id.btnGoOrganizerPanel);
        Button btnEditCosmetics = findViewById(R.id.btnEditCosmetics);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Cargar datos reales desde Supabase
        cargarDatosUsuario();

        // Acceso al Panel de Organizador
        if (btnGoOrganizerPanel != null) {
            btnGoOrganizerPanel.setOnClickListener(v -> {
                startActivity(new Intent(Perfil.this, PanelOrganizador.class));
            });
        }

        // Menú de Ajustes
        if (btnEditCosmetics != null) {
            btnEditCosmetics.setOnClickListener(v -> {
                String[] opciones = {
                        "🏷️ Cambiar Título Equipado",
                        "✏️ Editar Información",
                        "🚪 Cerrar Sesión"
                };

                new AlertDialog.Builder(Perfil.this)
                        .setTitle("Ajustes de Perfil")
                        .setItems(opciones, (dialog, which) -> {
                            if (which == 0) {
                                abrirSelectorTitulos();
                            } else if (which == 1) {
                                startActivity(new Intent(Perfil.this, EditarPerfil.class));
                            } else if (which == 2) {
                                cerrarSesion();
                            }
                        })
                        .show();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos si el usuario editó perfil
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        api.obtenerPerfilPorGamertag("eq." + gamertagSesion).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    usuarioActual = response.body().get(0);
                    actualizarUI(usuarioActual);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(Perfil.this, "Error al sincronizar perfil: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(Usuario user) {
        if (tvGamertag != null) tvGamertag.setText(user.getGamertag());
        if (tvUserLevel != null) tvUserLevel.setText("NIVEL " + user.getNivel());
        if (tvEquippedTitle != null) tvEquippedTitle.setText(" " + user.getTituloEquipado());
        if (tvCityLocation != null) tvCityLocation.setText(user.getCiudad());

        if (tvFollowersCount != null) tvFollowersCount.setText(String.valueOf(user.getSeguidores()));
        if (tvFollowingCount != null) tvFollowingCount.setText(String.valueOf(user.getSeguidos()));
        if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(user.getReputacionLikes()));

        // Sincronizar barra de progreso XP
        if (pbXpBar != null && tvXpProgress != null) {
            pbXpBar.setMax(user.getXpMeta());
            pbXpBar.setProgress(user.getXpActual());
            tvXpProgress.setText(user.getXpActual() + " / " + user.getXpMeta() + " XP");
        }
    }

    private void abrirSelectorTitulos() {
        String[] titulosDisponibles = {
                "AVENTURERO NOVATO",
                "FRIKI",
                "FIESTERO NOCTURNO",
                "CONQUISTADOR DE FESTIVALES"
        };

        new AlertDialog.Builder(Perfil.this)
                .setTitle("Equipar Título")
                .setItems(titulosDisponibles, (dialog, which) -> {
                    String tituloSeleccionado = titulosDisponibles[which];
                    guardarTituloEnNube(tituloSeleccionado);
                })
                .show();
    }

    private void guardarTituloEnNube(String nuevoTitulo) {
        Map<String, Object> body = new HashMap<>();
        body.put("titulo_equipado", nuevoTitulo);

        api.actualizarPerfil("eq." + gamertagSesion, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (tvEquippedTitle != null) tvEquippedTitle.setText(" " + nuevoTitulo);
                    Toast.makeText(Perfil.this, "Título actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Perfil.this, "Error al guardar título", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Perfil.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarSesion() {
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}