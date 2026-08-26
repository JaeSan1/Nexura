package com.example.nexura.model;

import java.io.Serializable;

public class Evento implements Serializable {
    private String id;
    private String titulo;
    private String fechaHora;
    private String ubicacionNombre;
    private double latitud;
    private double longitud;
    private double radioPermitidoMetros;
    private int xpRecompensa;
    private String aviso;
    private boolean asistenciaReclamada;

    // Constructor
    public Evento(String id, String titulo, String fechaHora, String ubicacionNombre,
                  double latitud, double longitud, double radioPermitidoMetros,
                  int xpRecompensa, String aviso) {
        this.id = id;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.ubicacionNombre = ubicacionNombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.radioPermitidoMetros = radioPermitidoMetros;
        this.xpRecompensa = xpRecompensa;
        this.aviso = aviso;
        this.asistenciaReclamada = false;
    }

    // Getters y Setters

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getFechaHora() { return fechaHora; }
    public String getUbicacionNombre() { return ubicacionNombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public double getRadioPermitidoMetros() { return radioPermitidoMetros; }
    public int getXpRecompensa() { return xpRecompensa; }
    public String getAviso() { return aviso; }
    public void setAviso(String aviso) { this.aviso = aviso; }
    public boolean isAsistenciaReclamada() { return asistenciaReclamada; }
    public void setAsistenciaReclamada(boolean asistenciaReclamada) { this.asistenciaReclamada = asistenciaReclamada; }
}
