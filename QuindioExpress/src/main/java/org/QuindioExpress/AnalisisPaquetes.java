package org.QuindioExpress;

import java.util.List;

public final class AnalisisPaquetes {
    private AnalisisPaquetes() {}

    public static double pesoTotalPorDestino(List<Paquete> paquetes, String destino) {
        return pesoTotal(paquetes, destino, 0);
    }

    private static double pesoTotal(List<Paquete> paquetes, String destino, int indice) {
        if (indice == paquetes.size()) return 0.0;
        Paquete p = paquetes.get(indice);
        double aporte = p.getDestino().equalsIgnoreCase(destino) ? p.getPeso() : 0.0;
        return aporte + pesoTotal(paquetes, destino, indice + 1);
    }

    public static int contarPrioridadMinima(List<Paquete> paquetes, int prioridadMinima) {
        return contar(paquetes, prioridadMinima, 0);
    }

    private static int contar(List<Paquete> paquetes, int minima, int indice) {
        if (indice == paquetes.size()) return 0;
        int aporte = paquetes.get(indice).getPrioridad() >= minima ? 1 : 0;
        return aporte + contar(paquetes, minima, indice + 1);
    }


    public static int busquedaBinaria(List<Paquete> ordenadaPorCodigo, String codigo) {
        int bajo = 0;
        int alto = ordenadaPorCodigo.size() - 1;
        while (bajo <= alto) {
            int medio = bajo + (alto - bajo) / 2;
            int comparacion = ordenadaPorCodigo.get(medio).getCodigo().compareTo(codigo);
            if (comparacion == 0) return medio;
            if (comparacion < 0) bajo = medio + 1;
            else alto = medio - 1;
        }
        return -1;
    }

    public static boolean estaOrdenadaPorCodigo(List<Paquete> lista) {
        for (int i = 1; i < lista.size(); i++)
            if (lista.get(i - 1).compareTo(lista.get(i)) > 0) return false;
        return true;
    }


    public static Paquete masPesado(List<Paquete> paquetes) {
        if (paquetes == null || paquetes.isEmpty())
            throw new IllegalArgumentException("No hay paquetes para analizar");
        return masPesado(paquetes, 0, paquetes.size() - 1);
    }

    private static Paquete masPesado(List<Paquete> paquetes, int inicio, int fin) {
        if (inicio == fin) return paquetes.get(inicio);
        int medio = inicio + (fin - inicio) / 2;
        Paquete izquierdo = masPesado(paquetes, inicio, medio);
        Paquete derecho = masPesado(paquetes, medio + 1, fin);
        return izquierdo.getPeso() >= derecho.getPeso() ? izquierdo : derecho;
    }
}
