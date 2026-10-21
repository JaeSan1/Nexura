package com.example.nexura.network;

import com.example.nexura.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseCliente {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor para inyectar headers de autenticación públicos de Supabase
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                                .header("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                                .header("Content-Type", "application/json");
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    // Interceptor de Logs (útil para ver peticiones en Logcat durante desarrollo)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            // Construcción del cliente Retrofit usando la URL expuesta por BuildConfig
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL + "/rest/v1/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}