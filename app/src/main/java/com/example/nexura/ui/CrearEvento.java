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

            new AlertDialog.Builder(CrearEvento.this)
                    .setTitle("¡Evento Publicado Oficialmente!")
                    .setMessage("'" + title + "' ha sido aprobado con +" + xpCalculada + " XP de recompensa verificada.")
                    .setPositiveButton("Aceptar", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
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
