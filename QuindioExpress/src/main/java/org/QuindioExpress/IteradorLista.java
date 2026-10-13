package org.QuindioExpress;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteradorLista<T> implements Iterator<T> {

    private Nodo<T> actual;

    public IteradorLista(Nodo<T> cabeza) {
        this.actual = cabeza;
    }

    @Override
    public boolean hasNext() {
        return actual != null;
    }

    @Override
    public T next() {
        if (actual == null) {
            throw new NoSuchElementException("No hay más elementos");
        }

        T valor = actual.valor;
        actual = actual.siguiente;

        return valor;
    }
}
