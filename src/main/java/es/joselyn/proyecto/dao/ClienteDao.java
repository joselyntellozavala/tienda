package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.ClienteConDireccionesDTO;
import es.joselyn.proyecto.modelo.NuevoCliente;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ClienteDao {

    int insertar(Connection conexion, NuevoCliente cliente) throws SQLException;

    Cliente buscarPorId(Connection conexion, int id) throws SQLException;

    Cliente buscarPorEmail(Connection conexion, String email) throws SQLException;

    List<Cliente> buscarTodos(Connection conexion) throws SQLException;

    ClienteConDireccionesDTO buscarConDirecciones(Connection conexion, int clienteId) throws SQLException;
}