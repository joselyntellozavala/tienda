package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.Categoria;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CategoriaDao {

    List<Categoria> buscarTodas(Connection conexion) throws SQLException;

    Categoria buscarPorNombre(Connection conexion, String nombre) throws SQLException;

    List<Categoria> buscarPorProducto(Connection conexion, int productoId) throws SQLException;
}