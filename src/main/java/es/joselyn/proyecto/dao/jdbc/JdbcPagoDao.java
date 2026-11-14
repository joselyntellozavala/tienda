package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.PagoDao;
import es.joselyn.proyecto.modelo.EstadoPago;
import es.joselyn.proyecto.modelo.MetodoPago;
import es.joselyn.proyecto.modelo.NuevoPago;
import es.joselyn.proyecto.modelo.Pago;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class JdbcPagoDao implements PagoDao {

    private static final Logger log = LogManager.getLogger(JdbcPagoDao.class);

    @Override
    public int insertar(Connection conexion, NuevoPago pago) throws SQLException {
        String sql = "INSERT INTO pago (pedido_id, metodo, importe, estado, fecha) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.pedidoId());
            ps.setString(2, pago.metodo().name());
            ps.setBigDecimal(3, pago.importe());
            ps.setString(4, pago.estado().name());
            ps.setObject(5, pago.fecha());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "pago");
            log.debug("Pago insertado id={} pedido={} estado={}", id, pago.pedidoId(), pago.estado());
            return id;
        }
    }

    @Override
    public Pago buscarPorPedido(Connection conexion, int pedidoId) throws SQLException {
        String sql = "SELECT id, pedido_id, metodo, importe, estado, fecha FROM pago WHERE pedido_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pago(
                            rs.getInt("id"),
                            rs.getInt("pedido_id"),
                            MetodoPago.valueOf(rs.getString("metodo")),
                            rs.getBigDecimal("importe"),
                            EstadoPago.valueOf(rs.getString("estado")),
                            rs.getObject("fecha", LocalDateTime.class));
                }
                return null;
            }
        }
    }

    @Override
    public int actualizarEstado(Connection conexion, int id, EstadoPago nuevoEstado) throws SQLException {
        String sql = "UPDATE pago SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Pago id={} pasa a {} filas={}", id, nuevoEstado, filas);
            return filas;
        }
    }
}