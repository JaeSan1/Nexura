package com.example.nexura.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.network.SupabaseApi;
import com.example.nexura.network.SupabaseCliente;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaInteractivoActivity extends AppCompatActivity {

    private MapView mapView;
    private SupabaseApi api;
    private FusedLocationProviderClient fusedLocationClient;

    private final ExecutorService executorMapa = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClientBusqueda = new OkHttpClient();

    private boolean modoSeleccion = false;
    private TextView tvMapHeaderTitle, tvDireccionSeleccionadaMap;
    private LinearLayout layoutBuscadorMapa, listaSugerenciasMapa, layoutConfirmarMapa;
    private ScrollView scrollSugerenciasMapa;
    private EditText etBuscarDireccionMap;
    private Button btnBuscarDireccionMap, btnMiGpsMap, btnConfirmarUbicacionMap;

    private Marker pinSeleccion;
    private GeoPoint puntoSeleccionado;
    private String direccionSeleccionada = "";
    private String ciudadSeleccionada = "";

    private final ActivityResultLauncher<String[]> requestLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    centrarEnUbicacionActual(true);
                }
            });

    private static class ResultadoLugar {
        final String nombreCorto;
        final String ciudad;
        final double lat;
        final double lon;

        ResultadoLugar(String nombreCorto, String ciudad, double lat, double lon) {
            this.nombreCorto = nombreCorto;
            this.ciudad = ciudad;
            this.lat = lat;
            this.lon = lon;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_interactivo);

        api = SupabaseCliente.getClient().create(SupabaseApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.mapView);
        TextView btnBack = findViewById(R.id.btnBackMap);
        tvMapHeaderTitle = findViewById(R.id.tvMapHeaderTitle);
        btnMiGpsMap = findViewById(R.id.btnMiGpsMap);
        layoutBuscadorMapa = findViewById(R.id.layoutBuscadorMapa);
        etBuscarDireccionMap = findViewById(R.id.etBuscarDireccionMap);
        btnBuscarDireccionMap = findViewById(R.id.btnBuscarDireccionMap);
        scrollSugerenciasMapa = findViewById(R.id.scrollSugerenciasMapa);
        listaSugerenciasMapa = findViewById(R.id.listaSugerenciasMapa);
        layoutConfirmarMapa = findViewById(R.id.layoutConfirmarMapa);
        tvDireccionSeleccionadaMap = findViewById(R.id.tvDireccionSeleccionadaMap);
        btnConfirmarUbicacionMap = findViewById(R.id.btnConfirmarUbicacionMap);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        modoSeleccion = getIntent().getBooleanExtra("MODO_SELECCION", false);

        configurarMapaBase();

        if (modoSeleccion) {
            inicializarModoSeleccion();
        } else {
            cargarEventosEnMapa();
            iniciarUbicacionUsuario(false);
        }
    }

    private void configurarMapaBase() {
        if (mapView == null) return;

        ITileSource osmHotDark = new XYTileSource(
                "OSM_HOT_DARK",
                4,
                18,
                256,
                ".png",
                new String[]{
                        "https://a.tile.openstreetmap.fr/hot/",
                        "https://b.tile.openstreetmap.fr/hot/",
                        "https://c.tile.openstreetmap.fr/hot/"
                }
        );

        mapView.setTileSource(osmHotDark);
        mapView.setUseDataConnection(true);
        mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        mapView.setTilesScaledToDpi(true);
        mapView.setMultiTouchControls(true);
        mapView.setMinZoomLevel(5.0);
        mapView.setMaxZoomLevel(18.0);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        double latRecibida = getIntent().getDoubleExtra("LAT_INICIAL", 0.0);
        double lngRecibida = getIntent().getDoubleExtra("LNG_INICIAL", 0.0);

        if (latRecibida != 0.0 && lngRecibida != 0.0) {
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(new GeoPoint(latRecibida, lngRecibida));
        } else {
            GeoPoint respaldoChillan = new GeoPoint(-36.6066, -72.1033);
            mapView.getController().setZoom(14.5);
            mapView.getController().setCenter(respaldoChillan);
        }
    }

    private void inicializarModoSeleccion() {
        if (tvMapHeaderTitle != null) tvMapHeaderTitle.setText("Elegir Ubicación");
        if (btnMiGpsMap != null) btnMiGpsMap.setVisibility(View.VISIBLE);
        if (layoutBuscadorMapa != null) layoutBuscadorMapa.setVisibility(View.VISIBLE);
        if (layoutConfirmarMapa != null) layoutConfirmarMapa.setVisibility(View.VISIBLE);

        boolean usarGpsAlAbrir = getIntent().getBooleanExtra("USAR_GPS_INICIAL", false);
        double latInicial = getIntent().getDoubleExtra("LAT_INICIAL", -36.6066);
        double lngInicial = getIntent().getDoubleExtra("LNG_INICIAL", -72.1033);
        ciudadSeleccionada = getIntent().getStringExtra("CIUDAD_ACTUAL");
        direccionSeleccionada = getIntent().getStringExtra("DIRECCION_ACTUAL");
        if (ciudadSeleccionada == null) ciudadSeleccionada = "";
        if (direccionSeleccionada == null) direccionSeleccionada = "";

        puntoSeleccionado = new GeoPoint(latInicial, lngInicial);

        mapView.getController().setZoom(15.5);
        mapView.getController().setCenter(puntoSeleccionado);

        pinSeleccion = new Marker(mapView);
        pinSeleccion.setPosition(puntoSeleccionado);
        pinSeleccion.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        pinSeleccion.setTitle("Ubicación del evento");
        pinSeleccion.setDraggable(true);

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                scrollSugerenciasMapa.setVisibility(View.GONE);
                puntoSeleccionado = p;
                pinSeleccion.setPosition(p);
                mapView.invalidate();
                obtenerDireccionDesdeCoordenadas(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return singleTapConfirmedHelper(p);
            }
        };

        pinSeleccion.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint p = marker.getPosition();
                puntoSeleccionado = p;
                obtenerDireccionDesdeCoordenadas(p);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {}
        });

        mapView.getOverlays().clear();
        mapView.getOverlays().add(new MapEventsOverlay(receiver));
        mapView.getOverlays().add(pinSeleccion);
        mapView.invalidate();

        if (!TextUtils.isEmpty(direccionSeleccionada)) {
            etBuscarDireccionMap.setText(direccionSeleccionada);
            tvDireccionSeleccionadaMap.setText("📍 " + direccionSeleccionada);
        } else if (usarGpsAlAbrir) {
            iniciarUbicacionUsuario(true);
        } else {
            obtenerDireccionDesdeCoordenadas(puntoSeleccionado);
        }

        Runnable ejecutarBusqueda = () -> {
            String consulta = etBuscarDireccionMap.getText().toString().trim();
            if (TextUtils.isEmpty(consulta)) {
                Toast.makeText(this, "Escribe una calle, plaza o recinto", Toast.LENGTH_SHORT).show();
                return;
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etBuscarDireccionMap.getWindowToken(), 0);
            }

            buscarLugarEnMapa(consulta);
        };

        btnBuscarDireccionMap.setOnClickListener(v -> ejecutarBusqueda.run());
        etBuscarDireccionMap.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                ejecutarBusqueda.run();
                return true;
            }
            return false;
        });

        btnMiGpsMap.setOnClickListener(v -> {
            scrollSugerenciasMapa.setVisibility(View.GONE);
            iniciarUbicacionUsuario(true);
        });

        btnConfirmarUbicacionMap.setOnClickListener(v -> {
            if (puntoSeleccionado == null) return;

            Intent data = new Intent();
            data.putExtra("LATITUD", puntoSeleccionado.getLatitude());
            data.putExtra("LONGITUD", puntoSeleccionado.getLongitude());
            data.putExtra("DIRECCION", direccionSeleccionada);
            data.putExtra("CIUDAD", ciudadSeleccionada);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void iniciarUbicacionUsuario(boolean mostrarAvisoError) {
        boolean permisoFino = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean permisoAprox = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (permisoFino || permisoAprox) {
            centrarEnUbicacionActual(mostrarAvisoError);
        } else {
            requestLocationLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void centrarEnUbicacionActual(boolean mostrarAvisoError) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                aplicarCoordenadasGps(new GeoPoint(location.getLatitude(), location.getLongitude()));
            } else {
                // Si no hay ubicación guardada en caché, solicitar lectura GPS actual
                CancellationTokenSource cts = new CancellationTokenSource();
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                        .addOnSuccessListener(this, freshLocation -> {
                            if (freshLocation != null) {
                                aplicarCoordenadasGps(new GeoPoint(freshLocation.getLatitude(), freshLocation.getLongitude()));
                            } else if (mostrarAvisoError) {
                                Toast.makeText(this, "Activa el GPS del teléfono para detectar tu ubicación", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void aplicarCoordenadasGps(GeoPoint miPunto) {
        if (mapView == null) return;
        mapView.getController().setZoom(15.5);
        mapView.getController().setCenter(miPunto);

        if (modoSeleccion && pinSeleccion != null) {
            puntoSeleccionado = miPunto;
            pinSeleccion.setPosition(miPunto);
            obtenerDireccionDesdeCoordenadas(miPunto);
        }
        mapView.invalidate();
    }

    private void buscarLugarEnMapa(String consulta) {
        tvDireccionSeleccionadaMap.setText("Buscando \"" + consulta + "\"...");

        executorMapa.execute(() -> {
            String consultaLimpia = consulta.trim();
            boolean esNombreDeCiudad = false;

            String[] ciudadesConocidas = {
                    "chillán", "chillan", "chillán viejo", "chillan viejo",
                    "san carlos", "bulnes", "quillón", "quillon",
                    "san nicolás", "san nicolas", "concepción", "concepcion",
                    "los ángeles", "los angeles", "santiago", "talcahuano", "valparaíso", "viña del mar"
            };

            String consultaLower = consultaLimpia.toLowerCase(Locale.ROOT);
            for (String c : ciudadesConocidas) {
                if (consultaLower.equals(c)) {
                    esNombreDeCiudad = true;
                    break;
                }
            }

            List<ResultadoLugar> resultados = new ArrayList<>();

            if (esNombreDeCiudad) {
                resultados = consultarNominatim(consultaLimpia + ", Chile", true);
            } else {
                if (!TextUtils.isEmpty(ciudadSeleccionada)
                        && !consultaLower.contains(ciudadSeleccionada.toLowerCase(Locale.ROOT))) {
                    resultados = consultarNominatim(consultaLimpia + ", " + ciudadSeleccionada + ", Chile", false);
                }

                if (resultados.isEmpty()) {
                    resultados = consultarNominatim(consultaLimpia + ", Chile", false);
                }
            }

            final List<ResultadoLugar> finales = resultados;
            runOnUiThread(() -> {
                if (finales.isEmpty()) {
                    scrollSugerenciasMapa.setVisibility(View.GONE);
                    tvDireccionSeleccionadaMap.setText("Sin resultados. Toca el mapa para marcar el punto.");
                    Toast.makeText(this, "No se encontró esa dirección", Toast.LENGTH_SHORT).show();
                    return;
                }

                aplicarResultadoEnMapa(finales.get(0));

                if (finales.size() > 1) {
                    listaSugerenciasMapa.removeAllViews();
                    int pad = (int) (11 * getResources().getDisplayMetrics().density);

                    for (ResultadoLugar item : finales) {
                        TextView tvItem = new TextView(this);
                        tvItem.setText("📍 " + item.nombreCorto);
                        tvItem.setTextColor(Color.WHITE);
                        tvItem.setTextSize(13f);
                        tvItem.setPadding(pad, pad, pad, pad);
                        tvItem.setOnClickListener(v -> {
                            aplicarResultadoEnMapa(item);
                            scrollSugerenciasMapa.setVisibility(View.GONE);
                        });

                        listaSugerenciasMapa.addView(tvItem);

                        View divider = new View(this);
                        divider.setBackgroundColor(Color.parseColor("#334155"));
                        listaSugerenciasMapa.addView(divider, new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    }
                    scrollSugerenciasMapa.setVisibility(View.VISIBLE);
                } else {
                    scrollSugerenciasMapa.setVisibility(View.GONE);
                }
            });
        });
    }

    private List<ResultadoLugar> consultarNominatim(String queryCompleta, boolean priorizarCiudad) {
        List<ResultadoLugar> lista = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(queryCompleta, StandardCharsets.UTF_8.name());
            String featureParam = priorizarCiudad ? "&featureType=city" : "";
            String url = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&countrycodes=cl&limit=6"
                    + featureParam + "&q=" + encoded;

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "NexuraAppAndroid/1.0")
                    .header("Accept-Language", "es-CL,es;q=0.9")
                    .build();

            try (okhttp3.Response response = httpClientBusqueda.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonStr = response.body().string();
                    JSONArray array = new JSONArray(jsonStr);

                    if (array.length() == 0 && priorizarCiudad) {
                        return consultarNominatim(queryCompleta, false);
                    }

                    List<JSONObject> ordenados = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        ordenados.add(array.getJSONObject(i));
                    }
                    ordenados.sort((a, b) -> Double.compare(
                            b.optDouble("importance", 0.0),
                            a.optDouble("importance", 0.0)
                    ));

                    for (JSONObject obj : ordenados) {
                        double lat = obj.getDouble("lat");
                        double lon = obj.getDouble("lon");
                        String displayName = obj.optString("display_name", "");
                        String tipo = obj.optString("type", "");

                        JSONObject addr = obj.optJSONObject("address");
                        String nombreLugar = "";
                        String ciudad = "";

                        if (addr != null) {
                            String poi = addr.optString("amenity",
                                    addr.optString("leisure",
                                            addr.optString("building",
                                                    addr.optString("stadium", ""))));
                            String road = addr.optString("road", "");
                            String houseNumber = addr.optString("house_number", "");
                            ciudad = addr.optString("city",
                                    addr.optString("town",
                                            addr.optString("village",
                                                    addr.optString("municipality", ""))));
                            String region = addr.optString("state", "");

                            if (tipo.equals("city") || tipo.equals("town") || tipo.equals("administrative") || tipo.equals("municipality")) {
                                String nombreCiudad = !TextUtils.isEmpty(ciudad) ? ciudad : obj.optString("name", "");
                                nombreLugar = !TextUtils.isEmpty(region) ? nombreCiudad + ", " + region : nombreCiudad;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                if (!TextUtils.isEmpty(poi)) sb.append(poi);
                                if (!TextUtils.isEmpty(road)) {
                                    if (sb.length() > 0) sb.append(", ");
                                    sb.append(road);
                                    if (!TextUtils.isEmpty(houseNumber)) {
                                        sb.append(" ").append(houseNumber);
                                    }
                                }
                                if (!TextUtils.isEmpty(ciudad)) {
                                    if (sb.length() > 0) sb.append(", ");
                                    sb.append(ciudad);
                                }
                                nombreLugar = sb.toString();
                            }
                        }

                        if (TextUtils.isEmpty(nombreLugar)) {
                            String[] partes = displayName.split(",");
                            nombreLugar = partes.length >= 2
                                    ? partes[0].trim() + ", " + partes[1].trim()
                                    : displayName;
                        }

                        lista.add(new ResultadoLugar(nombreLugar, ciudad, lat, lon));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MAPA_BUSQUEDA", "Error buscando dirección: " + e.getMessage());
        }
        return lista;
    }

    private void aplicarResultadoEnMapa(ResultadoLugar lugar) {
        GeoPoint punto = new GeoPoint(lugar.lat, lugar.lon);
        puntoSeleccionado = punto;
        direccionSeleccionada = lugar.nombreCorto;
        if (!TextUtils.isEmpty(lugar.ciudad)) {
            ciudadSeleccionada = lugar.ciudad;
        }

        pinSeleccion.setPosition(punto);

        boolean esCiudadGeneral = lugar.nombreCorto.contains("Región") || lugar.nombreCorto.equalsIgnoreCase(lugar.ciudad);
        mapView.getController().setZoom(esCiudadGeneral ? 13.5 : 16.0);
        mapView.getController().setCenter(punto);
        mapView.invalidate();

        tvDireccionSeleccionadaMap.setText("📍 " + lugar.nombreCorto);
    }

    private void obtenerDireccionDesdeCoordenadas(GeoPoint punto) {
        tvDireccionSeleccionadaMap.setText("Obteniendo dirección...");
        Geocoder geocoder = new Geocoder(this, new Locale("es", "CL"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(punto.getLatitude(), punto.getLongitude(), 1, addresses -> {
                String texto = formatearDireccion(addresses, punto);
                runOnUiThread(() -> tvDireccionSeleccionadaMap.setText("📍 " + texto));
            });
        } else {
            executorMapa.execute(() -> {
                try {
                    List<Address> addresses = geocoder.getFromLocation(punto.getLatitude(), punto.getLongitude(), 1);
                    String texto = formatearDireccion(addresses, punto);
                    runOnUiThread(() -> tvDireccionSeleccionadaMap.setText("📍 " + texto));
                } catch (Exception e) {
                    String coords = String.format(Locale.US, "%.5f, %.5f", punto.getLatitude(), punto.getLongitude());
                    direccionSeleccionada = coords;
                    runOnUiThread(() -> tvDireccionSeleccionadaMap.setText("📍 " + coords));
                }
            });
        }
    }

    private String formatearDireccion(List<Address> addresses, GeoPoint punto) {
        if (addresses != null && !addresses.isEmpty()) {
            Address dir = addresses.get(0);
            String calle = dir.getThoroughfare();
            String numero = dir.getSubThoroughfare();
            String localidad = dir.getLocality() != null ? dir.getLocality() : dir.getSubAdminArea();

            if (!TextUtils.isEmpty(localidad)) {
                ciudadSeleccionada = localidad;
            }

            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(calle)) {
                sb.append(calle);
                if (!TextUtils.isEmpty(numero)) {
                    sb.append(" ").append(numero);
                }
            } else if (!TextUtils.isEmpty(dir.getFeatureName())) {
                sb.append(dir.getFeatureName());
            }

            if (!TextUtils.isEmpty(localidad)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(localidad);
            }

            if (sb.length() > 0) {
                direccionSeleccionada = sb.toString();
                return direccionSeleccionada;
            }
        }

        direccionSeleccionada = String.format(Locale.US, "%.5f, %.5f", punto.getLatitude(), punto.getLongitude());
        return direccionSeleccionada;
    }

    private void cargarEventosEnMapa() {
        api.obtenerEventos().enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                if (response.isSuccessful() && response.body() != null && mapView != null) {
                    mapView.getOverlays().clear();

                    for (Evento evento : response.body()) {
                        double lat = evento.getLatitud();
                        double lon = evento.getLongitud();

                        if (lat != 0 && lon != 0 && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
                            GeoPoint pos = new GeoPoint(lat, lon);

                            Marker marker = new Marker(mapView);
                            marker.setPosition(pos);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle(evento.getTitulo());
                            marker.setSnippet(evento.getUbicacion() + " | +" + evento.getXpRecompensa() + " XP");

                            marker.setOnMarkerClickListener((m, mapView1) -> {
                                m.showInfoWindow();
                                Intent intent = new Intent(MapaInteractivoActivity.this, Detalle_Evento.class);
                                intent.putExtra("EVENTO_SELECCIONADO", evento);
                                startActivity(intent);
                                return true;
                            });

                            mapView.getOverlays().add(marker);
                        }
                    }

                    mapView.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<Evento>> call, Throwable t) {
                Toast.makeText(MapaInteractivoActivity.this, "Error al cargar pines en el mapa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorMapa.shutdownNow();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}