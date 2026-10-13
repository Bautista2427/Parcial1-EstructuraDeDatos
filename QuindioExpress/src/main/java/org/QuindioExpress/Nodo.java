package org.QuindioExpress;

final class Nodo<T> {

    final T valor;
    Nodo<T> siguiente;

    Nodo(T valor) {
        this.valor = valor;
    }
}
