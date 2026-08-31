package com.example.nexura.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;

public class RecuperarPassword extends AppCompatActivity {

    private boolean codigoEnviado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        TextView btnBackRecover = findViewById(R.id.btnBackRecover);
        TextView tvCancelRecover = findViewById(R.id.tvCancelRecover);
        TextView tvRecoverSubtitle = findViewById(R.id.tvRecoverSubtitle);
        EditText etRecoverEmail = findViewById(R.id.etRecoverEmail);
        LinearLayout layoutStep2 = findViewById(R.id.layoutStep2);
        EditText etSecurityCode = findViewById(R.id.etSecurityCode);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        Button btnActionRecovery = findViewById(R.id.btnActionRecovery);

        if (btnBackRecover != null) btnBackRecover.setOnClickListener(v -> finish());
        if (tvCancelRecover != null) tvCancelRecover.setOnClickListener(v -> finish());

        btnActionRecovery.setOnClickListener(v -> {
            // PASO 1: ENVIAR CÓDIGO
            if (!codigoEnviado) {
                String email = etRecoverEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etRecoverEmail.setError("Ingresa tu correo registrado");
                    etRecoverEmail.requestFocus();
                    return;
                }

                // Simular envío y pasar al Paso 2
                codigoEnviado = true;
                etRecoverEmail.setEnabled(false); // Bloquea el correo
                layoutStep2.setVisibility(View.VISIBLE); // Muestra código y nueva clave
                tvRecoverSubtitle.setText("Hemos enviado un código a " + email + ". Ingrésalo junto a tu nueva contraseña.");
                btnActionRecovery.setText("Actualizar Contraseña");

                Toast.makeText(this, "Código enviado: usa 123456 para probar", Toast.LENGTH_LONG).show();

                // PASO 2: VALIDAR CÓDIGO Y CAMBIAR CONTRASEÑA
            } else {
                String codigo = etSecurityCode.getText().toString().trim();
                String nuevaClave = etNewPassword.getText().toString().trim();

                if (TextUtils.isEmpty(codigo) || codigo.length() < 6) {
                    etSecurityCode.setError("Ingresa el código de 6 dígitos");
                    etSecurityCode.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(nuevaClave) || nuevaClave.length() < 6) {
                    etNewPassword.setError("La nueva contraseña debe tener al menos 6 caracteres");
                    etNewPassword.requestFocus();
                    return;
                }

                // Éxito
                new AlertDialog.Builder(RecuperarPassword.this)
                        .setTitle("¡Contraseña Actualizada! 🔒")
                        .setMessage("Tu contraseña ha sido cambiada correctamente. Ya puedes iniciar sesión con tus nuevas credenciales.")
                        .setPositiveButton("Iniciar Sesión", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        });
    }
}
