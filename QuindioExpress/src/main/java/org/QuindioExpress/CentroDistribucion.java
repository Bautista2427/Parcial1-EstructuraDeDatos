package org.QuindioExpress;

import java.text.Collator;
import java.util.*;

public class CentroDistribucion {

    public enum Modalidad {
        LLEGADA,
        PRIORIDAD
    }

    private final List<Paquete> registro = new ArrayList<>();
    private final Map<String, Paquete> indice = new HashMap<>();
    private final Queue<Paquete> colaLlegada = new ArrayDeque<>();
    private final PriorityQueue<Paquete> colaPrioridad = new PriorityQueue<>(ComparadoresPaquete.DESPACHO);
    private final Set<String> municipios = new HashSet<>();
    private final Map<String, List<Paquete>> porDestino = new HashMap<>();
    private final Map<String, Repartidor> repartidores = new LinkedHashMap<>();
    private final Map<String, Repartidor> asignaciones = new HashMap<>();
    private final ListaSimple<Paquete> historial = new ListaSimple<>();


    public void registrarPaquete(Paquete p) {
        if (p == null) throw new IllegalArgumentException("Paquete nulo");
        if (indice.containsKey(p.getCodigo()))
            throw new IllegalArgumentException("Código duplicado: " + p.getCodigo());
        registro.add(p);
        indice.put(p.getCodigo(), p);
        colaLlegada.offer(p);
        colaPrioridad.offer(p);
        municipios.add(p.getDestino());
        porDestino.computeIfAbsent(p.getDestino(), k -> new ArrayList<>()).add(p);
    }

    public void registrarRepartidor(Repartidor r) {
        if (r == null) throw new IllegalArgumentException("Repartidor nulo");
        if (repartidores.containsKey(r.getId()))
            throw new IllegalArgumentException("Identificación duplicada: " + r.getId());
        repartidores.put(r.getId(), r);
    }


    public boolean existe(String codigo) {
        return indice.containsKey(codigo);
    }

    public Optional<Paquete> consultar(String codigo) {
        return Optional.ofNullable(indice.get(codigo));
    }

    public Optional<Repartidor> consultarRepartidor(String id) {
        return Optional.ofNullable(repartidores.get(id));
    }

    public List<Paquete> getRegistro() {
        return Collections.unmodifiableList(registro);
    }


    public List<Paquete> getPendientes() {
        return new ArrayList<>(colaLlegada);
    }

    public Collection<Repartidor> getRepartidores() {
        return Collections.unmodifiableCollection(repartidores.values());
    }

    public Set<String> getMunicipios() {
        return Collections.unmodifiableSet(municipios);
    }


    public Set<String> getMunicipiosOrdenados() {
        Set<String> ordenados = new TreeSet<>(Collator.getInstance(Locale.forLanguageTag("es-CO")));
        ordenados.addAll(municipios);
        return ordenados;
    }

    public List<Paquete> getPaquetesPorMunicipio(String municipio) {
        List<Paquete> l = porDestino.get(municipio);
        return l == null ? Collections.<Paquete>emptyList() : Collections.unmodifiableList(l);
    }

    public Map<String, List<Paquete>> getPaquetesAgrupados() {
        return Collections.unmodifiableMap(porDestino);
    }

    public ListaSimple<Paquete> getHistorial() {
        return historial;
    }


    public List<Paquete> ordenados(Comparator<Paquete> criterio) {
        List<Paquete> copia = new ArrayList<>(registro);
        copia.sort(criterio);
        return copia;
    }


    public void asignar(Paquete p, Repartidor r) {
        if (!r.isDisponible())
            throw new IllegalStateException("El repartidor " + r.getNombre() + " no está disponible");
        if (!r.getZona().equalsIgnoreCase(p.getDestino()))
            throw new IllegalStateException("La zona " + r.getZona() + " no corresponde al destino " + p.getDestino());
        r.setDisponible(false);
        asignaciones.put(p.getCodigo(), r);
    }

    public Optional<Repartidor> buscarRepartidorDisponible(Paquete p) {
        for (Repartidor r : repartidores.values())
            if (r.isDisponible() && r.getZona().equalsIgnoreCase(p.getDestino())) return Optional.of(r);
        return Optional.empty();
    }


    public Optional<Paquete> siguiente(Modalidad m) {
        return Optional.ofNullable(m == Modalidad.LLEGADA ? colaLlegada.peek() : colaPrioridad.peek());
    }


    public Paquete despachar(Modalidad m) {
        Paquete p = siguiente(m).orElseThrow(() -> new IllegalStateException("No hay paquetes pendientes"));
        Repartidor r = buscarRepartidorDisponible(p).orElseThrow(() ->
                new IllegalStateException("No hay repartidor disponible en " + p.getDestino() + " para " + p.getCodigo()));
        asignar(p, r);
        retirarPendiente(p);
        return p;
    }

    public Repartidor repartidorAsignado(String codigo) {
        return asignaciones.get(codigo);
    }


    public void registrarEntrega(String codigo) {
        Repartidor r = asignaciones.remove(codigo);
        if (r == null) throw new IllegalStateException("El paquete " + codigo + " no está en ruta");
        historial.agregar(indice.get(codigo));
        r.setDisponible(true);
    }

    private void retirarPendiente(Paquete p) {
        colaLlegada.remove(p);
        colaPrioridad.remove(p);
        List<Paquete> lista = porDestino.get(p.getDestino());
        lista.remove(p);
        if (lista.isEmpty()) {
            porDestino.remove(p.getDestino());
            municipios.remove(p.getDestino());
        }
    }
}
