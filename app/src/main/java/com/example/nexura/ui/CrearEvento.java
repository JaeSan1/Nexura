package com.example.nexura.ui;

import android.net.Uri;
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
import com.example.nexura.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEvento extends AppCompatActivity {

    private ImageView ivEventCover;
    private LinearLayout layoutPlaceholderCover;
    private TextView tvCalculatedXp;

    private Uri uriImagenSeleccionada = null;
    private int xpCalculada = 250;
    private SupabaseApi api;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> selectCoverLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriImagenSeleccionada = uri;
                    ivEventCover.setImageURI(uri);
                    if (layoutPlaceholderCover != null) {
                        layoutPlaceholderCover.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Portada cargada", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBackCreate = findViewById(R.id.btnBackCreate);
        CardView cardSelectCover = findViewById(R.id.cardSelectCover);
        ivEventCover = findViewById(R.id.ivEventCover);
        layoutPlaceholderCover = findViewById(R.id.layoutPlaceholderCover);

        EditText etEventTitle = findViewById(R.id.etEventTitle);
        EditText etEventCategory = findViewById(R.id.etEventCategory);
        EditText etEventDuration = findViewById(R.id.etEventDuration);
        EditText etEventLocation = findViewById(R.id.etEventLocation);
        tvCalculatedXp = findViewById(R.id.tvCalculatedXp);

        Button btnPublishEvent = findViewById(R.id.btnPublishEvent);

        if (btnBackCreate != null) {
            btnBackCreate.setOnClickListener(v -> finish());
        }

        if (cardSelectCover != null) {
            cardSelectCover.setOnClickListener(v -> selectCoverLauncher.launch("image/*"));
        }

        TextWatcher xpWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                calcularXpDinamica(
                        etEventCategory != null ? etEventCategory.getText().toString().trim() : "",
                        etEventDuration != null ? etEventDuration.getText().toString().trim() : ""
                );
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        if (etEventCategory != null) etEventCategory.addTextChangedListener(xpWatcher);
        if (etEventDuration != null) etEventDuration.addTextChangedListener(xpWatcher);

        if (btnPublishEvent != null) {
            btnPublishEvent.setOnClickListener(v -> {
                String title = etEventTitle != null ? etEventTitle.getText().toString().trim() : "";
                String location = etEventLocation != null ? etEventLocation.getText().toString().trim() : "";
                String category = etEventCategory != null ? etEventCategory.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    if (etEventTitle != null) {
                        etEventTitle.setError("Ingresa el título");
                        etEventTitle.requestFocus();
                    }
                    return;
                }

                if (TextUtils.isEmpty(location)) {
                    if (etEventLocation != null) {
                        etEventLocation.setError("Ingresa la ubicación");
                        etEventLocation.requestFocus();
                    }
                    return;
                }

                btnPublishEvent.setEnabled(false);

                if (uriImagenSeleccionada != null) {
                    subirImagenYPublicar(title, location, category, btnPublishEvent);
                } else {
                    publicarEventoBase(title, location, category, "", btnPublishEvent);
                }
            });
        }
    }

    private void subirImagenYPublicar(String title, String location, String category, Button btn) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriImagenSeleccionada);
            if (inputStream == null) {
                publicarEventoBase(title, location, category, "", btn);
                return;
            }

            // Lectura completa y segura mediante ByteArrayOutputStream
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();

            byte[] bytes = byteBuffer.toByteArray();

            if (bytes.length == 0) {
                publicarEventoBase(title, location, category, "", btn);
                return;
            }

            String nombreArchivo = "evento_" + System.currentTimeMillis() + ".jpg";
            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

            api.subirImagenBucket("image/jpeg", nombreArchivo, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String urlPublica = "https://wfkzbqhsacitksuwqlei.supabase.co/storage/v1/object/public/eventos-imagenes/" + nombreArchivo;
                        publicarEventoBase(title, location, category, urlPublica, btn);
                    } else {
                        // Fallback: si falla el bucket, publica el evento sin imagen para no bloquear la app
                        publicarEventoBase(title, location, category, "", btn);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    publicarEventoBase(title, location, category, "", btn);
                }
            });

        } catch (Exception e) {
            publicarEventoBase(title, location, category, "", btn);
        }
    }

    private void publicarEventoBase(String title, String location, String category, String urlImagen, Button btn) {
        Evento nuevoEvento = new Evento();
        nuevoEvento.setTitulo(title);
        nuevoEvento.setUbicacion(location);
        nuevoEvento.setCiudad("Chillán");
        nuevoEvento.setCategoria(TextUtils.isEmpty(category) ? "Festival" : category);
        nuevoEvento.setOrganizador(sessionManager.getGamertag());
        nuevoEvento.setDescripcion("Evento comunitario creado por " + sessionManager.getGamertag());
        nuevoEvento.setFecha("Próximamente");
        nuevoEvento.setImagenUrl(urlImagen);
        nuevoEvento.setXpRecompensa(xpCalculada);
        nuevoEvento.setLatitud(-36.6067);
        nuevoEvento.setLongitud(-72.1034);

        api.crearEvento(nuevoEvento).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btn.setEnabled(true);
                if (response.isSuccessful()) {
                    new AlertDialog.Builder(CrearEvento.this)
                            .setTitle("¡Evento Publicado!")
                            .setMessage("'" + title + "' fue registrado correctamente. +" + xpCalculada + " XP para los asistentes.")
                            .setPositiveButton("Aceptar", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(CrearEvento.this, "Error al publicar evento: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btn.setEnabled(true);
                Toast.makeText(CrearEvento.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
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
        if (tvCalculatedXp != null) {
            tvCalculatedXp.setText("+" + xpCalculada + " XP estimada por asistencia completa");
        }
    }
}