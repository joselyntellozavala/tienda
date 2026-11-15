# Calmma · Tienda online de cerámica hecha a mano

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)
![JDBC](https://img.shields.io/badge/JDBC-sin_ORM-5382A1)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?logo=mysql&logoColor=white)
![H2](https://img.shields.io/badge/H2-2.5.252-1F6FB2)
![HikariCP](https://img.shields.io/badge/HikariCP-7.1.0-2E7D32)
![Log4j](https://img.shields.io/badge/Log4j-2.26.1-D22128?logo=apache&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5.14.4-25A162?logo=junit5&logoColor=white)


Repositorio: <https://github.com/joselyntellozavala/tienda>

Aplicación Java de consola que simula una tienda online de cerámica artesanal. Usa **JDBC directo** (sin ORM) y funciona con **MySQL o H2** y con conexiones de **DriverManager o HikariCP**, cambiando solo `application.properties`.

Cada ejecución:

1. Lee y valida `application.properties`.
2. Reconstruye las 11 tablas de la base `unidad2` (y, en MySQL, el procedimiento almacenado).
3. Hace la carga inicial por lotes (`addBatch` / `executeBatch`) en una transacción y recupera los ids generados.
4. Ejecuta la simulación: catálogo, clientes, inventario y reposición, pedidos, pagos, cancelaciones y consultas de gestión. En modo `FALLO_TRANSACCION`, provoca un fallo antes del commit de un pedido y comprueba con SQL que el rollback no deja cambios.

No se conservan datos entre ejecuciones: siempre se parte del mismo estado.

## Modelo de datos

Diagrama entidad-relación: [`docs/modelo-er.png`](docs/modelo-er.png) (fuente editable en Mermaid: [`docs/modelo-er.mmd`](docs/modelo-er.mmd)).

| Área | Tablas |
|---|---|
| Catálogo | `artesano`, `producto`, `categoria`, `producto_categoria` |
| Clientes | `cliente`, `direccion` |
| Inventario | `inventario`, `movimiento_stock` |
| Pedidos y pagos | `pedido`, `linea_pedido`, `pago` |

| Relación | Tipo | Cómo se garantiza |
|---|---|---|
| artesano → producto, cliente → direccion, cliente → pedido, direccion → pedido, pedido → linea_pedido, producto → movimiento_stock | 1:N | FK en el lado N |
| pedido → movimiento_stock | 1:N opcional | FK que admite NULL (las reposiciones no tienen pedido) |
| producto ↔ inventario | 1:1 | PK compartida: `inventario.producto_id` es PK y FK |
| pedido ↔ pago | 1:1 | FK + `UNIQUE` en `pago.pedido_id` |
| producto ↔ categoria | N:M | Tabla intermedia `producto_categoria` con PK compuesta |

**Característica propia de la temática:** cada producto es `SERIE` o `UNICA` (`tipo_pieza`). Una pieza única no se puede reponer (regla de negocio) y la consulta propia muestra las ventas por artesano separando piezas únicas y de serie.

`linea_pedido` guarda el precio aplicado al comprar, así que un cambio de precio en el catálogo no altera los pedidos anteriores.

## Requisitos

| Elemento | Versión |
|---|---|
| JDK | 25 |
| Maven | 3.9 o superior (o el integrado en IntelliJ IDEA) |
| MySQL | 8.0 o superior (probado con 8.0 y 9.7). Solo para `database.engine=MYSQL` |
| H2 | No se instala: va como dependencia (2.5.252) |

El usuario de MySQL necesita permiso para crear la base `unidad2`, sus tablas y procedimientos (por ejemplo, `root`).

Dependencias (en `pom.xml`): `mysql-connector-j`, `h2`, `HikariCP`, `log4j-core`, `log4j-slf4j2-impl` y `junit-jupiter` (solo pruebas).

## Configuración

`application.properties` contiene la contraseña y **no está en el repositorio**. Para crearlo:

1. Copia `application.properties.example` en la raíz del proyecto con el nombre `application.properties`.
2. Si vas a usar MySQL, cambia `mysql.password=CAMBIAR` por tu contraseña y, si hace falta, el puerto de `mysql.url`.

| Propiedad | Valores | Efecto |
|---|---|---|
| `database.engine` | `MYSQL` / `H2` | Gestor de base de datos |
| `connection.mode` | `DRIVER_MANAGER` / `POOL` | Conexiones directas o pool HikariCP |
| `simulation.mode` | `NORMAL` / `FALLO_TRANSACCION` | Simulación habitual o fallo controlado con rollback |
| `mysql.*`, `h2.*` | URL, usuario y contraseña | Solo se leen las del gestor elegido |
| `pool.maximumPoolSize`, `pool.connectionTimeout` | Enteros > 0 | Tamaño del pool y espera máxima (ms) |

Si falta el fichero o un valor no es válido, la aplicación se detiene al arrancar con un mensaje que indica el problema.

## Ejecución

Clase principal: `es.joselyn.proyecto.app.Main`. Debe ejecutarse con la **raíz del proyecto como directorio de trabajo**, porque ahí busca `application.properties`.

- **IntelliJ IDEA:** abre `Main` y pulsa ▶ junto a `main`.
- **Terminal:**

  ```
  mvn -q compile exec:java -Dexec.mainClass=es.joselyn.proyecto.app.Main
  ```

Para cambiar de modo, solo se editan estas líneas de `application.properties`. El código, los DAO y los servicios son los mismos:

| Ejecución | `database.engine` | `connection.mode` | `simulation.mode` |
|---|---|---|---|
| MySQL con pool | `MYSQL` | `POOL` | `NORMAL` |
| MySQL con DriverManager | `MYSQL` | `DRIVER_MANAGER` | `NORMAL` |
| H2 con DriverManager | `H2` | `DRIVER_MANAGER` | `NORMAL` |
| H2 con pool | `H2` | `POOL` | `NORMAL` |
| Fallo controlado | `MYSQL` o `H2` | cualquiera | `FALLO_TRANSACCION` |

## Pruebas

```
mvn test
```

En IntelliJ: clic derecho en `src/test/java` → *Run 'All Tests'*.

- 10 pruebas de integración con JUnit 5 contra **H2 en memoria** (`jdbc:h2:mem:calmma_pruebas`). No necesitan MySQL ni leen `application.properties`.
- Antes de cada prueba se reconstruye el esquema y se repite la carga inicial, así que son reproducibles e independientes del orden.
- Cubren inserción y consulta, modificación, id generado usado para relacionar, consultas con DTO, commit y rollback de un pedido, stock insuficiente y doble cancelación.
- Resultado esperado: `Tests run: 10, Failures: 0, Errors: 0`, con 4 líneas `WARN` de rollbacks provocados a propósito.

## Cómo comprobar los resultados

**Logs (Log4j 2, `src/main/resources/log4j2.xml`):**

| Salida | Contenido |
|---|---|
| Consola | Mensajes INFO o superiores |
| `logs/runtime/aplicacion.log` | Log activo. Rota al llegar a 20 KB en `aplicacion-yyyy-MM-dd-N.log.gz` y conserva 5 históricos por día. No se versiona |
| `logs/evidencias/ejecucion_dd-MM-yyyy_HH-mm-ss.log` | Un fichero completo por ejecución, con nivel DEBUG del proyecto |

**Evidencias versionadas** (`logs/evidencias/`, en orden cronológico):

1. MySQL · POOL · NORMAL
2. H2 · DRIVER_MANAGER · NORMAL
3. MySQL · DRIVER_MANAGER · FALLO_TRANSACCION
4. MySQL · DRIVER_MANAGER · NORMAL
5. H2 · POOL · NORMAL
6. H2 · DRIVER_MANAGER · FALLO_TRANSACCION

En las evidencias de FALLO_TRANSACCION, la comprobación termina con `Rollback verificado: no quedan cambios parciales`.

**Base H2:** queda en `data/unidad2.mv.db` (no se versiona). Se puede abrir con la ventana *Database* de IntelliJ (URL `jdbc:h2:./data/unidad2`, usuario `sa`, sin contraseña) cuando la aplicación no se está ejecutando: H2 embebido bloquea el fichero mientras lo usa un proceso.

## Diferencias entre MySQL y H2

- **Procedimiento almacenado** `resumen_ventas_producto`: solo existe en MySQL. En H2 no se crea ni se llama; el log lo indica y el resto de la aplicación funciona igual. No es un fallo.
- **Scripts de esquema:** uno por gestor (`esquema-mysql.sql` y `esquema-h2.sql`). Solo cambian los ids (`AUTO_INCREMENT` / `GENERATED BY DEFAULT AS IDENTITY`) y las fechas (`DATETIME` / `TIMESTAMP`).
- **Violación de integridad:** el email duplicado da SQLState `23000` y código `1062` en MySQL, y `23505` en H2.

## Estructura

```
├── docs/                         diagrama entidad-relación (PNG y fuente Mermaid)
├── logs/evidencias/              evidencias de ejecución versionadas
├── src/main/java/es/joselyn/proyecto/
│   ├── app/                      Main (dependencias) y Simulacion (escenarios)
│   ├── config/                   AppConfig y enums de configuración
│   ├── conexion/                 ConnectionProvider, DriverManager…, Hikari… y la factoría
│   ├── database/                 InicializadorEsquema, DatosIniciales y CargaInicial
│   ├── modelo/                   records de entidades, Nuevo…, DTO y enums
│   ├── dao/                      interfaces DAO
│   ├── dao/jdbc/                 implementaciones Jdbc…Dao
│   ├── servicio/                 servicios con las transacciones
│   └── excepcion/                ConfiguracionException, NegocioException, FalloSimuladoException
├── src/main/resources/           log4j2.xml y sql/ (esquemas y procedimiento)
├── src/test/                     pruebas JUnit 5 y log4j2-test.xml
├── application.properties.example
├── IA.md
└── pom.xml
```