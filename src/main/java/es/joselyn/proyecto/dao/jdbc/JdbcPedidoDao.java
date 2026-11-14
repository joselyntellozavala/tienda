package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.PedidoDao;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pedido;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

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
}