package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.modelo.FiltroCatalogo;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.ProductoCatalogoDTO;
import es.joselyn.proyecto.modelo.TipoPieza;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcProductoDao implements ProductoDao {

    private static final Logger log = LogManager.getLogger(JdbcProductoDao.class);

    private static final String COLUMNAS = "SELECT id, nombre, artesano_id, precio, tipo_pieza, activo FROM producto";

    private static final String FROM_CATALOGO = " FROM producto p"
            + " JOIN artesano a ON a.id = p.artesano_id"
            + " JOIN inventario i ON i.producto_id = p.id"
            + " WHERE p.activo = TRUE";

    @Override
    public int insertar(Connection conexion, NuevoProducto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombre, artesano_id, precio, tipo_pieza, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.nombre());
            ps.setInt(2, producto.artesanoId());
            ps.setBigDecimal(3, producto.precio());
            ps.setString(4, producto.tipoPieza().name());
            ps.setBoolean(5, producto.activo());
            ps.executeUpdate();
            int id = ClaveGenerada.leer(ps, "producto");
            log.debug("Producto insertado id={}", id);
            return id;
        }
    }

    @Override
    public Producto buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = COLUMNAS + " WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Producto buscarPorNombre(Connection conexion, String nombre) throws SQLException {
        String sql = COLUMNAS + " WHERE nombre = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public int actualizarPrecio(Connection conexion, int id, BigDecimal nuevoPrecio) throws SQLException {
        String sql = "UPDATE producto SET precio = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoPrecio);
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Precio actualizado producto={} filas={}", id, filas);
            return filas;
        }
    }

    @Override
    public int actualizarActivo(Connection conexion, int id, boolean activo) throws SQLException {
        String sql = "UPDATE producto SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            int filas = ps.executeUpdate();
            log.debug("Activo={} producto={} filas={}", activo, id, filas);
            return filas;
        }
    }

    @Override
    public List<ProductoCatalogoDTO> buscarCatalogo(Connection conexion, FiltroCatalogo filtro,
                                                    int pagina, int tamanoPagina) throws SQLException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, a.nombre AS artesano, p.precio, p.tipo_pieza, i.stock_actual"
                + FROM_CATALOGO
                + condiciones(filtro, parametros)
                + " ORDER BY p.precio, p.id LIMIT ? OFFSET ?";
        parametros.add(tamanoPagina);
        parametros.add(Math.multiplyExact(pagina - 1, tamanoPagina));

        List<ProductoCatalogoDTO> productos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoCatalogoDTO(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("artesano"),
                            rs.getBigDecimal("precio"),
                            TipoPieza.valueOf(rs.getString("tipo_pieza")),
                            rs.getInt("stock_actual")));
                }
            }
        }
        return productos;
    }

    @Override
    public int contarCatalogo(Connection conexion, FiltroCatalogo filtro) throws SQLException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS total" + FROM_CATALOGO + condiciones(filtro, parametros);
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    // Añade al WHERE solo los criterios que tiene el filtro. Los valores nunca se concatenan:
    // cada uno se guarda en la lista de parámetros y se envía con su ?.
    private String condiciones(FiltroCatalogo filtro, List<Object> parametros) {
        StringBuilder where = new StringBuilder();
        if (filtro.categoria() != null) {
            where.append(" AND EXISTS (SELECT 1 FROM producto_categoria pc")
                    .append(" JOIN categoria c ON c.id = pc.categoria_id")
                    .append(" WHERE pc.producto_id = p.id AND c.nombre = ?)");
            parametros.add(filtro.categoria());
        }
        if (filtro.precioMinimo() != null) {
            where.append(" AND p.precio >= ?");
            parametros.add(filtro.precioMinimo());
        }
        if (filtro.precioMaximo() != null) {
            where.append(" AND p.precio <= ?");
            parametros.add(filtro.precioMaximo());
        }
        if (filtro.soloDisponibles()) {
            where.append(" AND i.stock_actual > 0");
        }
        return where.toString();
    }

    private void asignarParametros(PreparedStatement ps, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            ps.setObject(i + 1, parametros.get(i));
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getInt("artesano_id"),
                rs.getBigDecimal("precio"),
                TipoPieza.valueOf(rs.getString("tipo_pieza")),
                rs.getBoolean("activo")
        );
    }
}