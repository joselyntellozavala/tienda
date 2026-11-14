package es.joselyn.proyecto.database;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.database.DatosIniciales.NuevaDireccion;
import es.joselyn.proyecto.database.DatosIniciales.NuevoArtesano;
import es.joselyn.proyecto.database.DatosIniciales.NuevoCliente;
import es.joselyn.proyecto.database.DatosIniciales.NuevoProducto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CargaInicial {

    private static final Logger log = LogManager.getLogger(CargaInicial.class);

    private final ConnectionProvider proveedor;

    public CargaInicial(ConnectionProvider proveedor) {
        this.proveedor = proveedor;
    }

    public void cargar() throws SQLException {
        log.info("Inicio de la carga inicial");
        try (Connection conexion = proveedor.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                Map<String, Integer> artesanos = insertarArtesanos(conexion);
                Map<String, Integer> categorias = insertarCategorias(conexion);
                Map<String, Integer> productos = insertarProductos(conexion, artesanos);
                insertarProductoCategoria(conexion, productos, categorias);
                insertarInventario(conexion, productos);
                Map<String, Integer> clientes = insertarClientes(conexion);
                insertarDirecciones(conexion, clientes);

                conexion.commit();
                log.info("Carga inicial confirmada (commit)");
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                log.warn("Carga inicial revertida (rollback): {}", e.getMessage());
                throw e;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    private Map<String, Integer> insertarArtesanos(Connection conexion) throws SQLException {
        String sql = "INSERT INTO artesano (nombre, especialidad) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (NuevoArtesano a : DatosIniciales.ARTESANOS) {
                ps.setString(1, a.nombre());
                ps.setString(2, a.especialidad());
                ps.addBatch();
            }
            ps.executeBatch();
            return idsGenerados(ps, "artesano", DatosIniciales.ARTESANOS.stream().map(NuevoArtesano::nombre).toList());
        }
    }

    private Map<String, Integer> insertarCategorias(Connection conexion) throws SQLException {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (String nombre : DatosIniciales.CATEGORIAS) {
                ps.setString(1, nombre);
                ps.addBatch();
            }
            ps.executeBatch();
            return idsGenerados(ps, "categoria", DatosIniciales.CATEGORIAS);
        }
    }

    private Map<String, Integer> insertarProductos(Connection conexion, Map<String, Integer> artesanos) throws SQLException {
        String sql = "INSERT INTO producto (nombre, artesano_id, precio, tipo_pieza, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (NuevoProducto p : DatosIniciales.PRODUCTOS) {
                ps.setString(1, p.nombre());
                ps.setInt(2, idDe(artesanos, p.artesano(), "artesano"));
                ps.setBigDecimal(3, p.precio());
                ps.setString(4, p.tipoPieza().name());
                ps.setBoolean(5, p.activo());
                ps.addBatch();
            }
            ps.executeBatch();
            return idsGenerados(ps, "producto", DatosIniciales.PRODUCTOS.stream().map(NuevoProducto::nombre).toList());
        }
    }

    private void insertarProductoCategoria(Connection conexion, Map<String, Integer> productos,
                                           Map<String, Integer> categorias) throws SQLException {
        String sql = "INSERT INTO producto_categoria (producto_id, categoria_id) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (NuevoProducto p : DatosIniciales.PRODUCTOS) {
                for (String categoria : p.categorias()) {
                    ps.setInt(1, idDe(productos, p.nombre(), "producto"));
                    ps.setInt(2, idDe(categorias, categoria, "categoria"));
                    ps.addBatch();
                }
            }
            int[] resultados = ps.executeBatch();
            log.info("Lote producto_categoria: {} filas", resultados.length);
        }
    }

    private void insertarInventario(Connection conexion, Map<String, Integer> productos) throws SQLException {
        String sql = "INSERT INTO inventario (producto_id, stock_actual, stock_minimo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (NuevoProducto p : DatosIniciales.PRODUCTOS) {
                ps.setInt(1, idDe(productos, p.nombre(), "producto"));
                ps.setInt(2, p.stockActual());
                ps.setInt(3, p.stockMinimo());
                ps.addBatch();
            }
            int[] resultados = ps.executeBatch();
            log.info("Lote inventario: {} filas", resultados.length);
        }
    }

    private Map<String, Integer> insertarClientes(Connection conexion) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, apellidos, email, fecha_alta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (NuevoCliente c : DatosIniciales.CLIENTES) {
                ps.setString(1, c.nombre());
                ps.setString(2, c.apellidos());
                ps.setString(3, c.email());
                ps.setObject(4, c.fechaAlta());
                ps.addBatch();
            }
            ps.executeBatch();
            return idsGenerados(ps, "cliente", DatosIniciales.CLIENTES.stream().map(NuevoCliente::email).toList());
        }
    }

    private void insertarDirecciones(Connection conexion, Map<String, Integer> clientes) throws SQLException {
        String sql = "INSERT INTO direccion (cliente_id, calle, ciudad, codigo_postal, principal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (NuevoCliente c : DatosIniciales.CLIENTES) {
                for (NuevaDireccion d : c.direcciones()) {
                    ps.setInt(1, idDe(clientes, c.email(), "cliente"));
                    ps.setString(2, d.calle());
                    ps.setString(3, d.ciudad());
                    ps.setString(4, d.codigoPostal());
                    ps.setBoolean(5, d.principal());
                    ps.addBatch();
                }
            }
            int[] resultados = ps.executeBatch();
            log.info("Lote direccion: {} filas", resultados.length);
        }
    }

    private Map<String, Integer> idsGenerados(PreparedStatement ps, String tabla, List<String> claves) throws SQLException {
        Map<String, Integer> ids = new LinkedHashMap<>();
        int recibidos = 0;
        try (ResultSet generados = ps.getGeneratedKeys()) {
            while (generados.next()) {
                if (recibidos < claves.size()) {
                    ids.put(claves.get(recibidos), generados.getInt(1));
                }
                recibidos++;
            }
        }
        if (recibidos != claves.size()) {
            throw new IllegalStateException("Lote " + tabla + ": se esperaban " + claves.size() + " ids generados y llegaron " + recibidos);
        }
        log.info("Lote {}: {} filas", tabla, ids.size());
        log.debug("Ids generados en {}: {}", tabla, ids);
        return ids;
    }

    private int idDe(Map<String, Integer> ids, String clave, String tabla) {
        Integer id = ids.get(clave);
        if (id == null) {
            throw new IllegalStateException("No existe " + tabla + " '" + clave + "' en los datos iniciales");
        }
        return id;
    }
}