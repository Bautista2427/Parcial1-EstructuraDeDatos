package org.QuindioExpress;

import java.util.Comparator;

public final class ComparadoresPaquete {
    private ComparadoresPaquete() {}

    public static final Comparator<Paquete> POR_PRIORIDAD_DESC =
            Comparator.comparingInt(Paquete::getPrioridad).reversed();

    public static final Comparator<Paquete> POR_PESO_DESC =
            Comparator.comparingDouble(Paquete::getPeso).reversed();

    public static final Comparator<Paquete> POR_TIEMPO_ASC =
            Comparator.comparingInt(Paquete::getTiempoEstimado);

    public static final Comparator<Paquete> DESPACHO =
            POR_PRIORIDAD_DESC.thenComparing(POR_TIEMPO_ASC).thenComparing(Comparator.naturalOrder());
}
