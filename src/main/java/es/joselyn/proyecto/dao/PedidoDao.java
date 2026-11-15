package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.DetallePedidoDTO;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pedido;
import es.joselyn.proyecto.modelo.ResumenPedidoDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PedidoDao {

    int insertar(Connection conexion, NuevoPedido pedido) throws SQLException;

    Pedido buscarPorId(Connection conexion, int id) throws SQLException;

    int actualizarEstado(Connection conexion, int id, EstadoPedido nuevoEstado) throws SQLException;

    DetallePedidoDTO buscarDetalle(Connection conexion, int id) throws SQLException;

    List<ResumenPedidoDTO> buscarHistorial(Connection conexion, int clienteId) throws SQLException;
}