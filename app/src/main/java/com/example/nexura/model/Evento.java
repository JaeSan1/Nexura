package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Evento implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("organizador_id")
    private String organizadorId;

    @SerializedName("organizador_nombre")
    private String organizadorNombre;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("ciudad")
    private String ciudad;

    @SerializedName("ubicacion")
    private String ubicacion;

    @SerializedName("portada_url")
    private String imagenUrl;

    @SerializedName("xp_recompensa")
    private int xpRecompensa;

    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("contador_likes")
    private int contadorLikes;

    @SerializedName("contador_asistentes")
    private int contadorAsistentes;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("creador_verificado")
    private boolean creadorVerificado;

    @SerializedName("perfiles")
    private PerfilOrganizador perfilOrganizador;

    public static class PerfilOrganizador implements Serializable {
        @SerializedName("foto_perfil_url")
        private String fotoPerfilUrl;

        @SerializedName("gamertag")
        private String gamertag;

        public String getFotoPerfilUrl() { return fotoPerfilUrl; }
        public String getGamertag() { return gamertag; }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(String organizadorId) { this.organizadorId = organizadorId; }

    public String getOrganizadorNombre() {
        if (perfilOrganizador != null && perfilOrganizador.getGamertag() != null && !perfilOrganizador.getGamertag().isEmpty()) {
            return perfilOrganizador.getGamertag();
        }
        return organizadorNombre;
    }
    public void setOrganizadorNombre(String organizadorNombre) { this.organizadorNombre = organizadorNombre; }

    public String getOrganizadorAvatarUrl() {
        return perfilOrganizador != null ? perfilOrganizador.getFotoPerfilUrl() : null;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getPortadaUrl() { return imagenUrl; }
    public void setPortadaUrl(String portadaUrl) { this.imagenUrl = portadaUrl; }

    public int getXpRecompensa() { return xpRecompensa; }
    public void setXpRecompensa(int xpRecompensa) { this.xpRecompensa = xpRecompensa; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public int getContadorLikes() { return contadorLikes; }
    public void setContadorLikes(int contadorLikes) { this.contadorLikes = contadorLikes; }

    public int getContadorAsistentes() { return contadorAsistentes; }
    public void setContadorAsistentes(int contadorAsistentes) { this.contadorAsistentes = contadorAsistentes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isCreadorVerificado() { return creadorVerificado; }
    public void setCreadorVerificado(boolean creadorVerificado) { this.creadorVerificado = creadorVerificado; }
}