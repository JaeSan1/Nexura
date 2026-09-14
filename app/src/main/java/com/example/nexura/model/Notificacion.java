package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Notificacion implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("evento_id")
    private String eventoId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("tiempo")
    private String tiempo;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("leida")
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String id, String eventoId, String titulo, String mensaje, String tiempo, String tipo, boolean leida) {
        this.id = id;
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tiempo = tiempo;
        this.tipo = tipo;
        this.leida = leida;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getTiempo() { return tiempo; }
    public void setTiempo(String tiempo) { this.tiempo = tiempo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}