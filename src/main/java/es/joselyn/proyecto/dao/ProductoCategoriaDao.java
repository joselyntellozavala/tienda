package es.joselyn.proyecto.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface ProductoCategoriaDao {

    int asignar(Connection conexion, int productoId, int categoriaId) throws SQLException;

    int quitar(Connection conexion, int productoId, int categoriaId) throws SQLException;
}