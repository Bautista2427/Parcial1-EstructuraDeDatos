package org.QuindioExpress;

public class Repartidor {
    private final String id;
    private final String nombre;
    private final String zona;
    private boolean disponible;

    public Repartidor(String id, String nombre, String zona, boolean disponible) {
        this.id = validarTexto(id, "Identificacion");
        this.nombre = validarTexto(nombre, "Nombre");
        this.zona = validarTexto(zona, "Zona");
        this.disponible = disponible;
    }

    private static String validarTexto(String valor, String campo) {
        if(valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + "invalido");
        }
        return valor.strip();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return id + "-" + nombre + " (zona " + zona + ", " + (disponible ? "disponible" : "ocupado") + ")";
    }
}
