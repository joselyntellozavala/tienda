package es.joselyn.proyecto.dao;

import es.joselyn.proyecto.modelo.FiltroCatalogo;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.ProductoCatalogoDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ProductoDao {

    int insertar(Connection conexion, NuevoProducto producto) throws SQLException;

    Producto buscarPorId(Connection conexion, int id) throws SQLException;

    Producto buscarPorNombre(Connection conexion, String nombre) throws SQLException;

    int actualizarPrecio(Connection conexion, int id, BigDecimal nuevoPrecio) throws SQLException;

    int actualizarActivo(Connection conexion, int id, boolean activo) throws SQLException;

    List<ProductoCatalogoDTO> buscarCatalogo(Connection conexion, FiltroCatalogo filtro,
                                             int pagina, int tamanoPagina) throws SQLException;

    int contarCatalogo(Connection conexion, FiltroCatalogo filtro) throws SQLException;
}