package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.modelo.Inventario;
import es.joselyn.proyecto.modelo.ProductoStockBajoDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcInventarioDao implements InventarioDao {

    private static final Logger log = LogManager.getLogger(JdbcInventarioDao.class);

    @Override
    public int insertar(Connection conexion, Inventario inventario) throws SQLException {
        String sql = "INSERT INTO inventario (producto_id, stock_actual, stock_minimo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, inventario.productoId());
            ps.setInt(2, inventario.stockActual());
            ps.setInt(3, inventario.stockMinimo());
            int filas = ps.executeUpdate();
            log.debug("Inventario insertado producto={}", inventario.productoId());
            return filas;
        }
    }

    @Override
    public Inventario buscarPorProducto(Connection conexion, int productoId) throws SQLException {
        String sql = "SELECT producto_id, stock_actual, stock_minimo FROM inventario WHERE producto_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Inventario(
                            rs.getInt("producto_id"),
                            rs.getInt("stock_actual"),
                            rs.getInt("stock_minimo"));
                }
                return null;
            }
        }
    }

    @Override
    public int incrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE inventario SET stock_actual = stock_actual + ? WHERE producto_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            int filas = ps.executeUpdate();
            log.debug("Stock +{} producto={} filas={}", cantidad, productoId, filas);
            return filas;
        }
    }

    @Override
    public int decrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE inventario SET stock_actual = stock_actual - ? "
                + "WHERE producto_id = ? AND stock_actual >= ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            int filas = ps.executeUpdate();
            log.debug("Stock -{} producto={} filas={}", cantidad, productoId, filas);
            return filas;
        }
    }

    @Override
    public List<ProductoStockBajoDTO> buscarStockBajo(Connection conexion) throws SQLException {
        String sql = "SELECT p.id, p.nombre, i.stock_actual, i.stock_minimo,"
                + " i.stock_minimo - i.stock_actual AS unidades_a_reponer"
                + " FROM inventario i"
                + " JOIN producto p ON p.id = i.producto_id"
                + " WHERE p.activo = TRUE AND i.stock_actual < i.stock_minimo"
                + " ORDER BY unidades_a_reponer DESC, p.nombre";
        List<ProductoStockBajoDTO> productos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new ProductoStockBajoDTO(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"),
                        rs.getInt("unidades_a_reponer")));
            }
        }
        return productos;
    }
}