package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.ProductoVendidoDTO;
import es.joselyn.proyecto.modelo.ResumenVentasProductoDTO;
import es.joselyn.proyecto.modelo.VentasArtesanoDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface InformeDao {

    List<ProductoVendidoDTO> buscarMasVendidos(Connection conexion, int limite) throws SQLException;

    List<VentasArtesanoDTO> buscarVentasPorArtesano(Connection conexion) throws SQLException;

    ResumenVentasProductoDTO resumenVentasProducto(Connection conexion, int productoId) throws SQLException;
}