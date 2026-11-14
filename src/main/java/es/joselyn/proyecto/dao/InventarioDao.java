package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.Inventario;

import java.sql.Connection;
import java.sql.SQLException;

public interface InventarioDao {

    int insertar(Connection conexion, Inventario inventario) throws SQLException;

    Inventario buscarPorProducto(Connection conexion, int productoId) throws SQLException;

    int incrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException;

    int decrementarStock(Connection conexion, int productoId, int cantidad) throws SQLException;
}