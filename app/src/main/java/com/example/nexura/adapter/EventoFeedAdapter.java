package com.example.nexura.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nexura.R;
import com.example.nexura.model.Evento;
import java.util.List;

public class EventoFeedAdapter extends RecyclerView.Adapter<EventoFeedAdapter.EventoViewHolder> {

    public interface OnEventoClickListener {
        void onEventoClick(Evento evento);
    }

    private final Context context;
    private final List<Evento> listaEventos;
    private final OnEventoClickListener listener;

    public EventoFeedAdapter(Context context, List<Evento> listaEventos, OnEventoClickListener listener) {
        this.context = context;
        this.listaEventos = listaEventos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_feed, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = listaEventos.get(position);

        holder.tvOrganizador.setText(evento.getOrganizadorNombre());
        holder.tvUbicacionDistancia.setText(evento.getUbicacion() + " • A " + evento.getDistanciaKm() + " km");
        holder.tvXpBadge.setText("+" + evento.getXpRecompensa() + " XP");
        holder.tvFechaBadge.setText("📅 " + evento.getFecha());
        holder.tvTitulo.setText(evento.getTitulo());
        holder.tvDescripcion.setText(evento.getDescripcion());

        holder.itemView.setOnClickListener(v -> listener.onEventoClick(evento));
    }

    @Override
    public int getItemCount() {
        return listaEventos.size();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrganizador, tvUbicacionDistancia, tvXpBadge, tvFechaBadge, tvTitulo, tvDescripcion;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrganizador = itemView.findViewById(R.id.tvFeedOrganizer);
            tvUbicacionDistancia = itemView.findViewById(R.id.tvFeedLocationSub);
            tvXpBadge = itemView.findViewById(R.id.tvFeedXpBadge);
            tvFechaBadge = itemView.findViewById(R.id.tvFeedDateBadge);
            tvTitulo = itemView.findViewById(R.id.tvFeedTitle);
            tvDescripcion = itemView.findViewById(R.id.tvFeedDescription);
        }
    }
}