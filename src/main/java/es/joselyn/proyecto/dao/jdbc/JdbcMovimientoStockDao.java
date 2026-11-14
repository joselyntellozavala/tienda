package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.MovimientoStockDao;
import es.joselyn.proyecto.modelo.MovimientoStock;
import es.joselyn.proyecto.modelo.NuevoMovimientoStock;
import es.joselyn.proyecto.modelo.TipoMovimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcMovimientoStockDao implements MovimientoStockDao {

    private static final Logger log = LogManager.getLogger(JdbcMovimientoStockDao.class);

    private static final String COLUMNAS = "SELECT id, producto_id, tipo, cantidad, fecha, pedido_id FROM movimiento_stock";

    @Override
    public int insertar(Connection conexion, NuevoMovimientoStock movimiento) throws SQLException {
        String sql = "INSERT INTO movimiento_stock (producto_id, tipo, cantidad, fecha, pedido_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, movimiento.productoId());
            ps.setString(2, movimiento.tipo().name());
            ps.setInt(3, movimiento.cantidad());
            ps.setObject(4, movimiento.fecha());
            if (movimiento.pedidoId() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, movimiento.pedidoId());
            }
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "movimiento_stock");
            log.debug("Movimiento insertado id={} tipo={} producto={} pedido={}",
                    id, movimiento.tipo(), movimiento.productoId(), movimiento.pedidoId());
            return id;
        }
    }

    @Override
    public List<MovimientoStock> buscarPorProducto(Connection conexion, int productoId) throws SQLException {
        return buscar(conexion, COLUMNAS + " WHERE producto_id = ? ORDER BY fecha, id", productoId);
    }

    @Override
    public List<MovimientoStock> buscarPorPedido(Connection conexion, int pedidoId) throws SQLException {
        return buscar(conexion, COLUMNAS + " WHERE pedido_id = ? ORDER BY fecha, id", pedidoId);
    }

    private List<MovimientoStock> buscar(Connection conexion, String sql, int parametro) throws SQLException {
        List<MovimientoStock> movimientos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapear(rs));
                }
            }
        }
        return movimientos;
    }

    private MovimientoStock mapear(ResultSet rs) throws SQLException {
        return new MovimientoStock(
                rs.getInt("id"),
                rs.getInt("producto_id"),
                TipoMovimiento.valueOf(rs.getString("tipo")),
                rs.getInt("cantidad"),
                rs.getObject("fecha", LocalDateTime.class),
                rs.getObject("pedido_id", Integer.class)
        );
    }
}