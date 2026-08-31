package com.example.nexura.model;

import java.io.Serializable;

public class Evento implements Serializable {
    private String id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String fecha;
    private String ubicacion;
    private String organizadorNombre;
    private String portadaUrl;
    private int xpRecompensa;
    private double distanciaKm;

    public Evento() {}

    public Evento(String id, String titulo, String descripcion, String categoria, String fecha,
                  String ubicacion, String organizadorNombre, String portadaUrl, int xpRecompensa, double distanciaKm) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fecha = fecha;
        this.ubicacion = ubicacion;
        this.organizadorNombre = organizadorNombre;
        this.portadaUrl = portadaUrl;
        this.xpRecompensa = xpRecompensa;
        this.distanciaKm = distanciaKm;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getOrganizadorNombre() { return organizadorNombre; }
    public void setOrganizadorNombre(String organizadorNombre) { this.organizadorNombre = organizadorNombre; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }

    public int getXpRecompensa() { return xpRecompensa; }
    public void setXpRecompensa(int xpRecompensa) { this.xpRecompensa = xpRecompensa; }

    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
}