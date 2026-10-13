package org.QuindioExpress;

import java.util.Iterator;
import java.util.Objects;

public class ListaSimple<T> implements Coleccion<T> {

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    @Override
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamano++;
    }

    @Override
    public boolean eliminar(T elemento) {
        Nodo<T> previo = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (Objects.equals(actual.valor, elemento)) {
                if (previo == null) {
                    cabeza = actual.siguiente;
                } else {
                    previo.siguiente = actual.siguiente;
                }
                if (actual == cola) {
                    cola = previo;
                }
                tamano--;
                return true;
            }
            previo = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    @Override
    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamano) {
            throw new IndexOutOfBoundsException("Posición " + posicion + " fuera de rango [0," + tamano + ")");
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual.valor;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteradorLista<>(cabeza);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T t : this) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append(t instanceof Paquete ? ((Paquete) t).getCodigo() : String.valueOf(t));
        }
        return sb.length() == 0 ? "(vacía)" : sb.toString();
    }
}
