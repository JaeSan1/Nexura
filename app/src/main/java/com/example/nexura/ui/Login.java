package com.example.nexura.ui;

import android.content.Intent;
import android.net.Uri;
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
import com.example.nexura.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLoginAction, btnLoginDiscord;
    private SupabaseApi api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // 1. AUTO-LOGIN: Redirigir a Home si la sesión ya está guardada
        if (sessionManager.isLogged()) {
            redireccionarAHome();
            return;
        }

        setContentView(R.layout.activity_login);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLoginAction = findViewById(R.id.btnLoginAction);
        btnLoginDiscord = findViewById(R.id.btnLoginDiscord);

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(Login.this, RecuperarPassword.class)));
        }

        if (tvGoRegister != null) {
            tvGoRegister.setOnClickListener(v ->
                    startActivity(new Intent(Login.this, Registro.class)));
        }

        if (btnLoginAction != null) {
            btnLoginAction.setOnClickListener(v -> autenticarUsuario());
        }

        // Evento Botón Discord
        if (btnLoginDiscord != null) {
            btnLoginDiscord.setOnClickListener(v -> iniciarOAuthDiscord());
        }

        // Procesar respuesta si el usuario viene de autenticarse en Discord
        procesarIntentOAuth(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        procesarIntentOAuth(intent);
    }

    private void autenticarUsuario() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmail != null) {
                etEmail.setError("Ingresa un correo electrónico válido");
                etEmail.requestFocus();
            }
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            if (etPassword != null) {
                etPassword.setError("Ingresa tu contraseña");
                etPassword.requestFocus();
            }
            return;
        }

        btnLoginAction.setEnabled(false);

        api.verificarCredenciales("eq." + email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                btnLoginAction.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario usuarioLogueado = response.body().get(0);

                    // Guardar usando SessionManager
                    sessionManager.guardarSesion(
                            usuarioLogueado.getId(),
                            usuarioLogueado.getGamertag(),
                            usuarioLogueado.getCorreo()
                    );

                    Toast.makeText(Login.this, "¡Bienvenido, " + usuarioLogueado.getGamertag() + "!", Toast.LENGTH_SHORT).show();
                    redireccionarAHome();
                } else {
                    Toast.makeText(Login.this, "Credenciales incorrectas o usuario no encontrado", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                btnLoginAction.setEnabled(true);
                Toast.makeText(Login.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarOAuthDiscord() {
        String supabaseUrl = "https://wfkzbqhsacitksuwqlei.supabase.co/auth/v1/authorize?provider=discord&redirect_to=nexura://login-callback";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(supabaseUrl));
        startActivity(intent);
    }

    private void procesarIntentOAuth(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("nexura://login-callback")) {
            String fragment = uri.getFragment();

            if (fragment != null && fragment.contains("access_token=")) {
                sessionManager.guardarSesion("discord_user_id", "DiscordUser", "discord@nexura.com");

                Toast.makeText(this, "¡Autenticado con éxito desde Discord!", Toast.LENGTH_SHORT).show();
                redireccionarAHome();
            }
        }
    }

    private void redireccionarAHome() {
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}