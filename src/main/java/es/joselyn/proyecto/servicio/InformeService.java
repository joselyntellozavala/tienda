package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.config.DatabaseEngine;
import es.joselyn.proyecto.dao.InformeDao;
import es.joselyn.proyecto.modelo.ProductoVendidoDTO;
import es.joselyn.proyecto.modelo.ResumenVentasProductoDTO;
import es.joselyn.proyecto.modelo.VentasArtesanoDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InformeService {

    private static final Logger log = LogManager.getLogger(InformeService.class);

    private final ConnectionProvider proveedor;
    private final InformeDao informeDao;
    private final DatabaseEngine gestor;

    public InformeService(ConnectionProvider proveedor, InformeDao informeDao, DatabaseEngine gestor) {
        this.proveedor = proveedor;
        this.informeDao = informeDao;
        this.gestor = gestor;
    }

    public List<ProductoVendidoDTO> productosMasVendidos(int limite) throws SQLException {
        if (limite < 1) {
            throw new IllegalArgumentException("El número de productos debe ser mayor que 0");
        }
        try (Connection conexion = proveedor.getConnection()) {
            return informeDao.buscarMasVendidos(conexion, limite);
        }
    }

    public List<VentasArtesanoDTO> ventasPorArtesano() throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return informeDao.buscarVentasPorArtesano(conexion);
        }
    }

    public boolean procedimientoDisponible() {
        return gestor.getScriptProcedimientos() != null;
    }

    public ResumenVentasProductoDTO resumenVentasProducto(int productoId) throws SQLException {
        if (!procedimientoDisponible()) {
            throw new IllegalStateException("El procedimiento resumen_ventas_producto es específico de MySQL");
        }
        log.info("Llamada al procedimiento resumen_ventas_producto producto={}", productoId);
        try (Connection conexion = proveedor.getConnection()) {
            return informeDao.resumenVentasProducto(conexion, productoId);
        }
    }
}