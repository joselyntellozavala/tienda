package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.ClienteDao;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.ClienteConDireccionesDTO;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.NuevoCliente;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcClienteDao implements ClienteDao {

    private static final Logger log = LogManager.getLogger(JdbcClienteDao.class);

    private static final String COLUMNAS = "SELECT id, nombre, apellidos, email, fecha_alta FROM cliente";

    @Override
    public int insertar(Connection conexion, NuevoCliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, apellidos, email, fecha_alta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.nombre());
            ps.setString(2, cliente.apellidos());
            ps.setString(3, cliente.email());
            ps.setObject(4, cliente.fechaAlta());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "cliente");
            log.debug("Cliente insertado id={}", id);
            return id;
        }
    }

    @Override
    public Cliente buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = COLUMNAS + " WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Cliente buscarPorEmail(Connection conexion, String email) throws SQLException {
        String sql = COLUMNAS + " WHERE email = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Cliente> buscarTodos(Connection conexion) throws SQLException {
        String sql = COLUMNAS + " ORDER BY apellidos, nombre";
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        }
        return clientes;
    }

    @Override
    public ClienteConDireccionesDTO buscarConDirecciones(Connection conexion, int clienteId) throws SQLException {
        String sql = "SELECT c.id, c.nombre, c.apellidos, c.email, c.fecha_alta,"
                + " d.id AS direccion_id, d.calle, d.ciudad, d.codigo_postal, d.principal"
                + " FROM cliente c"
                + " LEFT JOIN direccion d ON d.cliente_id = c.id"
                + " WHERE c.id = ?"
                + " ORDER BY d.principal DESC, d.id";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                ClienteConDireccionesDTO resultado = null;
                while (rs.next()) {
                    if (resultado == null) {
                        resultado = new ClienteConDireccionesDTO(mapear(rs), new ArrayList<>());
                    }
                    Integer direccionId = rs.getObject("direccion_id", Integer.class);
                    if (direccionId != null) {
                        resultado.direcciones().add(new Direccion(
                                direccionId,
                                clienteId,
                                rs.getString("calle"),
                                rs.getString("ciudad"),
                                rs.getString("codigo_postal"),
                                rs.getBoolean("principal")));
                    }
                }
                return resultado;
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("email"),
                rs.getObject("fecha_alta", LocalDate.class)
        );
    }
}