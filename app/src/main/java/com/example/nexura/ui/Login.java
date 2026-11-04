package com.example.nexura.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.nexura.R;
import com.example.nexura.Repository.UsuarioRepository;
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
    private Button btnLoginAction;
    private ImageButton btnLoginDiscord, btnSocialX, btnSocialGoogle;
    private SupabaseApi api;
    private SessionManager sessionManager;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Inicializar Splash Screen antes de super.onCreate
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        usuarioRepository = new UsuarioRepository();

        // Auto-login: Redirigir a Home si la sesión ya está activa
        if (sessionManager.isLogged()) {
            redireccionarAHome();
            return;
        }

        setContentView(R.layout.activity_login);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        // Vincular componentes de la UI
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLoginAction = findViewById(R.id.btnLoginAction);

        btnLoginDiscord = findViewById(R.id.btnLoginDiscord);
        btnSocialX = findViewById(R.id.btnSocialX);
        btnSocialGoogle = findViewById(R.id.btnSocialGoogle);

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

        if (btnLoginDiscord != null) {
            btnLoginDiscord.setOnClickListener(v -> iniciarOAuth("discord"));
        }

        if (btnSocialX != null) { // Utilizado para GitHub
            btnSocialX.setOnClickListener(v -> iniciarOAuth("github"));
        }

        if (btnSocialGoogle != null) {
            btnSocialGoogle.setOnClickListener(v -> iniciarOAuth("google"));
        }

        // Comprobar si la Activity se abrió mediante la redirección OAuth
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

    private void iniciarOAuth(String provider) {
        String supabaseUrl = "https://wfkzbqhsacitksuwqlei.supabase.co/auth/v1/authorize"
                + "?provider=" + provider
                + "&prompt=consent"
                + "&redirect_to=nexura://login-callback";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(supabaseUrl));
        startActivity(intent);
    }

    private void procesarIntentOAuth(Intent intent) {
        if (intent == null) return;

        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("nexura://login-callback")) {
            // Se limpia el data para que no vuelva a procesarse el Intent si se recrea la Activity
            intent.setData(null);

            String fragment = uri.getFragment();
            String query = uri.getQuery();

            boolean tieneToken = (fragment != null && fragment.contains("access_token="))
                    || (query != null && query.contains("access_token="));

            if (tieneToken) {
                String emailReal = extraerEmailDeUri(uri);

                if (TextUtils.isEmpty(emailReal)) {
                    emailReal = "social_" + System.currentTimeMillis() + "@nexura.com";
                }

                verificarOCompletarPerfil(emailReal);
            }
        }
    }

    private String extraerEmailDeUri(Uri uri) {
        String fragment = uri.getFragment();
        if (fragment != null && fragment.contains("email=")) {
            for (String param : fragment.split("&")) {
                if (param.startsWith("email=")) {
                    return Uri.decode(param.substring(6));
                }
            }
        }

        String queryEmail = uri.getQueryParameter("email");
        if (!TextUtils.isEmpty(queryEmail)) {
            return queryEmail;
        }

        return null;
    }

    private void verificarOCompletarPerfil(String emailExtraido) {
        if (btnLoginAction != null) btnLoginAction.setEnabled(false);

        api.verificarCredenciales("eq." + emailExtraido).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (btnLoginAction != null) btnLoginAction.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario usuarioExistente = response.body().get(0);
                    sessionManager.guardarSesion(
                            usuarioExistente.getId(),
                            usuarioExistente.getGamertag(),
                            usuarioExistente.getCorreo()
                    );
                    Toast.makeText(Login.this, "¡Bienvenido de nuevo, " + usuarioExistente.getGamertag() + "!", Toast.LENGTH_SHORT).show();
                    redireccionarAHome();
                } else {
                    Toast.makeText(Login.this, "Completa tu registro para continuar", Toast.LENGTH_LONG).show();

                    Intent intentRegistro = new Intent(Login.this, Registro.class);
                    intentRegistro.putExtra("email_social", emailExtraido);
                    startActivity(intentRegistro);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                if (btnLoginAction != null) btnLoginAction.setEnabled(true);
                Toast.makeText(Login.this, "Error de conexión al verificar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redireccionarAHome() {
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}