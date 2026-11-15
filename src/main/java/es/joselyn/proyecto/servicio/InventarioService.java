package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.dao.MovimientoStockDao;
import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Inventario;
import es.joselyn.proyecto.modelo.MovimientoStock;
import es.joselyn.proyecto.modelo.NuevoMovimientoStock;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.ProductoStockBajoDTO;
import es.joselyn.proyecto.modelo.TipoMovimiento;
import es.joselyn.proyecto.modelo.TipoPieza;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class InventarioService {

    private static final Logger log = LogManager.getLogger(InventarioService.class);

    private final ConnectionProvider proveedor;
    private final ProductoDao productoDao;
    private final InventarioDao inventarioDao;
    private final MovimientoStockDao movimientoStockDao;

    public InventarioService(ConnectionProvider proveedor, ProductoDao productoDao,
                             InventarioDao inventarioDao, MovimientoStockDao movimientoStockDao) {
        this.proveedor = proveedor;
        this.productoDao = productoDao;
        this.inventarioDao = inventarioDao;
        this.movimientoStockDao = movimientoStockDao;
    }

    public void reponerStock(int productoId, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad a reponer debe ser mayor que 0");
        }
        log.info("Inicio reposición producto={} cantidad={}", productoId, cantidad);

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                Producto producto = productoDao.buscarPorId(conexion, productoId);
                if (producto == null) {
                    throw new NegocioException("No existe el producto " + productoId);
                }
                if (producto.tipoPieza() == TipoPieza.UNICA) {
                    throw new NegocioException(producto.nombre() + " es una pieza única y no se puede reponer");
                }

                if (inventarioDao.incrementarStock(conexion, productoId, cantidad) == 0) {
                    throw new IllegalStateException("El producto " + productoId + " no tiene inventario");
                }
                LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                movimientoStockDao.insertar(conexion, new NuevoMovimientoStock(
                        productoId, TipoMovimiento.ENTRADA_REPOSICION, cantidad, fecha, null));

                conexion.commit();
                log.info("Reposición confirmada (commit) producto={} cantidad={}", productoId, cantidad);
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Reposición revertida (rollback) producto={}: {}", productoId, fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public Inventario consultarStock(int productoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return inventarioDao.buscarPorProducto(conexion, productoId);
        }
    }

    public List<MovimientoStock> movimientosDeProducto(int productoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return movimientoStockDao.buscarPorProducto(conexion, productoId);
        }
    }

    public List<MovimientoStock> movimientosDePedido(int pedidoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return movimientoStockDao.buscarPorPedido(conexion, pedidoId);
        }
    }

    public List<ProductoStockBajoDTO> productosConStockBajo() throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return inventarioDao.buscarStockBajo(conexion);
        }
    }
}
