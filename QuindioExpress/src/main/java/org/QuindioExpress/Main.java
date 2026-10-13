package org.QuindioExpress;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        CentroDistribucion centro = new CentroDistribucion();
        cargarDatos(centro);

        titulo("1. REGISTRO Y VALIDACIONES");
        System.out.println("Paquetes registrados: " + centro.getRegistro().size());
        System.out.println("Repartidores: " + centro.getRepartidores().size());
        intentar("Código duplicado", () -> centro.registrarPaquete(new Paquete("PQ731", "Armenia", 1, 3, 10)));
        intentar("Peso <= 0", () -> new Paquete("PQ001", "Armenia", 0, 3, 10));
        intentar("Destino vacío", () -> new Paquete("PQ002", " ", 1, 3, 10));
        intentar("Tiempo <= 0", () -> new Paquete("PQ003", "Armenia", 1, 3, 0));
        intentar("Prioridad fuera de rango", () -> new Paquete("PQ004", "Armenia", 1, 11, 5));
        intentar("Repartidor duplicado", () -> centro.registrarRepartidor(new Repartidor("R01", "Otro", "Armenia", true)));

        titulo("1.1 REGISTRO Y CONSULTA DE REPARTIDORES");
        centro.registrarRepartidor(new Repartidor("R10", "Valentina Cruz", "Armenia", true));
        System.out.println("Repartidores registrados: " + centro.getRepartidores().size());
        System.out.println("Consulta R10 -> " + centro.consultarRepartidor("R10").orElse(null));
        System.out.println("Consulta R99 -> "
                + centro.consultarRepartidor("R99").map(Repartidor::toString).orElse("no existe"));
        System.out.println("Listado completo:");
        for (Repartidor r : centro.getRepartidores()) {
            System.out.println("  " + r);
        }

        titulo("2. CONSULTA POR CÓDIGO");
        System.out.println("¿Existe PQ731? " + centro.existe("PQ731"));
        System.out.println("PQ731 -> " + centro.consultar("PQ731").orElse(null));
        System.out.println("¿Existe PQ000? " + centro.existe("PQ000"));

        titulo("3. MUNICIPIOS Y ORDENADOS");
        System.out.println("Sin repetir: " + centro.getMunicipios());
        System.out.println("Ordenados:   " + centro.getMunicipiosOrdenados());

        titulo("4. PAQUETES POR MUNICIPIO");
        for (String m : centro.getMunicipiosOrdenados()) {
            StringBuilder sb = new StringBuilder();
            for (Paquete p : centro.getPaquetesPorMunicipio(m)) sb.append(p.getCodigo()).append(' ');
            System.out.println(m + " -> " + sb.toString().trim());
        }

        titulo("5. ORDEN NATURAL Y COMPARATORS");
        imprimirCodigos("Natural (código)      ", centro.ordenados(null));
        imprimirCodigos("Prioridad desc        ", centro.ordenados(ComparadoresPaquete.POR_PRIORIDAD_DESC));
        imprimirCodigos("Peso desc             ", centro.ordenados(ComparadoresPaquete.POR_PESO_DESC));
        imprimirCodigos("Tiempo asc            ", centro.ordenados(ComparadoresPaquete.POR_TIEMPO_ASC));
        imprimirCodigos("Registro (sin cambios)", centro.getRegistro());

        titulo("6. RECURSIVIDAD");
        List<Paquete> reg = centro.getRegistro();
        System.out.printf("Peso total Armenia: %.1f kg%n", AnalisisPaquetes.pesoTotalPorDestino(reg, "Armenia"));
        System.out.printf("Peso total Salento: %.1f kg%n", AnalisisPaquetes.pesoTotalPorDestino(reg, "Salento"));
        System.out.println("Paquetes con prioridad >= 4: " + AnalisisPaquetes.contarPrioridadMinima(reg, 4));

        List<Paquete> ej = new ArrayList<>();
        ej.add(new Paquete("PQ101", "Armenia", 4.5, 5, 10));
        ej.add(new Paquete("PQ203", "Salento", 3.0, 5, 10));
        ej.add(new Paquete("PQ415", "Armenia", 7.2, 5, 10));
        ej.add(new Paquete("PQ730", "Armenia", 2.3, 5, 10));
        System.out.printf("Ejemplo enunciado, Armenia: %.1f kg (esperado 14.0)%n",
                AnalisisPaquetes.pesoTotalPorDestino(ej, "Armenia"));

        titulo("7. BÚSQUEDA BINARIA (requiere orden por código)");
        List<Paquete> porCodigo = centro.ordenados(null);
        System.out.println("Precondición (ordenada): " + AnalisisPaquetes.estaOrdenadaPorCodigo(porCodigo));
        System.out.println("¿Registro original ordenado? " + AnalisisPaquetes.estaOrdenadaPorCodigo(centro.getRegistro())
                + " -> NO se puede aplicar búsqueda binaria sobre él");
        int pos = AnalisisPaquetes.busquedaBinaria(porCodigo, "PQ731");
        System.out.println("PQ731 en posición: " + pos + " -> " + (pos >= 0 ? porCodigo.get(pos) : "no existe"));
        System.out.println("PQ000 en posición: " + AnalisisPaquetes.busquedaBinaria(porCodigo, "PQ000"));

        titulo("8. DIVIDE Y VENCERÁS: PAQUETE MÁS PESADO");
        System.out.println("Más pesado: " + AnalisisPaquetes.masPesado(reg));

        titulo("9. DESPACHO POR ORDEN DE LLEGADA");
        for (int i = 0; i < 3; i++) {
            Paquete p = centro.despachar(CentroDistribucion.Modalidad.LLEGADA);
            System.out.println("Despachado " + p + " con " + centro.repartidorAsignado(p.getCodigo()));
        }

        System.out.println("-- Entregas --");
        for (String cod : new String[]{"PQ105", "PQ942", "PQ731"}) centro.registrarEntrega(cod);
        System.out.println("Historial parcial: " + centro.getHistorial());

        titulo("10. DESPACHO PRIORITARIO ");
        for (int i = 0; i < 3; i++) {
            Paquete p = centro.despachar(CentroDistribucion.Modalidad.PRIORIDAD);
            System.out.println("Despachado " + p + " con " + centro.repartidorAsignado(p.getCodigo()));
        }

        titulo("11. REGLAS DE ASIGNACIÓN");
        Paquete sig = centro.siguiente(CentroDistribucion.Modalidad.LLEGADA).get();
        System.out.println("Siguiente: " + sig);
        for (Repartidor r : centro.getRepartidores()) {
            final Repartidor rep = r;
            if (!r.isDisponible()) intentar("Asignar a no disponible (" + r.getId() + ")", () -> centro.asignar(sig, rep));
        }
        Repartidor otraZona = null;
        for (Repartidor r : centro.getRepartidores())
            if (r.isDisponible() && !r.getZona().equalsIgnoreCase(sig.getDestino())) { otraZona = r; break; }
        final Repartidor rz = otraZona;
        intentar("Asignar a zona distinta", () -> centro.asignar(sig, rz));

        titulo("12. HISTORIAL DE ENTREGAS ");
        System.out.println("¿Historial vacío? " + centro.getHistorial().estaVacia());
        String[] enRuta = enRuta(centro, reg);

        for (int i = enRuta.length - 1; i >= 0; i--) centro.registrarEntrega(enRuta[i]);
        System.out.println("Historial: " + centro.getHistorial());
        System.out.println("Cantidad: " + centro.getHistorial().tamano());
        System.out.print("Recorrido con for-each (Iterable): ");
        for (Paquete p : centro.getHistorial()) System.out.print(p.getCodigo() + " ");
        System.out.println();
        System.out.println("obtener(0) = " + centro.getHistorial().obtener(0).getCodigo());
        intentar("Entregar paquete que no está en ruta", () -> centro.registrarEntrega("PQ731"));
        System.out.println("Pendientes restantes: " + centro.getPendientes().size());

        titulo("13. PRUEBAS DE LA LISTA PROPIA (casos borde)");
        probarLista();
    }



    private static void cargarDatos(CentroDistribucion c) {
        Object[][] d = {
                {"PQ731","Armenia",4.5,5,45},   {"PQ105","Salento",3.0,5,25},   {"PQ942","Armenia",7.2,4,20},
                {"PQ318","Calarcá",12.0,3,35},  {"PQ567","Salento",2.3,2,40},   {"PQ829","Armenia",18.7,5,30},
                {"PQ420","Montenegro",5.1,3,30},{"PQ214","Quimbaya",9.8,4,50},  {"PQ421","Circasia",1.2,1,15},
                {"PQ842","Filandia",6.4,5,55},  {"PQ377","Armenia",3.3,2,22},   {"PQ654","Calarcá",8.0,4,28},
                {"PQ109","Montenegro",14.5,5,32},{"PQ988","Quimbaya",2.7,3,44}, {"PQ251","Circasia",7.7,2,18},
                {"PQ463","Armenia",10.1,1,26},  {"PQ780","Filandia",4.0,4,60},  {"PQ136","Salento",5.5,3,38},
                {"PQ592","Calarcá",1.9,5,21},   {"PQ847","Armenia",6.0,3,33},   {"PQ305","Montenegro",3.8,2,27},
                {"PQ719","Quimbaya",11.2,5,48}, {"PQ224","Circasia",4.9,4,16},  {"PQ936","Armenia",2.2,5,24},
                {"PQ471","Salento",9.0,1,42},   {"PQ682","Filandia",3.5,3,52},  {"PQ158","Calarcá",15.3,2,31},
                {"PQ803","Montenegro",6.6,4,36},{"PQ349","Armenia",8.8,2,29},   {"PQ517","Quimbaya",5.0,3,46}
        };
        for (Object[] f : d)
            c.registrarPaquete(new Paquete((String) f[0], (String) f[1], (Double) f[2], (Integer) f[3], (Integer) f[4]));

        String[][] r = {
                {"R01","Carlos Gómez","Armenia"},   {"R02","Lucía Ramírez","Armenia"},
                {"R03","Andrés Pineda","Salento"},  {"R04","María Torres","Calarcá"},
                {"R05","Julián Herrera","Montenegro"},{"R06","Sofía Castaño","Quimbaya"},
                {"R07","Diego Mejía","Circasia"},   {"R08","Paula Ospina","Filandia"}
        };
        for (String[] f : r) c.registrarRepartidor(new Repartidor(f[0], f[1], f[2], true));

        c.registrarRepartidor(new Repartidor("R09", "Camilo Ríos", "Salento", false));
    }




    private static String[] enRuta(CentroDistribucion c, List<Paquete> reg) {
        List<String> l = new ArrayList<>();
        for (Paquete p : reg) if (c.repartidorAsignado(p.getCodigo()) != null) l.add(p.getCodigo());
        return l.toArray(new String[0]);
    }

    private static void probarLista() {
        ListaSimple<Integer> l = new ListaSimple<>();
        System.out.println("Vacía: " + l.estaVacia() + ", tamaño " + l.tamano());
        System.out.println("Eliminar en vacía: " + l.eliminar(1));
        intentar("obtener(0) en vacía", () -> l.obtener(0));
        l.agregar(10);
        System.out.println("Un elemento: " + l + " | eliminar(10)=" + l.eliminar(10) + " -> " + l);
        l.agregar(1); l.agregar(2); l.agregar(3); l.agregar(4);
        System.out.println("Lista: " + l);
        System.out.println("Eliminar primero (1): " + l.eliminar(1) + " -> " + l);
        System.out.println("Eliminar intermedio (3): " + l.eliminar(3) + " -> " + l);
        System.out.println("Eliminar último (4): " + l.eliminar(4) + " -> " + l);
        l.agregar(99);
        System.out.println("Agregar tras eliminar último: " + l);
        System.out.println("Eliminar inexistente (77): " + l.eliminar(77) + " -> " + l);
        ListaSimple<String> ls = new ListaSimple<>();
        ls.agregar("a"); ls.agregar("b");
        System.out.println("Genérica con String: " + ls + " tamaño " + ls.tamano());
    }

    private static void imprimirCodigos(String etiqueta, List<Paquete> l) {
        StringBuilder sb = new StringBuilder();
        for (Paquete p : l) sb.append(p.getCodigo()).append(' ');
        System.out.println(etiqueta + ": " + sb.toString().trim());
    }

    private static void titulo(String t) {
        System.out.println("\n=== " + t + " ===");
    }

    private static void intentar(String descripcion, Runnable accion) {
        try {
            accion.run();
            System.out.println("[!] " + descripcion + ": NO lanzó excepción");
        } catch (RuntimeException e) {
            System.out.println("[OK] " + descripcion + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
