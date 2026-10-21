package com.example.nexura.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        holder.tvOrganizador.setText(evento.getOrganizador() != null ? evento.getOrganizador() : "Organizador");
        holder.tvUbicacionDistancia.setText(evento.getUbicacion() + " • " + evento.getCiudad());
        holder.tvXpBadge.setText("+" + evento.getXpRecompensa() + " XP");
        holder.tvFechaBadge.setText(evento.getFecha() != null ? evento.getFecha() : "Próximamente");
        holder.tvTitulo.setText(evento.getTitulo());
        holder.tvDescripcion.setText(evento.getDescripcion());

        // Manejo del badge de creador verificado
        if (holder.tvVerifiedBadge != null) {
            if (evento.isCreadorVerificado()) {
                holder.tvVerifiedBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvVerifiedBadge.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onEventoClick(evento));
    }

    @Override
    public int getItemCount() {
        return listaEventos != null ? listaEventos.size() : 0;
    }

    // Método requerido para actualizar la lista dinámicamente con LiveData
    public void actualizarLista(List<Evento> nuevaLista) {
        if (nuevaLista != null) {
            this.listaEventos.clear();
            this.listaEventos.addAll(nuevaLista);
            notifyDataSetChanged();
        }
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrganizador, tvVerifiedBadge, tvUbicacionDistancia, tvXpBadge, tvFechaBadge, tvTitulo, tvDescripcion;
        ImageView ivEventImage;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrganizador = itemView.findViewById(R.id.tvFeedOrganizer);
            tvVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            tvUbicacionDistancia = itemView.findViewById(R.id.tvFeedLocationSub);
            tvXpBadge = itemView.findViewById(R.id.tvFeedXpBadge);
            tvFechaBadge = itemView.findViewById(R.id.tvFeedDateBadge);
            tvTitulo = itemView.findViewById(R.id.tvFeedTitle);
            tvDescripcion = itemView.findViewById(R.id.tvFeedDescription);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
        }
    }
}