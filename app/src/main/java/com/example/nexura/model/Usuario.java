package com.example.nexura.model;

public class Usuario {
    private String id;
    private String username;
    private String email;
    private String biografia;
    private String ciudad;
    private String avatarUrl;
    private String tituloEquipado;
    private int nivel;
    private int xpActual;
    private int xpMeta;
    private boolean esOrganizador;

    public Usuario() {}

    public Usuario(String id, String username, String email, String biografia, String ciudad,
                   String avatarUrl, String tituloEquipado, int nivel, int xpActual, int xpMeta, boolean esOrganizador) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.biografia = biografia;
        this.ciudad = ciudad;
        this.avatarUrl = avatarUrl;
        this.tituloEquipado = tituloEquipado;
        this.nivel = nivel;
        this.xpActual = xpActual;
        this.xpMeta = xpMeta;
        this.esOrganizador = esOrganizador;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTituloEquipado() { return tituloEquipado; }
    public void setTituloEquipado(String tituloEquipado) { this.tituloEquipado = tituloEquipado; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public int getXpActual() { return xpActual; }
    public void setXpActual(int xpActual) { this.xpActual = xpActual; }

    public int getXpMeta() { return xpMeta; }
    public void setXpMeta(int xpMeta) { this.xpMeta = xpMeta; }

    public boolean isEsOrganizador() { return esOrganizador; }
    public void setEsOrganizador(boolean esOrganizador) { this.esOrganizador = esOrganizador; }
}