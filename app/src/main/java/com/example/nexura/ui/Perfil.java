package com.example.nexura.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.nexura.R;
import com.example.nexura.Repository.UsuarioRepository;
import com.example.nexura.Viewmodel.PerfilViewModel;
import com.example.nexura.model.Logro;
import com.example.nexura.model.Usuario;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class Perfil extends AppCompatActivity {

    private TextView tvGamertag, tvVerifiedBadge, tvUserLevel, tvEquippedTitle, tvXpProgress, tvCityLocation;
    private TextView tvFollowersCount, tvFollowingCount, tvLikesCount, tvProfileBio, tvAchievementsCount;
    private ImageView ivProfileAvatar, ivProfileBanner;
    private ProgressBar pbXpBar;
    private LinearLayout containerLogros;

    private PerfilViewModel viewModel;
    private SessionManager sessionManager;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);
        usuarioRepository = new UsuarioRepository();
        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        TextView btnBack = findViewById(R.id.btnBackPerfil);
        tvGamertag = findViewById(R.id.tvProfileGamertag);
        tvVerifiedBadge = findViewById(R.id.tvVerifiedBadge);
        tvUserLevel = findViewById(R.id.tvProfileLevel);
        tvEquippedTitle = findViewById(R.id.tvEquippedTitle);
        tvXpProgress = findViewById(R.id.tvXpProgressText);
        tvCityLocation = findViewById(R.id.tvProfileLocation);
        tvProfileBio = findViewById(R.id.tvProfileBio);

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        ivProfileBanner = findViewById(R.id.ivProfileBanner);

        pbXpBar = findViewById(R.id.pbProfileXp);

        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvLikesCount = findViewById(R.id.tvReputationLikesCount);

        tvAchievementsCount = findViewById(R.id.tvAchievementsCount);
        containerLogros = findViewById(R.id.containerLogros);

        Button btnEditCosmetics = findViewById(R.id.btnQuickEdit);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Desplegar Modal de Ajustes
        if (btnEditCosmetics != null) {
            btnEditCosmetics.setOnClickListener(v -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Perfil.this);
                View sheetView = getLayoutInflater().inflate(R.layout.modal_ajuste_perfil, null);
                bottomSheetDialog.setContentView(sheetView);

                TextView optionEdit = sheetView.findViewById(R.id.optionEditProfile);
                TextView optionLogout = sheetView.findViewById(R.id.optionLogout);
                TextView optionDelete = sheetView.findViewById(R.id.optionDeleteAccount);

                // NAVEGACIÓN A EDITAR PERFIL
                if (optionEdit != null) {
                    optionEdit.setOnClickListener(view -> {
                        bottomSheetDialog.dismiss();
                        Intent intent = new Intent(Perfil.this, EditarPerfil.class);
                        startActivity(intent);
                    });
                }

                if (optionLogout != null) {
                    optionLogout.setOnClickListener(view -> {
                        bottomSheetDialog.dismiss();
                        cerrarSesion();
                    });
                }

                if (optionDelete != null) {
                    optionDelete.setOnClickListener(view -> {
                        bottomSheetDialog.dismiss();
                        new AlertDialog.Builder(Perfil.this)
                                .setTitle("¿Eliminar tu cuenta?")
                                .setMessage("Esta acción es irreversible. Se borrarán permanentemente tus eventos, XP, nivel y datos de perfil.")
                                .setPositiveButton("Eliminar", (dialog, which) -> procesarEliminacionCuenta())
                                .setNegativeButton("Cancelar", null)
                                .show();
                    });
                }

                bottomSheetDialog.show();
            });
        }

        // Suscribirse al ViewModel
        viewModel.getUsuario().observe(this, this::actualizarUI);
        viewModel.getLogros().observe(this, this::mostrarLogrosEnUI);

        cargarDatos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos(); // Recargar datos al volver de EditarPerfil
    }

    private void cargarDatos() {
        if (NetworkUtils.hayInternet(this)) {
            viewModel.cargarPerfil(sessionManager.getUserId());
            viewModel.cargarLogros();
        } else {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void procesarEliminacionCuenta() {
        String userId = sessionManager.getUserId();
        usuarioRepository.eliminarCuenta(userId, new UsuarioRepository.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(Perfil.this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();
                cerrarSesion();
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(Perfil.this, "No se pudo eliminar: " + mensajeError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLogrosEnUI(List<Logro> listaLogros) {
        if (containerLogros == null || listaLogros == null) return;
        containerLogros.removeAllViews();

        if (tvAchievementsCount != null) {
            tvAchievementsCount.setText(listaLogros.size() + " Logros Disponibles");
        }

        for (Logro logro : listaLogros) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.parseColor("#1E293B"));
            card.setRadius(12f);
            card.setCardElevation(2f);

            LinearLayout layoutInner = new LinearLayout(this);
            layoutInner.setOrientation(LinearLayout.HORIZONTAL);
            layoutInner.setPadding(14, 12, 14, 12);

            TextView tvEmoji = new TextView(this);
            tvEmoji.setText(logro.getIconoEmoji() != null ? logro.getIconoEmoji() : "🏆");
            tvEmoji.setTextSize(22f);
            tvEmoji.setPadding(0, 0, 12, 0);

            LinearLayout layoutTextos = new LinearLayout(this);
            layoutTextos.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            layoutTextos.setLayoutParams(textParams);

            TextView tvNombre = new TextView(this);
            tvNombre.setText(logro.getNombre());
            tvNombre.setTextColor(Color.WHITE);
            tvNombre.setTextSize(13f);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(logro.getDescripcion());
            tvDesc.setTextColor(Color.parseColor("#94A3B8"));
            tvDesc.setTextSize(11f);

            layoutTextos.addView(tvNombre);
            layoutTextos.addView(tvDesc);

            TextView tvXp = new TextView(this);
            tvXp.setText("+" + logro.getXpBonus() + " XP");
            tvXp.setTextColor(Color.parseColor("#00F2FE"));
            tvXp.setTextSize(11f);

            layoutInner.addView(tvEmoji);
            layoutInner.addView(layoutTextos);
            layoutInner.addView(tvXp);

            card.addView(layoutInner);
            containerLogros.addView(card);
        }
    }

    private void actualizarUI(Usuario user) {
        if (user == null) return;

        if (tvGamertag != null) tvGamertag.setText(user.getGamertag());

        if (tvVerifiedBadge != null) {
            tvVerifiedBadge.setVisibility(user.isEsVerificado() ? View.VISIBLE : View.GONE);
        }

        if (tvUserLevel != null) tvUserLevel.setText("NIVEL " + user.getNivel());
        if (tvEquippedTitle != null) tvEquippedTitle.setText(user.getTituloEquipado() != null ? user.getTituloEquipado() : "AVENTURERO NOVATO");
        if (tvCityLocation != null) tvCityLocation.setText("📍 " + (user.getCiudad() != null ? user.getCiudad() : "Chillán, Chile"));
        if (tvProfileBio != null && user.getBiografia() != null) tvProfileBio.setText(user.getBiografia());

        if (tvFollowersCount != null) tvFollowersCount.setText(String.valueOf(user.getSeguidores()));
        if (tvFollowingCount != null) tvFollowingCount.setText(String.valueOf(user.getSeguidos()));
        if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(user.getReputacionLikes()));

        if (pbXpBar != null && tvXpProgress != null) {
            int meta = user.getXpMeta() > 0 ? user.getXpMeta() : 1000;
            pbXpBar.setMax(meta);
            pbXpBar.setProgress(user.getXpActual());
            tvXpProgress.setText(user.getXpActual() + " / " + meta + " XP");
        }
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}