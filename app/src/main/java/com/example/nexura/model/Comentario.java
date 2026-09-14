package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Comentario implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("evento_id")
    private String eventoId;

    @SerializedName("usuario_id")
    private String usuarioId;

    @SerializedName("gamertag")
    private String gamertag;

    @SerializedName("contenido")
    private String contenido;

    @SerializedName("created_at")
    private String createdAt;

    public Comentario() {}

    public Comentario(String eventoId, String usuarioId, String gamertag, String contenido) {
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.gamertag = gamertag;
        this.contenido = contenido;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getGamertag() { return gamertag; }
    public void setGamertag(String gamertag) { this.gamertag = gamertag; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}