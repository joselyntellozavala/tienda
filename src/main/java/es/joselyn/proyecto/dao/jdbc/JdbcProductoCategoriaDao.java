package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.ProductoCategoriaDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcProductoCategoriaDao implements ProductoCategoriaDao {

    private static final Logger log = LogManager.getLogger(JdbcProductoCategoriaDao.class);

    @Override
    public int asignar(Connection conexion, int productoId, int categoriaId) throws SQLException {
        String sql = "INSERT INTO producto_categoria (producto_id, categoria_id) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setInt(2, categoriaId);
            int filas = ps.executeUpdate();
            log.debug("Categoría {} asignada a producto={}", categoriaId, productoId);
            return filas;
        }
    }

    @Override
    public int quitar(Connection conexion, int productoId, int categoriaId) throws SQLException {
        String sql = "DELETE FROM producto_categoria WHERE producto_id = ? AND categoria_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setInt(2, categoriaId);
            int filas = ps.executeUpdate();
            log.debug("Categoría {} quitada de producto={} filas={}", categoriaId, productoId, filas);
            return filas;
        }
    }
}