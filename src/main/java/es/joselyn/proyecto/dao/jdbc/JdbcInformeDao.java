package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.dao.InformeDao;
import es.joselyn.proyecto.modelo.ProductoVendidoDTO;
import es.joselyn.proyecto.modelo.ResumenVentasProductoDTO;
import es.joselyn.proyecto.modelo.VentasArtesanoDTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcInformeDao implements InformeDao {

    @Override
    public List<ProductoVendidoDTO> buscarMasVendidos(Connection conexion, int limite) throws SQLException {
        String sql = "SELECT p.id, p.nombre,"
                + " SUM(lp.cantidad) AS unidades,"
                + " SUM(lp.importe) AS ingresos,"
                + " COUNT(lp.pedido_id) AS pedidos"
                + " FROM linea_pedido lp"
                + " JOIN pedido pe ON pe.id = lp.pedido_id"
                + " JOIN producto p ON p.id = lp.producto_id"
                + " WHERE pe.estado <> 'CANCELADO'"
                + " GROUP BY p.id, p.nombre"
                + " ORDER BY unidades DESC, ingresos DESC, p.id"
                + " LIMIT ?";
        List<ProductoVendidoDTO> productos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoVendidoDTO(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getInt("unidades"),
                            rs.getBigDecimal("ingresos"),
                            rs.getInt("pedidos")));
                }
            }
        }
        return productos;
    }

    @Override
    public List<VentasArtesanoDTO> buscarVentasPorArtesano(Connection conexion) throws SQLException {
        String sql = "SELECT a.nombre,"
                + " COALESCE(SUM(CASE WHEN v.tipo_pieza = 'UNICA' THEN v.cantidad END), 0) AS piezas_unicas,"
                + " COALESCE(SUM(CASE WHEN v.tipo_pieza = 'SERIE' THEN v.cantidad END), 0) AS unidades_serie,"
                + " COALESCE(SUM(v.importe), 0) AS ingresos"
                + " FROM artesano a"
                + " LEFT JOIN (SELECT p.artesano_id, p.tipo_pieza, lp.cantidad, lp.importe"
                + "            FROM linea_pedido lp"
                + "            JOIN pedido pe ON pe.id = lp.pedido_id"
                + "            JOIN producto p ON p.id = lp.producto_id"
                + "            WHERE pe.estado <> 'CANCELADO') v ON v.artesano_id = a.id"
                + " GROUP BY a.id, a.nombre"
                + " ORDER BY ingresos DESC, a.nombre";
        List<VentasArtesanoDTO> ventas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ventas.add(new VentasArtesanoDTO(
                        rs.getString("nombre"),
                        rs.getInt("piezas_unicas"),
                        rs.getInt("unidades_serie"),
                        rs.getBigDecimal("ingresos")));
            }
        }
        return ventas;
    }

    @Override
    public ResumenVentasProductoDTO resumenVentasProducto(Connection conexion, int productoId) throws SQLException {
        String sql = "{call resumen_ventas_producto(?, ?, ?)}";
        try (CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setInt(1, productoId);
            llamada.registerOutParameter(2, Types.INTEGER);
            llamada.registerOutParameter(3, Types.DECIMAL);
            llamada.execute();
            return new ResumenVentasProductoDTO(productoId, llamada.getInt(2), llamada.getBigDecimal(3));
        }
    }
}