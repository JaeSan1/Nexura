package com.example.nexura;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        TextView btnBack = findViewById(R.id.btnBackPerfil);
        btnBack.setOnClickListener(v -> finish());
    }
}