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
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEvento extends AppCompatActivity {

    private ImageView ivEventCover;
    private LinearLayout layoutPlaceholderCover;
    private TextView tvGalleryCount, tvCalculatedXp;

    private Uri uriImagenSeleccionada = null;
    private List<Uri> listaFotosGaleria = new ArrayList<>();
    private int xpCalculada = 250;
    private SupabaseApi api;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> selectCoverLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriImagenSeleccionada = uri;
                    if (ivEventCover != null) ivEventCover.setImageURI(uri);
                    if (layoutPlaceholderCover != null) layoutPlaceholderCover.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<String> selectGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    listaFotosGaleria.clear();
                    listaFotosGaleria.addAll(uris);
                    if (tvGalleryCount != null) {
                        tvGalleryCount.setText(uris.size() + " fotos seleccionadas");
                    }
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
        CardView cardSelectGallery = findViewById(R.id.btnAddGalleryPhoto);

        ivEventCover = findViewById(R.id.ivEventCover);
        layoutPlaceholderCover = findViewById(R.id.layoutPlaceholderCover);
        tvGalleryCount = findViewById(R.id.tvGalleryCount);

        EditText etEventTitle = findViewById(R.id.etEventTitle);
        EditText etEventCategory = findViewById(R.id.etEventCategory);
        EditText etEventDuration = findViewById(R.id.etEventDuration);
        EditText etEventLocation = findViewById(R.id.etEventLocation);
        tvCalculatedXp = findViewById(R.id.tvCalculatedXp);

        Button btnPublishEvent = findViewById(R.id.btnPublishEvent);

        if (btnBackCreate != null) btnBackCreate.setOnClickListener(v -> finish());
        if (cardSelectCover != null) cardSelectCover.setOnClickListener(v -> selectCoverLauncher.launch("image/*"));
        if (cardSelectGallery != null) cardSelectGallery.setOnClickListener(v -> selectGalleryLauncher.launch("image/*"));

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
                    if (etEventTitle != null) etEventTitle.setError("Ingresa el título");
                    return;
                }
                if (TextUtils.isEmpty(location)) {
                    if (etEventLocation != null) etEventLocation.setError("Ingresa la ubicación");
                    return;
                }

                btnPublishEvent.setEnabled(false);
                if (uriImagenSeleccionada != null) {
                    subirImagenYPublicar(title, location, category, btnPublishEvent);
                } else {
                    publicarEventoMap(title, location, category, "", btnPublishEvent);
                }
            });
        }
    }

    private void subirImagenYPublicar(String title, String location, String category, Button btn) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriImagenSeleccionada);
            if (inputStream == null) {
                publicarEventoMap(title, location, category, "", btn);
                return;
            }
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();

            byte[] bytes = byteBuffer.toByteArray();
            String nombreArchivo = "evento_" + System.currentTimeMillis() + ".jpg";
            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

            api.subirImagenBucket("image/jpeg", nombreArchivo, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    String urlPublica = "https://wfkzbqhsacitksuwqlei.supabase.co/storage/v1/object/public/eventos-imagenes/" + nombreArchivo;
                    publicarEventoMap(title, location, category, urlPublica, btn);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    publicarEventoMap(title, location, category, "", btn);
                }
            });

        } catch (Exception e) {
            publicarEventoMap(title, location, category, "", btn);
        }
    }

    private void publicarEventoMap(String title, String location, String category, String urlImagen, Button btn) {
        Map<String, Object> bodyEvento = new HashMap<>();
        bodyEvento.put("titulo", title);
        bodyEvento.put("descripcion", "Evento publicado por " + sessionManager.getGamertag());
        bodyEvento.put("categoria", TextUtils.isEmpty(category) ? "Festival" : category);
        bodyEvento.put("ubicacion", location);
        bodyEvento.put("ciudad", "Chillán");
        bodyEvento.put("fecha", "Próximamente");
        bodyEvento.put("organizador", sessionManager.getGamertag());
        bodyEvento.put("imagen_url", urlImagen);
        bodyEvento.put("xp_recompensa", xpCalculada);
        bodyEvento.put("latitud", -36.6018);
        bodyEvento.put("longitud", -72.0782);

        api.crearEventoMap(bodyEvento).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btn.setEnabled(true);
                if (response.isSuccessful()) {
                    new AlertDialog.Builder(CrearEvento.this)
                            .setTitle("¡Evento Registrado!")
                            .setMessage("Publicado correctamente con +" + xpCalculada + " XP.")
                            .setPositiveButton("Aceptar", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(CrearEvento.this, "Error " + response.code(), Toast.LENGTH_LONG).show();
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
        if (categoria.toLowerCase().contains("festival") || categoria.toLowerCase().contains("concierto")) base += 200;
        else if (categoria.toLowerCase().contains("feria")) base += 100;

        int horas = 1;
        if (!strHoras.isEmpty()) {
            try { horas = Math.min(Integer.parseInt(strHoras), 12); } catch (Exception ignored) {}
        }
        xpCalculada = base + (horas * 50);
        if (tvCalculatedXp != null) {
            tvCalculatedXp.setText("+" + xpCalculada + " XP estimada por asistencia completa");
        }
    }
}