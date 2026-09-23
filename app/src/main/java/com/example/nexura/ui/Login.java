package com.example.nexura.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLoginAction;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLoginAction = findViewById(R.id.btnLoginAction);

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        // Navegación a Recuperar Contraseña
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(Login.this, RecuperarPassword.class)));
        }

        // Navegación a Registro
        if (tvGoRegister != null) {
            tvGoRegister.setOnClickListener(v ->
                    startActivity(new Intent(Login.this, Registro.class)));
        }

        // Acción de Iniciar Sesión contra Supabase
        if (btnLoginAction != null) {
            btnLoginAction.setOnClickListener(v -> autenticarUsuario());
        }
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

        // Consultar el perfil en la nube por correo
        api.verificarCredenciales("eq." + email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                btnLoginAction.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario usuarioLogueado = response.body().get(0);

                    // Guardar sesión en SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("NexuraSession", MODE_PRIVATE);
                    prefs.edit()
                            .putString("USER_GAMERTAG", usuarioLogueado.getGamertag())
                            .putString("USER_ID", usuarioLogueado.getId())
                            .putString("USER_EMAIL", usuarioLogueado.getCorreo())
                            .putBoolean("IS_LOGGED", true)
                            .apply();

                    Toast.makeText(Login.this, "¡Bienvenido de vuelta, " + usuarioLogueado.getGamertag() + "!", Toast.LENGTH_SHORT).show();

                    // Ir a Home y limpiar historial de navegación
                    Intent intent = new Intent(Login.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Credenciales incorrectas o usuario no encontrado", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                btnLoginAction.setEnabled(true);
                Toast.makeText(Login.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}