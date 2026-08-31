package com.example.nexura.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nexura.R;
import com.example.nexura.adapter.NotificacionAdapter;
import com.example.nexura.model.Notificacion;
import java.util.ArrayList;
import java.util.List;

public class Notificaciones extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        TextView btnBack = findViewById(R.id.btnBackNotif);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvNotificaciones = findViewById(R.id.rvNotificaciones);
        if (rvNotificaciones != null) {
            rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));

            List<Notificacion> listaNotif = new ArrayList<>();
            listaNotif.add(new Notificacion("1", "Aviso urgente de organizador", "Fiesta de la Longaniza: Se cerró el acceso por Av. Libertad.", "Hace 15 min", "URGENTE", false));
            listaNotif.add(new Notificacion("2", "¡Recompensa Desbloqueada!", "Has recibido +350 XP por tu permanencia en la Feria del Libro.", "Ayer", "XP", true));
            listaNotif.add(new Notificacion("3", "Recordatorio de Asistencia", "Neon Beats Festival comienza en 2 días. Prepara tu llegada para validar tu GPS.", "Hace 2 días", "RECORDATORIO", true));

            NotificacionAdapter adapter = new NotificacionAdapter(this, listaNotif, notif -> {
                Toast.makeText(this, notif.getTitulo(), Toast.LENGTH_SHORT).show();
            });

            rvNotificaciones.setAdapter(adapter);
        }
    }
}
