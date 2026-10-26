package com.example.nexura.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.model.Comentario;

import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private final List<Comentario> listaComentarios;

    public ComentarioAdapter(List<Comentario> listaComentarios) {
        this.listaComentarios = listaComentarios;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);
        if (comentario != null) {
            String usuario = comentario.getGamertag() != null ? comentario.getGamertag() : "Gamer";
            String texto = comentario.getContenido() != null ? comentario.getContenido() : "";

            holder.tvUser.setText(usuario);
            holder.tvText.setText(texto);
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios != null ? listaComentarios.size() : 0;
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvText = itemView.findViewById(R.id.tvCommentText);
        }
    }
}