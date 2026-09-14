package com.example.nexura.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseCliente {

    // Reemplaza con tus valores de Supabase (Settings -> API)
    private static final String BASE_URL = "https://wfkzbqhsacitksuwqlei.supabase.co/rest/v1/";
    private static final String API_KEY = "PEGA_AQUI_TU_ANON_PUBLISHABLE_KEY";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor para inyectar cabeceras requeridas por Supabase en cada petición
            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("apikey", API_KEY)
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}