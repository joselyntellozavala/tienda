package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.Inventario;
import es.joselyn.proyecto.modelo.ProductoStockBajoDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface InventarioDao {

    int insertar(Connection conexion, Inventario inventario) throws SQLException;

    Inventario buscarPorProducto(Connection conexion, int productoId) throws SQLException;

    int incrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException;

    int decrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException;

    List<ProductoStockBajoDTO> buscarStockBajo(Connection conexion) throws SQLException;
}