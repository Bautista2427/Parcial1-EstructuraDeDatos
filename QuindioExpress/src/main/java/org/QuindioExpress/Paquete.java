package org.QuindioExpress;

public class Paquete implements Comparable<Paquete> {
    public static final int PRIORIDAD_MIN = 1;
    public static final int PRIORIDAD_MAX = 5;
    public static final int PRIORIDAD_DEFECTO = 5;

    private final String codigo;
    private final String destino;
    private final double peso;
    private final int prioridad;
    private final int tiempoEstimado;

    public Paquete(String codigo, String destino, double peso, int tiempoEstimado) {
        this(codigo, destino, peso, PRIORIDAD_DEFECTO, tiempoEstimado);
    }

    public Paquete(String codigo, String destino, double peso, int prioridad, int tiempoEstimado) {
        if (!Double.isFinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser finito y mayor que cero");
        }

        if (prioridad < PRIORIDAD_MIN || prioridad > PRIORIDAD_MAX) {
            throw new IllegalArgumentException("La prioridad debe estar entre "
                    + PRIORIDAD_MIN + " y " + PRIORIDAD_MAX);
        }

        if (tiempoEstimado <= 0) {
            throw new IllegalArgumentException("El tiempo estimado debe ser un entero positivo");
        }

        this.codigo = validarTexto(codigo, "El codigo");
        this.destino = validarTexto(destino, "El destino");
        this.peso = peso;
        this.prioridad = prioridad;
        this.tiempoEstimado = tiempoEstimado;
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " no puede ser nulo ni vacío");
        }

        return valor.trim();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    @Override
    public int compareTo(Paquete otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paquete)) return false;
        return codigo.equals(((Paquete) o).codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %.1fkg, prio=%d, %dmin]",
                codigo, destino, peso, prioridad, tiempoEstimado);
    }
}
