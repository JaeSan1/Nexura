package com.example.nexura.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nexura.R;
import com.example.nexura.model.Usuario;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfil extends AppCompatActivity {

    private ImageView ivAvatarEdit, ivBannerEdit;
    private FrameLayout containerBannerEdit;
    private EditText etGamertagEdit, etCityEdit, etBioEdit;
    private Button btnSaveProfile;

    // Referencias al Overlay de Carga
    private View layoutLoadingOverlay;
    private TextView tvLoadingText;

    private Uri uriAvatarSeleccionado = null;
    private Uri uriBannerSeleccionado = null;

    private SupabaseApi api;
    private SessionManager sessionManager;

    // Launcher para Seleccionar Avatar
    private final ActivityResultLauncher<String> selectAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriAvatarSeleccionado = uri;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        if (ivAvatarEdit != null) ivAvatarEdit.setImageBitmap(bitmap);
                        Toast.makeText(this, "Avatar seleccionado", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        if (ivAvatarEdit != null) ivAvatarEdit.setImageURI(uri);
                    }
                }
            });

    // Launcher para Seleccionar Banner
    private final ActivityResultLauncher<String> selectBannerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriBannerSeleccionado = uri;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        if (ivBannerEdit != null) ivBannerEdit.setImageBitmap(bitmap);
                        Toast.makeText(this, "Banner seleccionado", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        if (ivBannerEdit != null) ivBannerEdit.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        TextView btnBack = findViewById(R.id.btnBackEditProfile);
        ivAvatarEdit = findViewById(R.id.ivAvatarEdit);
        ivBannerEdit = findViewById(R.id.ivBannerEdit);
        containerBannerEdit = findViewById(R.id.containerBannerEdit);
        etGamertagEdit = findViewById(R.id.etGamertagEdit);
        etCityEdit = findViewById(R.id.etCityEdit);
        etBioEdit = findViewById(R.id.etBioEdit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Overlay de Carga
        layoutLoadingOverlay = findViewById(R.id.layoutLoadingOverlay);
        tvLoadingText = findViewById(R.id.tvLoadingText);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (ivAvatarEdit != null) ivAvatarEdit.setOnClickListener(v -> selectAvatarLauncher.launch("image/*"));
        if (containerBannerEdit != null) containerBannerEdit.setOnClickListener(v -> selectBannerLauncher.launch("image/*"));

        // Cargar datos actuales del usuario
        cargarDatosActuales();

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> procesarGuardado());
        }
    }

    private void mostrarCargando(String mensaje) {
        if (layoutLoadingOverlay != null) {
            if (tvLoadingText != null && mensaje != null) {
                tvLoadingText.setText(mensaje);
            }
            layoutLoadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarCargando() {
        if (layoutLoadingOverlay != null) {
            layoutLoadingOverlay.setVisibility(View.GONE);
        }
    }

    private void cargarDatosActuales() {
        String userId = sessionManager.getUserId();
        if (TextUtils.isEmpty(userId)) {
            if (etGamertagEdit != null) etGamertagEdit.setText(sessionManager.getGamertag());
            return;
        }

        mostrarCargando("Cargando perfil...");

        api.obtenerPerfilPorId("eq." + userId).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                ocultarCargando();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario user = response.body().get(0);
                    if (etGamertagEdit != null) etGamertagEdit.setText(user.getGamertag());
                    if (etCityEdit != null && user.getCiudad() != null) etCityEdit.setText(user.getCiudad());
                    if (etBioEdit != null && user.getBiografia() != null) etBioEdit.setText(user.getBiografia());
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                ocultarCargando();
            }
        });
    }

    private void procesarGuardado() {
        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnSaveProfile != null) btnSaveProfile.setEnabled(false);

        if (uriAvatarSeleccionado != null) {
            mostrarCargando("Subiendo avatar...");
            subirImagenABucket(uriAvatarSeleccionado, "avatar_", urlAvatar -> {
                if (uriBannerSeleccionado != null) {
                    mostrarCargando("Subiendo banner...");
                    subirImagenABucket(uriBannerSeleccionado, "banner_", urlBanner -> guardarPerfilEnSupabase(urlAvatar, urlBanner));
                } else {
                    guardarPerfilEnSupabase(urlAvatar, "");
                }
            });
        } else if (uriBannerSeleccionado != null) {
            mostrarCargando("Subiendo banner...");
            subirImagenABucket(uriBannerSeleccionado, "banner_", urlBanner -> guardarPerfilEnSupabase("", urlBanner));
        } else {
            mostrarCargando("Guardando cambios...");
            guardarPerfilEnSupabase("", "");
        }
    }

    private interface UploadCallback {
        void onCompleted(String urlPublica);
    }

    private void subirImagenABucket(Uri uriImagen, String prefijo, UploadCallback callback) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriImagen);
            if (inputStream == null) {
                callback.onCompleted("");
                return;
            }

            // Lectura segura mediante buffer dinámico (evita inputStream.available() == 0)
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();

            byte[] bytes = byteBuffer.toByteArray();

            if (bytes.length == 0) {
                callback.onCompleted("");
                return;
            }

            String nombreArchivo = prefijo + System.currentTimeMillis() + ".jpg";
            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

            api.subirImagenBucket("image/jpeg", nombreArchivo, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String urlPublica = "https://wfkzbqhsacitksuwqlei.supabase.co/storage/v1/object/public/eventos-imagenes/" + nombreArchivo;
                        callback.onCompleted(urlPublica);
                    } else {
                        // Fallback si falla la subida de la imagen
                        callback.onCompleted("");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    callback.onCompleted("");
                }
            });
        } catch (Exception e) {
            callback.onCompleted("");
        }
    }

    private void guardarPerfilEnSupabase(String urlAvatar, String urlBanner) {
        String gamertag = etGamertagEdit != null ? etGamertagEdit.getText().toString().trim() : "";
        String ciudad = etCityEdit != null ? etCityEdit.getText().toString().trim() : "";
        String bio = etBioEdit != null ? etBioEdit.getText().toString().trim() : "";

        if (TextUtils.isEmpty(gamertag)) {
            ocultarCargando();
            if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
            Toast.makeText(this, "El Gamertag no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("gamertag", gamertag);
        body.put("ciudad", ciudad);
        body.put("biografia", bio);
        if (!TextUtils.isEmpty(urlAvatar)) body.put("foto_perfil_url", urlAvatar);
        if (!TextUtils.isEmpty(urlBanner)) body.put("banner_url", urlBanner);

        String filtro = "eq." + sessionManager.getUserId();

        api.actualizarPerfil(filtro, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                ocultarCargando();
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);

                if (response.isSuccessful()) {
                    sessionManager.guardarSesion(
                            sessionManager.getUserId(),
                            gamertag,
                            sessionManager.getEmail()
                    );
                    Toast.makeText(EditarPerfil.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditarPerfil.this, "Error al actualizar perfil: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ocultarCargando();
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                Toast.makeText(EditarPerfil.this, "Error de red al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}