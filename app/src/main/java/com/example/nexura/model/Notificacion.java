package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Notificacion implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("usuario_destino_id")
    private String usuarioDestinoId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("created_at")
    private String createdAt;

    public Notificacion() {}

    public Notificacion(String usuarioDestinoId, String titulo, String mensaje, String tipo) {
        this.usuarioDestinoId = usuarioDestinoId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioDestinoId() { return usuarioDestinoId; }
    public void setUsuarioDestinoId(String usuarioDestinoId) { this.usuarioDestinoId = usuarioDestinoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}