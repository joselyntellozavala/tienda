package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.PedidoDao;
import es.joselyn.proyecto.modelo.DetallePedidoDTO;
import es.joselyn.proyecto.modelo.EstadoPago;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pedido;
import es.joselyn.proyecto.modelo.ResumenPedidoDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcPedidoDao implements PedidoDao {

    private static final Logger log = LogManager.getLogger(JdbcPedidoDao.class);

    @Override
    public int insertar(Connection conexion, NuevoPedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (cliente_id, direccion_id, fecha, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.clienteId());
            ps.setInt(2, pedido.direccionId());
            ps.setObject(3, pedido.fecha());
            ps.setString(4, pedido.estado().name());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "pedido");
            log.debug("Pedido insertado id={} cliente={}", id, pedido.clienteId());
            return id;
        }
    }

    @Override
    public Pedido buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = "SELECT id, cliente_id, direccion_id, fecha, estado FROM pedido WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pedido(
                            rs.getInt("id"),
                            rs.getInt("cliente_id"),
                            rs.getInt("direccion_id"),
                            rs.getObject("fecha", LocalDateTime.class),
                            EstadoPedido.valueOf(rs.getString("estado")));
                }
                return null;
            }
        }
    }

    @Override
    public int actualizarEstado(Connection conexion, int id, EstadoPedido nuevoEstado) throws SQLException {
        String sql = "UPDATE pedido SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Pedido id={} pasa a {} filas={}", id, nuevoEstado, filas);
            return filas;
        }
    }

    @Override
    public DetallePedidoDTO buscarDetalle(Connection conexion, int id) throws SQLException {
        String sql = "SELECT pe.id, pe.fecha, pe.estado,"
                + " CONCAT(c.nombre, ' ', c.apellidos) AS cliente, d.calle, d.ciudad,"
                + " pr.nombre AS producto, lp.cantidad, lp.precio_unitario, lp.importe,"
                + " (SELECT SUM(l2.importe) FROM linea_pedido l2 WHERE l2.pedido_id = pe.id) AS total"
                + " FROM pedido pe"
                + " JOIN cliente c ON c.id = pe.cliente_id"
                + " JOIN direccion d ON d.id = pe.direccion_id"
                + " JOIN linea_pedido lp ON lp.pedido_id = pe.id"
                + " JOIN producto pr ON pr.id = lp.producto_id"
                + " WHERE pe.id = ?"
                + " ORDER BY pr.nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                DetallePedidoDTO detalle = null;
                while (rs.next()) {
                    if (detalle == null) {
                        detalle = new DetallePedidoDTO(
                                rs.getInt("id"),
                                rs.getString("cliente"),
                                rs.getObject("fecha", LocalDateTime.class),
                                EstadoPedido.valueOf(rs.getString("estado")),
                                rs.getString("calle"),
                                rs.getString("ciudad"),
                                rs.getBigDecimal("total"),
                                new ArrayList<>());
                    }
                    detalle.lineas().add(new DetallePedidoDTO.Linea(
                            rs.getString("producto"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("importe")));
                }
                return detalle;
            }
        }
    }

    @Override
    public List<ResumenPedidoDTO> buscarHistorial(Connection conexion, int clienteId) throws SQLException {
        String sql = "SELECT pe.id, pe.fecha, pe.estado,"
                + " COUNT(lp.producto_id) AS productos,"
                + " SUM(lp.cantidad) AS unidades,"
                + " SUM(lp.importe) AS total,"
                + " pa.estado AS estado_pago"
                + " FROM pedido pe"
                + " JOIN linea_pedido lp ON lp.pedido_id = pe.id"
                + " LEFT JOIN pago pa ON pa.pedido_id = pe.id"
                + " WHERE pe.cliente_id = ?"
                + " GROUP BY pe.id, pe.fecha, pe.estado, pa.estado"
                + " ORDER BY pe.fecha DESC, pe.id DESC";
        List<ResumenPedidoDTO> historial = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estadoPago = rs.getString("estado_pago");
                    historial.add(new ResumenPedidoDTO(
                            rs.getInt("id"),
                            rs.getObject("fecha", LocalDateTime.class),
                            EstadoPedido.valueOf(rs.getString("estado")),
                            rs.getInt("productos"),
                            rs.getInt("unidades"),
                            rs.getBigDecimal("total"),
                            estadoPago == null ? null : EstadoPago.valueOf(estadoPago)));
                }
            }
        }
        return historial;
    }
}