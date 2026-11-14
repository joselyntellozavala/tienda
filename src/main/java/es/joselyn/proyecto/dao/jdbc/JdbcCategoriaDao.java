package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.CategoriaDao;
import es.joselyn.proyecto.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoriaDao implements CategoriaDao {

    @Override
    public List<Categoria> buscarTodas(Connection conexion) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
        }
        return categorias;
    }

    @Override
    public Categoria buscarPorNombre(Connection conexion, String nombre) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE nombre = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Categoria> buscarPorProducto(Connection conexion, int productoId) throws SQLException {
        String sql = "SELECT c.id, c.nombre FROM categoria c "
                + "JOIN producto_categoria pc ON pc.categoria_id = c.id "
                + "WHERE pc.producto_id = ? ORDER BY c.nombre";
        List<Categoria> categorias = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(mapear(rs));
                }
            }
        }
        return categorias;
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nombre"));
    }
}