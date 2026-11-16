package com.example.nexura.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nexura.R;
import com.example.nexura.model.Evento;
import com.example.nexura.ui.Perfil;

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

        holder.tvOrganizador.setText(evento.getOrganizadorNombre() != null ? evento.getOrganizadorNombre() : "Organizador");
        holder.tvUbicacionDistancia.setText(evento.getUbicacion() + " • " + evento.getCiudad());
        holder.tvXpBadge.setText("+" + evento.getXpRecompensa() + " XP");
        holder.tvFechaBadge.setText(evento.getFecha() != null ? evento.getFecha() : "Próximamente");
        holder.tvTitulo.setText(evento.getTitulo());
        holder.tvDescripcion.setText(evento.getDescripcion());

        // Cargar avatar circular del organizador
        if (holder.ivOrganizerAvatar != null) {
            String avatarUrl = evento.getOrganizadorAvatarUrl();
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_logo_nexura)
                        .error(R.drawable.ic_logo_nexura)
                        .circleCrop()
                        .into(holder.ivOrganizerAvatar);
            } else {
                holder.ivOrganizerAvatar.setImageResource(R.drawable.ic_logo_nexura);
            }
        }

        // Abrir perfil del organizador al tocar su foto o gamertag
        View.OnClickListener abrirPerfilOrganizador = v -> {
            if (!TextUtils.isEmpty(evento.getOrganizadorId())) {
                Intent intent = new Intent(context, Perfil.class);
                intent.putExtra("USER_ID_PERFIL", evento.getOrganizadorId());
                context.startActivity(intent);
            }
        };

        if (holder.ivOrganizerAvatar != null) {
            holder.ivOrganizerAvatar.setOnClickListener(abrirPerfilOrganizador);
        }
        if (holder.tvOrganizador != null) {
            holder.tvOrganizador.setOnClickListener(abrirPerfilOrganizador);
        }

        // Cargar portada del evento
        if (holder.ivEventImage != null) {
            if (!TextUtils.isEmpty(evento.getPortadaUrl())) {
                Glide.with(context)
                        .load(evento.getPortadaUrl())
                        .placeholder(R.drawable.ic_logo_nexura)
                        .error(R.drawable.ic_logo_nexura)
                        .centerCrop()
                        .into(holder.ivEventImage);
            } else {
                holder.ivEventImage.setImageResource(R.drawable.ic_logo_nexura);
            }
        }

        if (holder.tvVerifiedBadge != null) {
            holder.tvVerifiedBadge.setVisibility(evento.isCreadorVerificado() ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onEventoClick(evento));
    }

    @Override
    public int getItemCount() {
        return listaEventos != null ? listaEventos.size() : 0;
    }

    public void actualizarLista(List<Evento> nuevaLista) {
        if (nuevaLista != null) {
            this.listaEventos.clear();
            this.listaEventos.addAll(nuevaLista);
            notifyDataSetChanged();
        }
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrganizador, tvVerifiedBadge, tvUbicacionDistancia, tvXpBadge, tvFechaBadge, tvTitulo, tvDescripcion;
        ImageView ivEventImage, ivOrganizerAvatar;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrganizerAvatar = itemView.findViewById(R.id.ivFeedOrganizerAvatar);
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