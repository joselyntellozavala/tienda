package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.LineaPedido;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface LineaPedidoDao {

    int insertar(Connection conexion, LineaPedido linea) throws SQLException;

    List<LineaPedido> buscarPorPedido(Connection conexion, int pedidoId) throws SQLException;
}