package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.TipoPieza;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcProductoDao implements ProductoDao {

    private static final Logger log = LogManager.getLogger(JdbcProductoDao.class);

    private static final String COLUMNAS = "SELECT id, nombre, artesano_id, precio, tipo_pieza, activo FROM producto";

    @Override
    public int insertar(Connection conexion, NuevoProducto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombre, artesano_id, precio, tipo_pieza, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.nombre());
            ps.setInt(2, producto.artesanoId());
            ps.setBigDecimal(3, producto.precio());
            ps.setString(4, producto.tipoPieza().name());
            ps.setBoolean(5, producto.activo());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "producto");
            log.debug("Producto insertado id={}", id);
            return id;
        }
    }

    @Override
    public Producto buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = COLUMNAS + " WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Producto buscarPorNombre(Connection conexion, String nombre) throws SQLException {
        String sql = COLUMNAS + " WHERE nombre = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public int actualizarPrecio(Connection conexion, int id, BigDecimal nuevoPrecio) throws SQLException {
        String sql = "UPDATE producto SET precio = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoPrecio);
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Precio actualizado producto={} filas={}", id, filas);
            return filas;
        }
    }

    @Override
    public int actualizarActivo(Connection conexion, int id, boolean activo) throws SQLException {
        String sql = "UPDATE producto SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Activo={} producto={} filas={}", activo, id, filas);
            return filas;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getInt("artesano_id"),
                rs.getBigDecimal("precio"),
                TipoPieza.valueOf(rs.getString("tipo_pieza")),
                rs.getBoolean("activo")
        );
    }
}