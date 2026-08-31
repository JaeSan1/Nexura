package com.example.nexura.model;

public class Notificacion {
    private String id;
    private String titulo;
    private String mensaje;
    private String tiempo;
    private String tipo; // "URGENTE", "XP", "RECORDATORIO"
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String id, String titulo, String mensaje, String tiempo, String tipo, boolean leida) {
        this.id = id;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tiempo = tiempo;
        this.tipo = tipo;
        this.leida = leida;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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