package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.NuevaDireccion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DireccionDao {

    int insertar(Connection conexion, NuevaDireccion direccion) throws SQLException;

    Direccion buscarPorId(Connection conexion, int id) throws SQLException;

    List<Direccion> buscarPorCliente(Connection conexion, int clienteId) throws SQLException;
}