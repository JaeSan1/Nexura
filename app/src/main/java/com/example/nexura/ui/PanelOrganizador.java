package com.example.nexura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Notificacion;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanelOrganizador extends AppCompatActivity {

    private SupabaseApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_organizador);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackOrganizer);
        Button btnCreateNewEvent = findViewById(R.id.btnCreateNewEvent);
        Button btnSendBroadcastAlert = findViewById(R.id.btnSendBroadcastAlert);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Acceso a creación de un nuevo evento
        if (btnCreateNewEvent != null) {
            btnCreateNewEvent.setOnClickListener(v ->
                    startActivity(new Intent(PanelOrganizador.this, CrearEvento.class)));
        }

        // 2. Emitir comunicado / aviso urgente a la comunidad
        if (btnSendBroadcastAlert != null) {
            btnSendBroadcastAlert.setOnClickListener(v -> mostrarDialogoEmisionAviso());
        }
    }

    private void mostrarDialogoEmisionAviso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📢 Emitir Comunicado Urgente");

        // Contenedor dinámico de inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputTitulo = new EditText(this);
        inputTitulo.setHint("Título del comunicado (ej. Cambio de hora)");
        layout.addView(inputTitulo);

        final EditText inputMensaje = new EditText(this);
        inputMensaje.setHint("Detalle del mensaje a la comunidad");
        layout.addView(inputMensaje);

        builder.setView(layout);

        builder.setPositiveButton("Transmitir", (dialog, which) -> {
            String titulo = inputTitulo.getText().toString().trim();
            String mensaje = inputMensaje.getText().toString().trim();

            if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(mensaje)) {
                Toast.makeText(this, "Completa el título y el mensaje", Toast.LENGTH_SHORT).show();
                return;
            }

            publicarNotificacionEnNube(titulo, mensaje);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void publicarNotificacionEnNube(String titulo, String mensaje) {
        Notificacion nuevaNotificacion = new Notificacion();
        nuevaNotificacion.setTitulo(titulo);
        nuevaNotificacion.setMensaje(mensaje);
        nuevaNotificacion.setTiempo("Ahora");
        nuevaNotificacion.setTipo("AVISO");
        nuevaNotificacion.setLeida(false);

        api.emitirAvisoOrganizador(nuevaNotificacion).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PanelOrganizador.this, "¡Aviso transmitido a todos los usuarios!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PanelOrganizador.this, "Error al emitir: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PanelOrganizador.this, "Falla de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}