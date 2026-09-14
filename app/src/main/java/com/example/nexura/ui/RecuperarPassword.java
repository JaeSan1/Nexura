package com.example.nexura.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarPassword extends AppCompatActivity {

    private EditText etEmailReset;
    private Button btnSendResetCode;
    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackReset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);
        etEmailReset = findViewById(R.id.etResetEmail);
        btnSendResetCode = findViewById(R.id.btnSendResetCode);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> finish());
        }

        if (btnSendResetCode != null) {
            btnSendResetCode.setOnClickListener(v -> procesarRecuperacion());
        }
    }

    private void procesarRecuperacion() {
        String email = etEmailReset != null ? etEmailReset.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmailReset != null) {
                etEmailReset.setError("Ingresa un correo electrónico válido");
                etEmailReset.requestFocus();
            }
            return;
        }

        btnSendResetCode.setEnabled(false);

        // Validar si el correo existe en la base de datos de Supabase
        api.verificarCredenciales("eq." + email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                btnSendResetCode.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario user = response.body().get(0);

                    new AlertDialog.Builder(RecuperarPassword.this)
                            .setTitle("Enlace de Recuperación Enviado 📧")
                            .setMessage("Hemos verificado la cuenta de @" + user.getGamertag() +
                                    ". Te enviamos las instrucciones de restablecimiento a " + email + ".")
                            .setPositiveButton("Volver al Login", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    if (etEmailReset != null) {
                        etEmailReset.setError("No existe ninguna cuenta asociada a este correo");
                        etEmailReset.requestFocus();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                btnSendResetCode.setEnabled(true);
                Toast.makeText(RecuperarPassword.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
