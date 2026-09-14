package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Evento implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("ubicacion")
    private String ubicacion;

    @SerializedName("ciudad")
    private String ciudad;

    @SerializedName("organizador_nombre")
    private String organizador;

    @SerializedName("portada_url")
    private String imagenUrl;

    @SerializedName("xp_recompensa")
    private int xpRecompensa;

    @SerializedName("contador_likes")
    private int contadorLikes;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    // Constructor vacío necesario para Retrofit/Gson
    public Evento() {}

    // Getters y Setters...
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

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getOrganizador() { return organizador; }
    public void setOrganizador(String organizador) { this.organizador = organizador; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public int getXpRecompensa() { return xpRecompensa; }
    public void setXpRecompensa(int xpRecompensa) { this.xpRecompensa = xpRecompensa; }

    public int getContadorLikes() { return contadorLikes; }
    public void setContadorLikes(int contadorLikes) { this.contadorLikes = contadorLikes; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}