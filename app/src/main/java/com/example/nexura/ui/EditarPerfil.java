package com.example.nexura.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

public class EditarPerfil extends AppCompatActivity {

    private EditText etGamertag, etCity, etBio;
    private Button btnSaveProfile;
    private SupabaseApi api;
    private final String gamertagActual = "TheGoat99"; // Usuario activo en sesión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackEditProfile);
        etGamertag = findViewById(R.id.etEditGamertag);
        etCity = findViewById(R.id.etEditCity);
        etBio = findViewById(R.id.etEditBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Precargar los datos actuales desde la nube
        cargarDatosActuales();

        // 2. Guardar cambios en Supabase
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> guardarCambios());
        }
    }

    private void cargarDatosActuales() {
        api.obtenerPerfilPorGamertag("eq." + gamertagActual).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario user = response.body().get(0);
                    if (etGamertag != null) etGamertag.setText(user.getGamertag());
                    if (etCity != null) etCity.setText(user.getCiudad());
                    if (etBio != null) etBio.setText(user.getBiografia());
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(EditarPerfil.this, "No se pudo cargar la info actual", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios() {
        String nuevoGamertag = etGamertag != null ? etGamertag.getText().toString().trim() : "";
        String nuevaCiudad = etCity != null ? etCity.getText().toString().trim() : "";
        String nuevaBio = etBio != null ? etBio.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nuevoGamertag)) {
            if (etGamertag != null) etGamertag.setError("El Gamertag es obligatorio");
            return;
        }

        if (btnSaveProfile != null) btnSaveProfile.setEnabled(false);

        Map<String, Object> campos = new HashMap<>();
        campos.put("gamertag", nuevoGamertag);
        campos.put("ciudad", nuevaCiudad);
        campos.put("biografia", nuevaBio);

        api.actualizarPerfil("eq." + gamertagActual, campos).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(EditarPerfil.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Vuelve a Perfil.java, donde onResume() refrescará la vista
                } else {
                    Toast.makeText(EditarPerfil.this, "Error al actualizar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                Toast.makeText(EditarPerfil.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
