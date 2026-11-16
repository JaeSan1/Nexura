package com.example.nexura.ui.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nexura.R;
import com.example.nexura.model.Comentario;

import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    public interface OnComentarioDeleteListener {
        void onEliminarComentario(Comentario comentario, int position);
    }

    private final List<Comentario> listaComentarios;
    private final String currentUserId;
    private final OnComentarioDeleteListener deleteListener;

    public ComentarioAdapter(List<Comentario> listaComentarios, String currentUserId, OnComentarioDeleteListener deleteListener) {
        this.listaComentarios = listaComentarios;
        this.currentUserId = currentUserId;
        this.deleteListener = deleteListener;
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
        if (comentario == null) return;

        String usuario = comentario.getGamertag() != null ? comentario.getGamertag() : "Gamer";
        String texto = comentario.getContenido() != null ? comentario.getContenido() : "";

        holder.tvUser.setText(usuario);
        holder.tvText.setText(texto);

        boolean esMio = !TextUtils.isEmpty(currentUserId)
                && currentUserId.equals(comentario.getUsuarioId());

        if (holder.btnEliminar != null) {
            if (esMio && deleteListener != null) {
                holder.btnEliminar.setVisibility(View.VISIBLE);
                holder.btnEliminar.setOnClickListener(v -> {
                    int posActual = holder.getAdapterPosition();
                    if (posActual != RecyclerView.NO_POSITION) {
                        deleteListener.onEliminarComentario(comentario, posActual);
                    }
                });
            } else {
                holder.btnEliminar.setVisibility(View.GONE);
                holder.btnEliminar.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios != null ? listaComentarios.size() : 0;
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText, btnEliminar;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvText = itemView.findViewById(R.id.tvCommentText);

            // Crear botón de eliminar dinámicamente si no existe en item_comentario.xml
            if (tvUser != null && tvUser.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) tvUser.getParent();
                btnEliminar = new TextView(itemView.getContext());
                btnEliminar.setText("Eliminar");
                btnEliminar.setTextColor(Color.parseColor("#FF5252"));
                btnEliminar.setTextSize(11f);
                btnEliminar.setPadding(16, 4, 8, 4);
                btnEliminar.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.gravity = Gravity.END;
                btnEliminar.setLayoutParams(params);
                parent.addView(btnEliminar);
            }
        }
    }
}