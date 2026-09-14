package com.example.nexura.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Logro implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("codigo")
    private String codigo;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("icono_emoji")
    private String iconoEmoji;

    @SerializedName("xp_bonus")
    private int xpBonus;

    public Logro() {}

    public Logro(String id, String codigo, String nombre, String descripcion, String iconoEmoji, int xpBonus) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.iconoEmoji = iconoEmoji;
        this.xpBonus = xpBonus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIconoEmoji() { return iconoEmoji; }
    public void setIconoEmoji(String iconoEmoji) { this.iconoEmoji = iconoEmoji; }

    public int getXpBonus() { return xpBonus; }
    public void setXpBonus(int xpBonus) { this.xpBonus = xpBonus; }
}