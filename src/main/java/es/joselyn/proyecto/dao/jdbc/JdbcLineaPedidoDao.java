package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.LineaPedidoDao;
import es.joselyn.proyecto.modelo.LineaPedido;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcLineaPedidoDao implements LineaPedidoDao {

    private static final Logger log = LogManager.getLogger(JdbcLineaPedidoDao.class);

    @Override
    public int insertar(Connection conexion, LineaPedido linea) throws SQLException {
        String sql = "INSERT INTO linea_pedido (pedido_id, producto_id, cantidad, precio_unitario, importe) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, linea.pedidoId());
            ps.setInt(2, linea.productoId());
            ps.setInt(3, linea.cantidad());
            ps.setBigDecimal(4, linea.precioUnitario());
            ps.setBigDecimal(5, linea.importe());
            int filas = ps.executeUpdate();
            log.debug("Línea insertada pedido={} producto={} cantidad={}",
                    linea.pedidoId(), linea.productoId(), linea.cantidad());
            return filas;
        }
    }

    @Override
    public List<LineaPedido> buscarPorPedido(Connection conexion, int pedidoId) throws SQLException {
        String sql = "SELECT pedido_id, producto_id, cantidad, precio_unitario, importe "
                + "FROM linea_pedido WHERE pedido_id = ? ORDER BY producto_id";
        List<LineaPedido> lineas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new LineaPedido(
                            rs.getInt("pedido_id"),
                            rs.getInt("producto_id"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("importe")));
                }
            }
        }
        return lineas;
    }

    @Override
    public BigDecimal calcularTotal(Connection conexion, int pedidoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(importe), 0) AS total FROM linea_pedido WHERE pedido_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }
}