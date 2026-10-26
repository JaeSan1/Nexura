package com.example.nexura.util;

import android.content.Context;
import com.google.android.gms.safetynet.SafetyNet;

public class RecaptchaHelper {

    // Reemplaza esto con tu Site Key real de Google Cloud
    private static final String RECAPTCHA_SITE_KEY = "TU_SITE_KEY_AQUI";

    public interface RecaptchaCallback {
        void onSuccess(String token);
        void onFailure(String error);
    }

    public static void ejecutarVerificacion(Context context, RecaptchaCallback callback) {
        SafetyNet.getClient(context).verifyWithRecaptcha(RECAPTCHA_SITE_KEY)
                .addOnSuccessListener(response -> {
                    String token = response.getTokenResult();
                    if (token != null && !token.isEmpty()) {
                        callback.onSuccess(token);
                    } else {
                        callback.onFailure("El token recibido está vacío.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error al ejecutar CAPTCHA: " + e.getMessage()));
    }
}