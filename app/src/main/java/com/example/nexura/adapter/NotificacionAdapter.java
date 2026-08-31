package com.example.nexura.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nexura.R;
import com.example.nexura.model.Notificacion;
import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {

    public interface OnNotifClickListener {
        void onNotifClick(Notificacion notificacion);
    }

    private final Context context;
    private final List<Notificacion> listaNotificaciones;
    private final OnNotifClickListener listener;

    public NotificacionAdapter(Context context, List<Notificacion> listaNotificaciones, OnNotifClickListener listener) {
        this.context = context;
        this.listaNotificaciones = listaNotificaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacion, parent, false);
        return new NotificacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificacion notif = listaNotificaciones.get(position);

        holder.tvTitulo.setText(notif.getTitulo());
        holder.tvMensaje.setText(notif.getMensaje());
        holder.tvTiempo.setText(notif.getTiempo());

        switch (notif.getTipo()) {
            case "URGENTE":
                holder.tvIcono.setText("📢");
                break;
            case "XP":
                holder.tvIcono.setText("⚡");
                break;
            default:
                holder.tvIcono.setText("📅");
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onNotifClick(notif));
    }

    @Override
    public int getItemCount() {
        return listaNotificaciones.size();
    }

    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcono, tvTitulo, tvMensaje, tvTiempo;

        public NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcono = itemView.findViewById(R.id.tvNotifIcon);
            tvTitulo = itemView.findViewById(R.id.tvNotifTitle);
            tvMensaje = itemView.findViewById(R.id.tvNotifMessage);
            tvTiempo = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}