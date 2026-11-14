package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pedido;

import java.sql.Connection;
import java.sql.SQLException;

public interface PedidoDao {

    int insertar(Connection conexion, NuevoPedido pedido) throws SQLException;

    Pedido buscarPorId(Connection conexion, int id) throws SQLException;

    int actualizarEstado(Connection conexion, int id, EstadoPedido nuevoEstado) throws SQLException;
}