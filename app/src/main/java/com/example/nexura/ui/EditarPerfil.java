package com.example.nexura.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.bumptech.glide.Glide;
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

    private View layoutLoadingOverlay;
    private TextView tvLoadingText;

    private Uri uriAvatarSeleccionado = null;
    private Uri uriBannerSeleccionado = null;

    private SupabaseApi api;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> selectAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriAvatarSeleccionado = uri;
                    if (ivAvatarEdit != null) {
                        Glide.with(this).load(uri).circleCrop().into(ivAvatarEdit);
                    }
                    Toast.makeText(this, "Avatar seleccionado", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> selectBannerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriBannerSeleccionado = uri;
                    if (ivBannerEdit != null) {
                        Glide.with(this).load(uri).centerCrop().into(ivBannerEdit);
                    }
                    Toast.makeText(this, "Banner seleccionado", Toast.LENGTH_SHORT).show();
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

        layoutLoadingOverlay = findViewById(R.id.layoutLoadingOverlay);
        tvLoadingText = findViewById(R.id.tvLoadingText);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Clic para cambiar avatar
        if (ivAvatarEdit != null) {
            ivAvatarEdit.setClickable(true);
            ivAvatarEdit.setOnClickListener(v -> selectAvatarLauncher.launch("image/*"));
        }

        // Clic en contenedor e imagen del banner para evitar bloqueos táctiles
        View.OnClickListener abrirGaleriaBanner = v -> selectBannerLauncher.launch("image/*");
        if (containerBannerEdit != null) {
            containerBannerEdit.setClickable(true);
            containerBannerEdit.setOnClickListener(abrirGaleriaBanner);
        }
        if (ivBannerEdit != null) {
            ivBannerEdit.setClickable(true);
            ivBannerEdit.setOnClickListener(abrirGaleriaBanner);
        }

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

                    if (ivAvatarEdit != null && !TextUtils.isEmpty(user.getFotoPerfilUrl())) {
                        Glide.with(EditarPerfil.this)
                                .load(user.getFotoPerfilUrl())
                                .circleCrop()
                                .into(ivAvatarEdit);
                    }

                    if (ivBannerEdit != null && !TextUtils.isEmpty(user.getBannerUrl())) {
                        Glide.with(EditarPerfil.this)
                                .load(user.getBannerUrl())
                                .centerCrop()
                                .into(ivBannerEdit);
                    }
                } else {
                    Log.e("ERROR_PERFIL", "No se encontró el perfil para el ID: " + userId);
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                ocultarCargando();
                Log.e("ERROR_PERFIL", "Fallo de red al cargar perfil: " + t.getMessage());
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

            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (original == null) {
                callback.onCompleted("");
                return;
            }

            int maxDim = 1080;
            int width = original.getWidth();
            int height = original.getHeight();
            Bitmap bitmapFinal = original;

            if (width > maxDim || height > maxDim) {
                float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                bitmapFinal = Bitmap.createScaledBitmap(
                        original, Math.round(width * ratio), Math.round(height * ratio), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 82, baos);
            byte[] bytes = baos.toByteArray();

            String nombreArchivo = prefijo + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg";
            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

            api.subirImagenBucket("image/jpeg", nombreArchivo, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String urlPublica = "https://wfkzbqhsacitksuwqlei.supabase.co/storage/v1/object/public/eventos-imagenes/" + nombreArchivo;
                        callback.onCompleted(urlPublica);
                    } else {
                        try {
                            String err = response.errorBody() != null ? response.errorBody().string() : "";
                            Log.e("SUPABASE_STORAGE", "Error " + response.code() + ": " + err);
                            Toast.makeText(EditarPerfil.this, "Error subiendo imagen (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        } catch (Exception ignored) {}
                        callback.onCompleted("");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SUPABASE_STORAGE", "Fallo de red en Storage: " + t.getMessage());
                    callback.onCompleted("");
                }
            });
        } catch (Exception e) {
            Log.e("SUPABASE_STORAGE", "Excepción al procesar imagen: " + e.getMessage());
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
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorExacto = response.errorBody() != null ? response.errorBody().string() : "Sin detalles";
                        Log.e("ERROR_SUPABASE", "Detalle del " + response.code() + ": " + errorExacto);
                        Toast.makeText(EditarPerfil.this, "Error " + response.code() + ": " + errorExacto, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(EditarPerfil.this, "Error al guardar el perfil", Toast.LENGTH_LONG).show();
                    }
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