package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.nexura.R;

public class PanelOrganizador extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_organizador);

        TextView btnBackOrganizer = findViewById(R.id.btnBackOrganizer);
        EditText etBroadcastMessage = findViewById(R.id.etBroadcastMessage);
        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
        CardView cardActiveEvent1 = findViewById(R.id.cardActiveEvent1);
        Button btnCreateFromPanel = findViewById(R.id.btnCreateFromPanel);

        if (btnBackOrganizer != null) {
            btnBackOrganizer.setOnClickListener(v -> finish());
        }

        // 1. Enviar Aviso Urgente en tiempo real
        if (btnSendBroadcast != null) {
            btnSendBroadcast.setOnClickListener(v -> {
                String aviso = etBroadcastMessage.getText().toString().trim();
                if (TextUtils.isEmpty(aviso)) {
                    etBroadcastMessage.setError("Escribe el mensaje del aviso");
                    etBroadcastMessage.requestFocus();
                    return;
                }

                new AlertDialog.Builder(PanelOrganizador.this)
                        .setTitle("Aviso Urgente Enviado")
                        .setMessage("Se ha transmitido la notificación a todos los usuarios con el evento guardado y activos en la zona.")
                        .setPositiveButton("Entendido", (dialog, which) -> etBroadcastMessage.setText(""))
                        .show();
            });
        }

        // 2. Ver Detalle del Evento Activo
        if (cardActiveEvent1 != null) {
            cardActiveEvent1.setOnClickListener(v -> {
                startActivity(new Intent(PanelOrganizador.this, Detalle_Evento.class));
            });
        }

        // 3. Crear nuevo evento desde el panel
        if (btnCreateFromPanel != null) {
            btnCreateFromPanel.setOnClickListener(v -> {
                startActivity(new Intent(PanelOrganizador.this, CrearEvento.class));
            });
        }
    }
}