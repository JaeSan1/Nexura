package com.example.nexura.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEvento extends AppCompatActivity {

    private ImageView ivEventCover;
    private LinearLayout layoutPlaceholderCover;
    private TextView tvGalleryCount;
    private TextView tvCalculatedXp;

    private int fotosGaleriaSeleccionadas = 0;
    private int xpCalculada = 250;

    private final ActivityResultLauncher<String> selectCoverLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    ivEventCover.setImageURI(uri);
                    layoutPlaceholderCover.setVisibility(View.GONE);
                    Toast.makeText(this, "Portada cargada", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> selectGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    fotosGaleriaSeleccionadas = uris.size();
                    tvGalleryCount.setText(fotosGaleriaSeleccionadas + " foto(s) seleccionada(s)");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        TextView btnBackCreate = findViewById(R.id.btnBackCreate);
        CardView cardSelectCover = findViewById(R.id.cardSelectCover);
        ivEventCover = findViewById(R.id.ivEventCover);
        layoutPlaceholderCover = findViewById(R.id.layoutPlaceholderCover);

        CardView btnAddGalleryPhoto = findViewById(R.id.btnAddGalleryPhoto);
        tvGalleryCount = findViewById(R.id.tvGalleryCount);

        EditText etEventTitle = findViewById(R.id.etEventTitle);
        EditText etEventCategory = findViewById(R.id.etEventCategory);
        EditText etEventDuration = findViewById(R.id.etEventDuration);
        EditText etEventLocation = findViewById(R.id.etEventLocation);
        tvCalculatedXp = findViewById(R.id.tvCalculatedXp);

        Button btnPublishEvent = findViewById(R.id.btnPublishEvent);

        if (btnBackCreate != null) {
            btnBackCreate.setOnClickListener(v -> finish());
        }

        cardSelectCover.setOnClickListener(v -> selectCoverLauncher.launch("image/*"));
        btnAddGalleryPhoto.setOnClickListener(v -> selectGalleryLauncher.launch("image/*"));

        TextWatcher xpWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                calcularXpDinamica(
                        etEventCategory.getText().toString().trim(),
                        etEventDuration.getText().toString().trim()
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEventCategory.addTextChangedListener(xpWatcher);
        etEventDuration.addTextChangedListener(xpWatcher);

        btnPublishEvent.setOnClickListener(v -> {
            String title = etEventTitle.getText().toString().trim();
            String location = etEventLocation.getText().toString().trim();
            String category = etEventCategory.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etEventTitle.setError("Ingresa el título");
                etEventTitle.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(location)) {
                etEventLocation.setError("Ingresa la ubicación");
                etEventLocation.requestFocus();
                return;
            }

            // Deshabilitar botón temporalmente para evitar doble envío
            btnPublishEvent.setEnabled(false);

            // 1. Armar el objeto Evento con los nombres mapeados a Supabase
            Evento nuevoEvento = new Evento();
            nuevoEvento.setTitulo(title);
            nuevoEvento.setUbicacion(location);
            nuevoEvento.setCiudad("Chillán");
            nuevoEvento.setCategoria(category.isEmpty() ? "Festival" : category);
            nuevoEvento.setOrganizador("Organizador Nexura");
            nuevoEvento.setDescripcion("Evento comunitario publicado desde la app Nexura.");
            nuevoEvento.setFecha("Próximamente");
            nuevoEvento.setImagenUrl("");
            nuevoEvento.setXpRecompensa(xpCalculada);
            nuevoEvento.setLatitud(-36.6067); // Coordenadas base
            nuevoEvento.setLongitud(-72.1034);

            // 2. Llamada HTTP a Supabase vía Retrofit
            SupabaseApi api = SupabaseCliente.getClient().create(SupabaseApi.class);
            api.crearEvento(nuevoEvento).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnPublishEvent.setEnabled(true);
                    if (response.isSuccessful()) {
                        new AlertDialog.Builder(CrearEvento.this)
                                .setTitle("¡Evento Publicado en la Nube! 🚀")
                                .setMessage("'" + title + "' ha sido registrado exitosamente en Supabase con +" + xpCalculada + " XP.")
                                .setPositiveButton("Aceptar", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(CrearEvento.this, "Error del servidor: código " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnPublishEvent.setEnabled(true);
                    Toast.makeText(CrearEvento.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void calcularXpDinamica(String categoria, String strHoras) {
        int base = 200;

        if (categoria.toLowerCase().contains("festival") || categoria.toLowerCase().contains("concierto")) {
            base += 200;
        } else if (categoria.toLowerCase().contains("feria") || categoria.toLowerCase().contains("expo")) {
            base += 100;
        }

        int horas = 1;
        if (!strHoras.isEmpty()) {
            try {
                horas = Integer.parseInt(strHoras);
                if (horas > 12) horas = 12;
            } catch (NumberFormatException ignored) {}
        }

        xpCalculada = base + (horas * 50);
        tvCalculatedXp.setText("+" + xpCalculada + " XP estimada por asistencia completa");
    }
}
