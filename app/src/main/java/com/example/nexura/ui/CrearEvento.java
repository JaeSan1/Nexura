package com.example.nexura.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.example.nexura.util.NetworkUtils;
import com.example.nexura.util.SessionManager;

import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEvento extends AppCompatActivity {

    private EditText etTitle, etDescription, etCategory, etDate, etCity, etLocation, etXp, etLat, etLng;
    private ImageView ivPreview;
    private Button btnSave, btnSeleccionarMapa;
    private TextView tvHeaderTitle;

    private Uri uriImagenSeleccionada = null;
    private SupabaseApi api;
    private SessionManager sessionManager;

    private Evento eventoAEditar = null;
    private boolean esModoEdicion = false;

    private final String[] categoriasDisponibles = {
            "Gaming", "Anime y Manga", "Deportes", "Música",
            "Tecnología", "Cultura y Arte", "Cosplay", "Torneos"
    };
    private final boolean[] categoriasSeleccionadas = new boolean[categoriasDisponibles.length];

    private final String[] ciudadesDisponibles = {
            "Chillán", "Chillán Viejo", "San Carlos", "Bulnes",
            "Quillón", "San Nicolás", "Concepción", "Los Ángeles", "Santiago"
    };

    private int xpCalculada = 100;

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriImagenSeleccionada = uri;
                    if (ivPreview != null) {
                        Glide.with(this).load(uri).centerCrop().into(ivPreview);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> seleccionarMapaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra("LATITUD", 0.0);
                    double lng = data.getDoubleExtra("LONGITUD", 0.0);
                    String direccion = data.getStringExtra("DIRECCION");
                    String ciudad = data.getStringExtra("CIUDAD");

                    if (lat != 0.0 && lng != 0.0) {
                        etLat.setText(String.format(Locale.US, "%.6f", lat));
                        etLng.setText(String.format(Locale.US, "%.6f", lng));
                    }
                    if (!TextUtils.isEmpty(direccion)) {
                        etLocation.setText(direccion);
                    }
                    if (TextUtils.isEmpty(etCity.getText().toString().trim()) && !TextUtils.isEmpty(ciudad)) {
                        etCity.setText(ciudad);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        sessionManager = new SessionManager(this);
        api = SupabaseCliente.getClient().create(SupabaseApi.class);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etCategory = findViewById(R.id.etEventCategory);
        etDate = findViewById(R.id.etEventDate);
        etCity = findViewById(R.id.etEventCity);
        etLocation = findViewById(R.id.etEventLocation);
        etXp = findViewById(R.id.etEventXp);
        etLat = findViewById(R.id.etEventLat);
        etLng = findViewById(R.id.etEventLng);
        ivPreview = findViewById(R.id.ivEventPreview);
        btnSave = findViewById(R.id.btnSaveEvent);
        btnSeleccionarMapa = findViewById(R.id.btnSeleccionarMapa);

        configurarCamposSoloSeleccion();

        TextView btnBack = findViewById(R.id.btnBackCreate);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (ivPreview != null) ivPreview.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
        if (btnSeleccionarMapa != null) btnSeleccionarMapa.setOnClickListener(v -> abrirMapaPantallaCompleta());

        if (getIntent().hasExtra("EVENTO_A_EDITAR")) {
            eventoAEditar = (Evento) getIntent().getSerializableExtra("EVENTO_A_EDITAR");
            if (eventoAEditar != null) {
                esModoEdicion = true;
                cargarDatosEnFormulario();
            }
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> procesarGuardado());
        }
    }

    private void abrirMapaPantallaCompleta() {
        double latInicial = 0.0;
        double lngInicial = 0.0;
        boolean usarGpsAlAbrir = true;

        try {
            String latTxt = etLat.getText().toString().trim();
            String lngTxt = etLng.getText().toString().trim();
            if (!TextUtils.isEmpty(latTxt) && !TextUtils.isEmpty(lngTxt)) {
                double lat = Double.parseDouble(latTxt);
                double lng = Double.parseDouble(lngTxt);
                if (lat != 0 && lng != 0) {
                    latInicial = lat;
                    lngInicial = lng;
                    usarGpsAlAbrir = false;
                }
            }
        } catch (NumberFormatException ignored) {}

        Intent intent = new Intent(this, MapaInteractivoActivity.class);
        intent.putExtra("MODO_SELECCION", true);
        intent.putExtra("USAR_GPS_INICIAL", usarGpsAlAbrir);
        if (!usarGpsAlAbrir) {
            intent.putExtra("LAT_INICIAL", latInicial);
            intent.putExtra("LNG_INICIAL", lngInicial);
        }
        intent.putExtra("CIUDAD_ACTUAL", etCity.getText().toString().trim());
        intent.putExtra("DIRECCION_ACTUAL", etLocation.getText().toString().trim());
        seleccionarMapaLauncher.launch(intent);
    }

    private GeoPoint obtenerCentroPorCiudad(String ciudad) {
        if (TextUtils.isEmpty(ciudad)) return new GeoPoint(-36.6066, -72.1033);
        switch (ciudad) {
            case "Chillán Viejo":
                return new GeoPoint(-36.6231, -72.1319);
            case "San Carlos":
                return new GeoPoint(-36.4248, -71.9581);
            case "Bulnes":
                return new GeoPoint(-36.7423, -72.2985);
            case "Quillón":
                return new GeoPoint(-36.7436, -72.4706);
            case "San Nicolás":
                return new GeoPoint(-36.5028, -72.2128);
            case "Concepción":
                return new GeoPoint(-36.8201, -73.0444);
            case "Los Ángeles":
                return new GeoPoint(-37.4689, -72.3527);
            case "Santiago":
                return new GeoPoint(-33.4489, -70.6693);
            case "Chillán":
            default:
                return new GeoPoint(-36.6066, -72.1033);
        }
    }

    private void configurarCamposSoloSeleccion() {
        if (etCategory != null) {
            etCategory.setFocusable(false);
            etCategory.setClickable(true);
            etCategory.setCursorVisible(false);
            etCategory.setHint("Toca para elegir una o más categorías");
            etCategory.setOnClickListener(v -> mostrarSelectorCategorias());
        }

        if (etDate != null) {
            etDate.setFocusable(false);
            etDate.setClickable(true);
            etDate.setCursorVisible(false);
            etDate.setHint("Selecciona fecha, hora y duración");
            etDate.setOnClickListener(v -> mostrarSelectorFechaYHora());
        }

        if (etCity != null) {
            etCity.setFocusable(false);
            etCity.setClickable(true);
            etCity.setCursorVisible(false);
            etCity.setHint("Selecciona una ciudad");
            etCity.setOnClickListener(v -> mostrarSelectorCiudad());
        }

        if (etXp != null) {
            etXp.setFocusable(false);
            etXp.setClickable(false);
            etXp.setCursorVisible(false);
            etXp.setText("100 XP (Se calcula según las horas)");
        }
    }

    private void mostrarSelectorCategorias() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona categorías");
        builder.setMultiChoiceItems(categoriasDisponibles, categoriasSeleccionadas, (dialog, which, isChecked) -> {
            categoriasSeleccionadas[which] = isChecked;
        });

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            List<String> elegidas = new ArrayList<>();
            for (int i = 0; i < categoriasDisponibles.length; i++) {
                if (categoriasSeleccionadas[i]) {
                    elegidas.add(categoriasDisponibles[i]);
                }
            }
            etCategory.setText(TextUtils.join(", ", elegidas));
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarSelectorCiudad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona la ciudad");
        builder.setItems(ciudadesDisponibles, (dialog, which) -> {
            etCity.setText(ciudadesDisponibles[which]);
        });
        builder.show();
    }

    private void mostrarSelectorFechaYHora() {
        Calendar calendario = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendario.set(Calendar.YEAR, year);
                    calendario.set(Calendar.MONTH, month);
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendario.set(Calendar.MINUTE, minute);
                                mostrarSelectorDuracion(calendario);
                            },
                            calendario.get(Calendar.HOUR_OF_DAY),
                            calendario.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.setTitle("Hora de inicio");
                    timePicker.show();
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void mostrarSelectorDuracion(Calendar fechaInicio) {
        String[] opcionesHoras = {
                "1 hora (+100 XP)",
                "2 horas (+200 XP)",
                "3 horas (+300 XP)",
                "4 horas (+400 XP)",
                "5 horas (+500 XP)",
                "6 horas o más (+600 XP)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duración del evento");
        builder.setItems(opcionesHoras, (dialog, which) -> {
            int horas = which + 1;
            xpCalculada = horas * 100;

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM - HH:mm", new Locale("es", "CL"));
            String fechaFormateada = sdf.format(fechaInicio.getTime()) + " (" + horas + "h)";
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);

            etDate.setText(fechaFormateada);
            if (etXp != null) {
                etXp.setText(xpCalculada + " XP (" + horas + " hrs)");
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void cargarDatosEnFormulario() {
        if (tvHeaderTitle != null) tvHeaderTitle.setText("Editar Evento");
        if (btnSave != null) btnSave.setText("Guardar Cambios");

        if (etTitle != null) etTitle.setText(eventoAEditar.getTitulo());
        if (etDescription != null) etDescription.setText(eventoAEditar.getDescripcion());

        if (etCategory != null) {
            String catActual = eventoAEditar.getCategoria();
            etCategory.setText(catActual);
            if (!TextUtils.isEmpty(catActual)) {
                for (int i = 0; i < categoriasDisponibles.length; i++) {
                    if (catActual.contains(categoriasDisponibles[i])) {
                        categoriasSeleccionadas[i] = true;
                    }
                }
            }
        }

        if (etDate != null) etDate.setText(eventoAEditar.getFecha());
        if (etCity != null) etCity.setText(eventoAEditar.getCiudad());
        if (etLocation != null) etLocation.setText(eventoAEditar.getUbicacion());

        xpCalculada = eventoAEditar.getXpRecompensa() > 0 ? eventoAEditar.getXpRecompensa() : 100;
        if (etXp != null) etXp.setText(xpCalculada + " XP");

        if (etLat != null) etLat.setText(String.valueOf(eventoAEditar.getLatitud()));
        if (etLng != null) etLng.setText(String.valueOf(eventoAEditar.getLongitud()));

        if (ivPreview != null && !TextUtils.isEmpty(eventoAEditar.getPortadaUrl())) {
            Glide.with(this).load(eventoAEditar.getPortadaUrl()).centerCrop().into(ivPreview);
        }
    }

    private void procesarGuardado() {
        if (!NetworkUtils.hayInternet(this)) {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String cat = etCategory.getText().toString().trim();
        String fecha = etDate.getText().toString().trim();
        String ciudad = etCity.getText().toString().trim();
        String ubicacion = etLocation.getText().toString().trim();
        String latStr = etLat.getText().toString().trim();
        String lngStr = etLng.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(cat) || TextUtils.isEmpty(fecha)
                || TextUtils.isEmpty(ciudad) || TextUtils.isEmpty(ubicacion)) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) {
            Toast.makeText(this, "Selecciona la ubicación del evento en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        Toast.makeText(this, "Guardando evento...", Toast.LENGTH_SHORT).show();

        if (uriImagenSeleccionada != null) {
            subirImagenYGuardar(titulo, desc, cat, fecha, ciudad, ubicacion, xpCalculada, latStr, lngStr);
        } else {
            String urlImagenFinal = (esModoEdicion && eventoAEditar != null && eventoAEditar.getPortadaUrl() != null)
                    ? eventoAEditar.getPortadaUrl() : "";
            enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xpCalculada, latStr, lngStr, urlImagenFinal);
        }
    }

    private void subirImagenYGuardar(String titulo, String desc, String cat, String fecha, String ciudad,
                                     String ubicacion, int xp, String latStr, String lngStr) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriImagenSeleccionada);
            if (inputStream == null) {
                enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, "");
                return;
            }

            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (original == null) {
                enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, "");
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

            String nombreArchivo = "evento_" + System.currentTimeMillis() + ".jpg";
            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

            api.subirImagenBucket("image/jpeg", nombreArchivo, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String urlPublica = "https://wfkzbqhsacitksuwqlei.supabase.co/storage/v1/object/public/eventos-imagenes/" + nombreArchivo;
                        enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, urlPublica);
                    } else {
                        Log.e("SUPABASE_STORAGE", "Error subiendo portada: " + response.code());
                        Toast.makeText(CrearEvento.this, "No se pudo subir la portada (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, "");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, "");
                }
            });
        } catch (Exception e) {
            enviarEventoASupabase(titulo, desc, cat, fecha, ciudad, ubicacion, xp, latStr, lngStr, "");
        }
    }

    private void enviarEventoASupabase(String titulo, String desc, String cat, String fecha, String ciudad,
                                       String ubicacion, int xp, String latStr, String lngStr, String urlImagen) {

        double lat = 0.0;
        double lng = 0.0;
        try {
            if (!TextUtils.isEmpty(latStr)) lat = Double.parseDouble(latStr);
            if (!TextUtils.isEmpty(lngStr)) lng = Double.parseDouble(lngStr);
        } catch (NumberFormatException ignored) {}

        Map<String, Object> body = new HashMap<>();
        body.put("titulo", titulo);
        body.put("descripcion", desc);
        body.put("categoria", cat);
        body.put("fecha", fecha);
        body.put("ciudad", ciudad);
        body.put("ubicacion", ubicacion);
        body.put("xp_recompensa", xp);
        body.put("latitud", lat);
        body.put("longitud", lng);
        body.put("portada_url", urlImagen != null ? urlImagen : "");

        if (esModoEdicion) {
            api.actualizarEvento("eq." + eventoAEditar.getId(), body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(CrearEvento.this, "Evento actualizado correctamente", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mostrarErrorDetallado(response);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(CrearEvento.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String userId = sessionManager.getUserId();
            if (TextUtils.isEmpty(userId)) {
                btnSave.setEnabled(true);
                Toast.makeText(this, "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                return;
            }

            String gamertag = sessionManager.getGamertag();
            if (TextUtils.isEmpty(gamertag)) {
                gamertag = "Usuario Nexura";
            }

            body.put("organizador_id", userId);
            body.put("organizador_nombre", gamertag);
            body.put("contador_likes", 0);
            body.put("contador_asistentes", 0);
            body.put("creador_verificado", false);

            api.crearEvento(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(CrearEvento.this, "¡Evento publicado con éxito!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mostrarErrorDetallado(response);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(CrearEvento.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarErrorDetallado(Response<Void> response) {
        try {
            String errorDetalle = response.errorBody() != null ? response.errorBody().string() : "Sin detalle";
            Log.e("SUPABASE_ERROR", "Código: " + response.code() + " | Detalle: " + errorDetalle);
            Toast.makeText(this, "Error " + response.code() + ": " + errorDetalle, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }
}