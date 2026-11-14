package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.MovimientoStock;
import es.joselyn.proyecto.modelo.NuevoMovimientoStock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface MovimientoStockDao {

    int insertar(Connection conexion, NuevoMovimientoStock movimiento) throws SQLException;

    List<MovimientoStock> buscarPorProducto(Connection conexion, int productoId) throws SQLException;

    List<MovimientoStock> buscarPorPedido(Connection conexion, int pedidoId) throws SQLException;
}