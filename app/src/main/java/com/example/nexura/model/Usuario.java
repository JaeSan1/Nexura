package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Usuario implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("gamertag")
    private String gamertag;

    @SerializedName("correo")
    private String correo;

    @SerializedName("biografia")
    private String biografia;

    @SerializedName("ciudad")
    private String ciudad;

    @SerializedName("foto_perfil_url")
    private String fotoPerfilUrl;

    @SerializedName("titulo_equipado")
    private String tituloEquipado;

    @SerializedName("nivel")
    private int nivel;

    @SerializedName("xp_actual")
    private int xpActual;

    @SerializedName("xp_meta")
    private int xpMeta;

    @SerializedName("seguidores")
    private int seguidores;

    @SerializedName("seguidos")
    private int seguidos;

    @SerializedName("reputacion_likes")
    private int reputacionLikes;

    @SerializedName("es_organizador")
    private boolean esOrganizador;

    public Usuario() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGamertag() { return gamertag; }
    public void setGamertag(String gamertag) { this.gamertag = gamertag; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }

    public String getTituloEquipado() { return tituloEquipado; }
    public void setTituloEquipado(String tituloEquipado) { this.tituloEquipado = tituloEquipado; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public int getXpActual() { return xpActual; }
    public void setXpActual(int xpActual) { this.xpActual = xpActual; }

    public int getXpMeta() { return xpMeta; }
    public void setXpMeta(int xpMeta) { this.xpMeta = xpMeta; }

    public int getSeguidores() { return seguidores; }
    public void setSeguidores(int seguidores) { this.seguidores = seguidores; }

    public int getSeguidos() { return seguidos; }
    public void setSeguidos(int seguidos) { this.seguidos = seguidos; }

    public int getReputacionLikes() { return reputacionLikes; }
    public void setReputacionLikes(int reputacionLikes) { this.reputacionLikes = reputacionLikes; }

    public boolean isEsOrganizador() { return esOrganizador; }
    public void setEsOrganizador(boolean esOrganizador) { this.esOrganizador = esOrganizador; }
}