package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;

public class Registro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        TextView btnBackRegister = findViewById(R.id.btnBackRegister);
        TextView tvBackToLogin = findViewById(R.id.tvGoToLogin);
        EditText etRegisterUsername = findViewById(R.id.etRegisterUsername);
        EditText etRegisterEmail = findViewById(R.id.etRegisterEmail);
        EditText etRegisterPassword = findViewById(R.id.etRegisterPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnDoRegister = findViewById(R.id.btnDoRegister);

        // Volver atrás
        if (btnBackRegister != null) btnBackRegister.setOnClickListener(v -> finish());
        if (tvBackToLogin != null) tvBackToLogin.setOnClickListener(v -> finish());

        // Lógica de registro
        btnDoRegister.setOnClickListener(v -> {
            String username = etRegisterUsername.getText().toString().trim();
            String email = etRegisterEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etRegisterUsername.setError("Ingresa tu Gamertag");
                etRegisterUsername.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etRegisterEmail.setError("Ingresa tu correo");
                etRegisterEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etRegisterPassword.setError("Mínimo 6 caracteres");
                etRegisterPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPass)) {
                etConfirmPassword.setError("Las contraseñas no coinciden");
                etConfirmPassword.requestFocus();
                return;
            }

            Toast.makeText(Registro.this, "¡Cuenta creada con éxito! Bienvenido " + username, Toast.LENGTH_SHORT).show();

            // Redirigir directo al Home
            Intent intent = new Intent(Registro.this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
