package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.EstadoPago;
import es.joselyn.proyecto.modelo.NuevoPago;
import es.joselyn.proyecto.modelo.Pago;

import java.sql.Connection;
import java.sql.SQLException;

public interface PagoDao {

    int insertar(Connection conexion, NuevoPago pago) throws SQLException;

    Pago buscarPorPedido(Connection conexion, int pedidoId) throws SQLException;

    int actualizarEstado(Connection conexion, int id, EstadoPago nuevoEstado) throws SQLException;
}