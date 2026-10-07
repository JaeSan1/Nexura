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
import com.example.nexura.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends AppCompatActivity {

    private TextView tvGamertag, tvUserLevel, tvEquippedTitle, tvXpProgress, tvCityLocation;
    private TextView tvFollowersCount, tvFollowingCount, tvLikesCount, tvProfileBio;
    private ProgressBar pbXpBar;

    private SupabaseApi api;
    private SessionManager sessionManager;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackPerfil);
        tvGamertag = findViewById(R.id.tvProfileGamertag);
        tvUserLevel = findViewById(R.id.tvProfileLevel);
        tvEquippedTitle = findViewById(R.id.tvEquippedTitle);
        tvXpProgress = findViewById(R.id.tvXpProgressText);
        tvCityLocation = findViewById(R.id.tvProfileLocation);
        tvProfileBio = findViewById(R.id.tvProfileBio);

        pbXpBar = findViewById(R.id.pbProfileXp);

        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvLikesCount = findViewById(R.id.tvReputationLikesCount);

        Button btnGoOrganizerPanel = findViewById(R.id.btnGoOrganizerPanel);
        Button btnEditCosmetics = findViewById(R.id.btnQuickEdit);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnGoOrganizerPanel != null) {
            btnGoOrganizerPanel.setOnClickListener(v -> startActivity(new Intent(Perfil.this, PanelOrganizador.class)));
        }

        if (btnEditCosmetics != null) {
            btnEditCosmetics.setOnClickListener(v -> {
                String[] opciones = {"Cambiar Título Equipado", "Editar Información", "Cerrar Sesión"};
                new AlertDialog.Builder(Perfil.this)
                        .setTitle("Ajustes de Perfil")
                        .setItems(opciones, (dialog, which) -> {
                            if (which == 0) abrirSelectorTitulos();
                            else if (which == 1) startActivity(new Intent(Perfil.this, EditarPerfil.class));
                            else if (which == 2) cerrarSesion();
                        })
                        .show();
            });
        }

        cargarDatosUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        String userId = sessionManager.getUserId();
        String gamertag = sessionManager.getGamertag();

        Call<List<Usuario>> call = (!userId.isEmpty())
                ? api.obtenerPerfilPorId("eq." + userId)
                : api.obtenerPerfilPorGamertag("eq." + gamertag);

        call.enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    usuarioActual = response.body().get(0);
                    actualizarUI(usuarioActual);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(Perfil.this, "Sin conexión con tu perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(Usuario user) {
        if (tvGamertag != null) tvGamertag.setText(user.getGamertag());
        if (tvUserLevel != null) tvUserLevel.setText("NIVEL " + user.getNivel());
        if (tvEquippedTitle != null) tvEquippedTitle.setText(user.getTituloEquipado());
        if (tvCityLocation != null) tvCityLocation.setText(user.getCiudad());
        if (tvProfileBio != null && user.getBiografia() != null) tvProfileBio.setText(user.getBiografia());

        if (tvFollowersCount != null) tvFollowersCount.setText(String.valueOf(user.getSeguidores()));
        if (tvFollowingCount != null) tvFollowingCount.setText(String.valueOf(user.getSeguidos()));
        if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(user.getReputacionLikes()));

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

        new AlertDialog.Builder(this)
                .setTitle("Equipar Título")
                .setItems(titulosDisponibles, (dialog, which) -> guardarTituloEnNube(titulosDisponibles[which]))
                .show();
    }

    private void guardarTituloEnNube(String nuevoTitulo) {
        String filtro = (!sessionManager.getUserId().isEmpty())
                ? "eq." + sessionManager.getUserId()
                : "eq." + sessionManager.getGamertag();

        Map<String, Object> body = new HashMap<>();
        body.put("titulo_equipado", nuevoTitulo);

        api.actualizarPerfil(filtro, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (tvEquippedTitle != null) tvEquippedTitle.setText(nuevoTitulo);
                    Toast.makeText(Perfil.this, "Título actualizado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}