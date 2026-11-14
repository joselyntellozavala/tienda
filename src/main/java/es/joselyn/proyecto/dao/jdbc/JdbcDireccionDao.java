package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.DireccionDao;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.NuevaDireccion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcDireccionDao implements DireccionDao {

    private static final Logger log = LogManager.getLogger(JdbcDireccionDao.class);

    private static final String COLUMNAS = "SELECT id, cliente_id, calle, ciudad, codigo_postal, principal FROM direccion";

    @Override
    public int insertar(Connection conexion, NuevaDireccion direccion) throws SQLException {
        String sql = "INSERT INTO direccion (cliente_id, calle, ciudad, codigo_postal, principal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, direccion.clienteId());
            ps.setString(2, direccion.calle());
            ps.setString(3, direccion.ciudad());
            ps.setString(4, direccion.codigoPostal());
            ps.setBoolean(5, direccion.principal());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "direccion");
            log.debug("Dirección insertada id={} cliente={}", id, direccion.clienteId());
            return id;
        }
    }

    @Override
    public Direccion buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = COLUMNAS + " WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Direccion> buscarPorCliente(Connection conexion, int clienteId) throws SQLException {
        String sql = COLUMNAS + " WHERE cliente_id = ? ORDER BY principal DESC, id";
        List<Direccion> direcciones = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    direcciones.add(mapear(rs));
                }
            }
        }
        return direcciones;
    }

    private Direccion mapear(ResultSet rs) throws SQLException {
        return new Direccion(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getString("calle"),
                rs.getString("ciudad"),
                rs.getString("codigo_postal"),
                rs.getBoolean("principal")
        );
    }
}