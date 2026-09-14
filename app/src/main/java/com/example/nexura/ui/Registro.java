package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registro extends AppCompatActivity {

    private EditText etGamertag, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        etGamertag = findViewById(R.id.etRegGamertag);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegisterAction);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvGoLogin != null) {
            tvGoLogin.setOnClickListener(v -> finish());
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> validarYRegistrar());
        }
    }

    private void validarYRegistrar() {
        String gamertag = etGamertag != null ? etGamertag.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword != null ? etPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validaciones básicas de formulario
        if (TextUtils.isEmpty(gamertag)) {
            etGamertag.setError("Ingresa tu Gamertag");
            etGamertag.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo electrónico inválido");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!pass.equals(confirmPass)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        if (cbTerms != null && !cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        // 1. Verificar si el correo ya existe en Supabase
        api.verificarCredenciales("eq." + email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    btnRegister.setEnabled(true);
                    etEmail.setError("Este correo ya se encuentra registrado");
                    etEmail.requestFocus();
                } else {
                    // 2. Si no existe, procedemos a crear el perfil
                    guardarUsuarioEnNube(gamertag, email);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(Registro.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarUsuarioEnNube(String gamertag, String email) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setGamertag(gamertag);
        nuevoUsuario.setCorreo(email);
        nuevoUsuario.setCiudad("Chillán");
        nuevoUsuario.setBiografia("¡Nuevo jugador en Nexura!");
        nuevoUsuario.setTituloEquipado("AVENTURERO NOVATO");
        nuevoUsuario.setNivel(1);
        nuevoUsuario.setXpActual(0);
        nuevoUsuario.setXpMeta(500);
        nuevoUsuario.setSeguidores(0);
        nuevoUsuario.setSeguidos(0);
        nuevoUsuario.setReputacionLikes(0);
        nuevoUsuario.setEsOrganizador(false);

        api.registrarUsuario(nuevoUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(Registro.this, "¡Cuenta creada con éxito! Inicia sesión", Toast.LENGTH_LONG).show();
                    finish(); // Vuelve a Login.java
                } else {
                    Toast.makeText(Registro.this, "Error al registrar en la nube: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(Registro.this, "Falla al conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
