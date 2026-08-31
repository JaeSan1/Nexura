package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // 1. Botón Iniciar Sesión
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Ingresa tu correo electrónico");
                etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Ingresa tu contraseña");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("La contraseña debe tener al menos 6 caracteres");
                etPassword.requestFocus();
                return;
            }

            // Navegar al Home
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            finish(); // Cierra el Login
        });

        // Redirección a Recuperar Contraseña
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, RecuperarPassword.class);
            startActivity(intent);
        });

        // Redirección a Crear Cuenta
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Registro.class);
            startActivity(intent);
        });
    }
}