# Quindío Express – Gestión de operaciones de un centro de distribución

Parcial 1 · Estructuras de Datos · Ingeniería de Sistemas y Computación

Aplicación de consola en Java que simula una jornada de trabajo del centro de distribución de **QuindíoExpress**: registro y consulta de paquetes y repartidores, agrupación por municipio, despacho por orden de llegada o por prioridad, asignación de repartidores, historial de entregas y análisis con recursividad, búsqueda binaria y divide y vencerás.

## Integrante

Juan Felipe Bautista Oliveros

## Tabla de contenido

1. [Requisitos](#requisitos)
2. [Cómo compilar y ejecutar](#cómo-compilar-y-ejecutar)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Arquitectura y relación entre clases](#arquitectura-y-relación-entre-clases)
5. [Estructuras de datos elegidas](#estructuras-de-datos-elegidas)
6. [Lista simplemente enlazada propia](#lista-simplemente-enlazada-propia)
7. [Algoritmos](#algoritmos)
8. [Reglas de negocio implementadas](#reglas-de-negocio-implementadas)
9. [Datos de prueba](#datos-de-prueba)
10. [Qué muestra la ejecución](#qué-muestra-la-ejecución)
11. [Decisiones de diseño y limitaciones](#decisiones-de-diseño-y-limitaciones)
12. [Documentación adicional](#documentación-adicional)

## Requisitos

- JDK 17 o superior (el código usa solo la biblioteca estándar; no hay dependencias externas).
- Maven (opcional; también se puede compilar con `javac`).

> El `pom.xml` define la versión de Java en las propiedades `maven.compiler.source` y `maven.compiler.target`. Si su JDK es anterior a la versión configurada, ajusten esos dos valores (por ejemplo a `17` o `21`).

## Cómo compilar y ejecutar

### Con Maven

```bash
cd QuindioExpress
mvn compile
mvn exec:java -Dexec.mainClass="org.QuindioExpress.Main"
```

Si no tienen el plugin `exec`, usen la opción siguiente.

### Con `javac` (sin Maven)

```bash
cd QuindioExpress
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/org/QuindioExpress/*.java
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out org.QuindioExpress.Main
```

### Desde IntelliJ IDEA

Abrir la carpeta `QuindioExpress`, esperar a que importe el proyecto Maven y ejecutar la clase `org.QuindioExpress.Main`.

> Las tildes se ven bien solo si la consola usa UTF-8; los parámetros `-Dfile.encoding` y `-Dstdout.encoding` de arriba lo garantizan.

## Estructura del proyecto

```
Parcial1-EstructuraDeDatos/
├── README.md
└── QuindioExpress/
    ├── pom.xml
    └── src/main/java/org/QuindioExpress/
        ├── Main.java                 Datos de prueba y demostración
        ├── CentroDistribucion.java   Fachada: coordina todas las estructuras
        ├── Paquete.java              Modelo (Comparable, validaciones)
        ├── Repartidor.java           Modelo
        ├── ComparadoresPaquete.java  Criterios alternativos (Comparator)
        ├── AnalisisPaquetes.java     Recursividad, búsqueda binaria, divide y vencerás
        ├── Coleccion.java            Interfaz genérica (contrato)
        ├── ListaSimple.java          Lista simplemente enlazada propia
        ├── Nodo.java                 Nodo de la lista (valor + siguiente)
        └── IteradorLista.java        Iterator de la lista
```

## Arquitectura y relación entre clases

```
                       Main  (demostración)
                        │ usa
                        ▼
              CentroDistribucion  (fachada)
      ┌───────┬─────────┼──────────┬───────────────┐
      │       │         │          │               │
   Paquete Repartidor ComparadoresPaquete    ListaSimple<T>
  (Comparable)         (Comparator)          implements Coleccion<T>
                                             usa Nodo<T> e IteradorLista<T>

     AnalisisPaquetes  (métodos estáticos que reciben List<Paquete>)
```

- `Main` solo conversa con `CentroDistribucion` y `AnalisisPaquetes`.
- `CentroDistribucion` guarda paquetes y repartidores, usa `ComparadoresPaquete` en su cola de prioridad y `ListaSimple` para el historial.
- `AnalisisPaquetes` no guarda estado: recibe una lista y calcula resultados.

### Ciclo de vida de un paquete

```
registrar → pendiente (en las colas) → despachar → en ruta (asignado a repartidor)
          → registrarEntrega → historial
```

## Estructuras de datos elegidas

| Necesidad | Estructura | Operación principal | Complejidad | Alternativa descartada |
|---|---|---|---|---|
| Orden exacto de registro (auditoría) | `ArrayList<Paquete>` | agregar al final | O(1) amortizado | `HashSet` (no conserva orden) |
| Atención por orden de llegada | `ArrayDeque` (`Queue`) | `offer` / `peek` / `poll` | O(1) | `ArrayList` (quitar al inicio es O(n)) |
| Consulta por código | `HashMap<String, Paquete>` | `get` / `containsKey` | O(1) promedio | `ArrayList` + recorrido, O(n) |
| Municipios sin repetir | `HashSet<String>` | `add` / `contains` | O(1) promedio | `ArrayList` + `contains`, O(n) |
| Municipios ordenados sin repetir | `TreeSet<String>` con `Collator` es-CO | `add` / recorrido ordenado | O(log n) | `HashSet` + ordenar cada vez |
| Paquetes por municipio | `HashMap<String, List<Paquete>>` | `get` | O(1) promedio | Recorrer todos los paquetes por cada consulta |
| Despacho prioritario | `PriorityQueue` con `DESPACHO` | `offer` / `poll` | O(log n) | Lista reordenada tras cada inserción |
| Repartidores | `LinkedHashMap<String, Repartidor>` | `get` por id | O(1) promedio | `List` (búsqueda O(n)) |
| Historial de entregas | `ListaSimple<Paquete>` propia | agregar al final | O(1) | `LinkedList` (prohibida por el enunciado) |

Un mismo `Paquete` está referenciado desde varias estructuras a la vez (se duplican referencias, no objetos). Cada estructura responde a una pregunta distinta; el costo es más memoria y la necesidad de mantenerlas sincronizadas, algo que centraliza `CentroDistribucion`.

### Orden natural y criterios alternativos

- **`Comparable`:** el orden natural de `Paquete` es por código ascendente (`compareTo`). `equals` y `hashCode` también usan solo el código, para ser coherentes.
- **`Comparator`** (en `ComparadoresPaquete`):
  - `POR_PRIORIDAD_DESC`: prioridad de mayor a menor.
  - `POR_PESO_DESC`: peso de mayor a menor.
  - `POR_TIEMPO_ASC`: tiempo estimado de menor a mayor.
  - `DESPACHO`: prioridad desc, luego tiempo asc, luego código (las tres reglas del enunciado).

Ordenar con un `Comparator` se hace sobre una copia, así que no altera el registro ni el orden natural.

## Lista simplemente enlazada propia

`ListaSimple<T>` implementa la interfaz genérica `Coleccion<T>` sin usar ninguna colección de Java para almacenar.

```
cabeza → [PQ105|•] → [PQ942|•] → [PQ731|null] ← cola
```

| Operación | Complejidad | Notas |
|---|---|---|
| `agregar` | O(1) | Se conserva la referencia a la cola |
| `eliminar(elemento)` | O(n) | Primero, intermedio, último, inexistente y lista vacía |
| `obtener(posicion)` | O(n) | Lanza `IndexOutOfBoundsException` si la posición no es válida |
| `tamano`, `estaVacia` | O(1) | Contador interno |
| `iterator()` | O(1) | Cada llamada crea un `IteradorLista` independiente |

- **Genérica:** funciona con cualquier tipo (en la demostración se usa con `Integer`, `String` y `Paquete`).
- **Encapsulada:** el código externo nunca accede al primer nodo ni a las referencias entre nodos; recorre con `for-each` gracias a `Iterable`.
- **`Iterable` vs `Iterator`:** `ListaSimple` es `Iterable` (puede dar un recorrido) e `IteradorLista` es el `Iterator` (el recorrido en curso). Se pueden hacer varios recorridos simultáneos sin interferencia.

## Algoritmos

Todos están en `AnalisisPaquetes`.

| Algoritmo | Tiempo | Espacio | Detalle |
|---|---|---|---|
| `pesoTotalPorDestino` (recursivo) | O(n) | O(n) pila | Caso base: `indice == size`. Suma el aporte del actual más la llamada con `indice + 1` |
| `contarPrioridadMinima` (recursivo) | O(n) | O(n) pila | Misma estructura, cuenta los paquetes con prioridad ≥ mínima |
| `busquedaBinaria` | O(log n) | O(1) | **Precondición:** lista ordenada por código; se verifica con `estaOrdenadaPorCodigo` |
| `masPesado` (divide y vencerás) | O(n) | O(log n) pila | Caso base de un elemento, división por el punto medio, subproblemas izquierdo y derecho, combinación quedándose con el mayor |

Los métodos recursivos no usan `for`, `while` ni `do-while`, como exige el enunciado. El método de divide y vencerás parte realmente el problema en dos mitades y combina sus resultados; una recursión lineal no lo haría.

## Reglas de negocio implementadas

| Regla | Dónde se garantiza |
|---|---|
| No hay dos paquetes con el mismo código | `CentroDistribucion.registrarPaquete` |
| No hay dos repartidores con la misma identificación | `CentroDistribucion.registrarRepartidor` |
| Peso mayor que cero (y finito) | Constructor de `Paquete` |
| Prioridad entre 1 y 5 (por defecto 5) | Constructor de `Paquete` (constantes `PRIORIDAD_MIN`, `PRIORIDAD_MAX`, `PRIORIDAD_DEFECTO`) |
| Tiempo estimado positivo | Constructor de `Paquete` |
| Destino no nulo ni vacío | Constructor de `Paquete` |
| Un repartidor no disponible no recibe asignaciones | `CentroDistribucion.asignar` |
| La zona del repartidor debe coincidir con el destino | `CentroDistribucion.asignar` |
| Atención por llegada respeta el orden de registro | `ArrayDeque` (FIFO) |
| Despacho prioritario: prioridad, tiempo y código | `PriorityQueue` con `ComparadoresPaquete.DESPACHO` |
| El historial conserva el orden real de entrega | `ListaSimple` + `registrarEntrega` |
| El historial usa la lista propia | `CentroDistribucion.historial` |

## Datos de prueba

Definidos directamente en `Main.cargarDatos`:

- **30 paquetes** con códigos no consecutivos (PQ731, PQ105, PQ942…), pesos y prioridades variados, y varios paquetes al mismo municipio.
- **7 destinos:** Armenia, Calarcá, Circasia, Filandia, Montenegro, Quimbaya y Salento.
- **9 repartidores**, uno por zona más repartidores adicionales; uno de ellos (R09) no está disponible para demostrar esa regla.

## Qué muestra la ejecución

`Main` recorre una sección por cada requisito:

| Sección | Contenido |
|---|---|
| 1 | Registro de paquetes y repartidores, y las validaciones (duplicados, peso, destino, tiempo, prioridad) |
| 2 | Consulta por código con `HashMap` |
| 3 | Municipios sin repetir y municipios ordenados alfabéticamente |
| 4 | Paquetes agrupados por municipio |
| 5 | Orden natural y los criterios alternativos, comprobando que el registro no cambia |
| 6 | Recursividad: peso total por municipio y conteo por prioridad (ejemplo del enunciado: 14.0 kg) |
| 7 | Búsqueda binaria y por qué no aplica al registro original |
| 8 | Divide y vencerás: paquete más pesado |
| 9 | Despacho por orden de llegada y entregas (historial `PQ105 → PQ942 → PQ731`) |
| 10 | Despacho prioritario |
| 11 | Reglas de asignación (repartidor no disponible, zona distinta) |
| 12 | Historial con la lista propia: recorrido con `for-each`, `obtener`, `tamano` |
| 13 | Casos borde de la lista propia y uso con otros tipos |

Las validaciones y los errores se muestran con el prefijo `[OK]`: significa que la acción inválida fue rechazada con la excepción esperada. Un `[!]` indicaría que no se lanzó la excepción.

## Decisiones de diseño y limitaciones

- **Fachada (`CentroDistribucion`):** el resto del código no necesita saber que hay varias estructuras detrás, y la sincronización queda en un solo lugar.
- **Despacho sin pérdida:** si el siguiente paquete no tiene repartidor disponible en su zona, `despachar` lanza una excepción y el paquete **sigue pendiente**; solo se retira de las colas cuando la asignación tiene éxito.
- **`remove` en las colas es O(n):** retirar un paquete de `ArrayDeque` y de `PriorityQueue` por referencia recorre la estructura. Se aceptó a cambio de mantener ambas colas consistentes. Con muchísimos paquetes pendientes, una alternativa sería marcar los paquetes como despachados y descartarlos al sacarlos de la cola (borrado perezoso).
- **Municipios con pendientes:** `getMunicipios` y la agrupación por destino reflejan los paquetes **pendientes**; un municipio desaparece cuando se despacha su último paquete. El registro completo se conserva en `getRegistro`.
- **Inmutabilidad:** `Paquete` no cambia después de creado, lo que evita que las estructuras que lo contienen queden desordenadas.
- **Alcance:** solo se usan los temas del curso (recursividad, búsqueda binaria, divide y vencerás, colecciones, genéricos, `Iterable`/`Iterator` y lista propia). No hay archivos ni bases de datos.

## Documentación adicional

El documento PDF de entrega contiene:

- **Parte A:** selección de colecciones y comparación tiempo/memoria (`ArrayList` vs `HashMap` con 500.000 paquetes).
- **Parte C:** pruebas de escritorio, diagramas de pila de llamadas y análisis de complejidad.
- **Parte D:** bitácora de uso de inteligencia artificial y caso de respuesta cuestionable.
