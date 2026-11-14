package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.config.SimulationMode;
import es.joselyn.proyecto.dao.ClienteDao;
import es.joselyn.proyecto.dao.DireccionDao;
import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.dao.LineaPedidoDao;
import es.joselyn.proyecto.dao.MovimientoStockDao;
import es.joselyn.proyecto.dao.PagoDao;
import es.joselyn.proyecto.dao.PedidoDao;
import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.excepcion.FalloSimuladoException;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.EstadoPago;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.Inventario;
import es.joselyn.proyecto.modelo.LineaPedido;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.MetodoPago;
import es.joselyn.proyecto.modelo.NuevoMovimientoStock;
import es.joselyn.proyecto.modelo.NuevoPago;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pago;
import es.joselyn.proyecto.modelo.Pedido;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.TipoMovimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PedidoService {

    private static final Logger log = LogManager.getLogger(PedidoService.class);

    private final ConnectionProvider proveedor;
    private final ClienteDao clienteDao;
    private final DireccionDao direccionDao;
    private final ProductoDao productoDao;
    private final InventarioDao inventarioDao;
    private final PedidoDao pedidoDao;
    private final LineaPedidoDao lineaPedidoDao;
    private final MovimientoStockDao movimientoStockDao;
    private final PagoDao pagoDao;
    private final SimulationMode modoSimulacion;

    public PedidoService(ConnectionProvider proveedor, ClienteDao clienteDao, DireccionDao direccionDao,
                         ProductoDao productoDao, InventarioDao inventarioDao, PedidoDao pedidoDao,
                         LineaPedidoDao lineaPedidoDao, MovimientoStockDao movimientoStockDao,
                         PagoDao pagoDao, SimulationMode modoSimulacion) {
        this.proveedor = proveedor;
        this.clienteDao = clienteDao;
        this.direccionDao = direccionDao;
        this.productoDao = productoDao;
        this.inventarioDao = inventarioDao;
        this.pedidoDao = pedidoDao;
        this.lineaPedidoDao = lineaPedidoDao;
        this.movimientoStockDao = movimientoStockDao;
        this.pagoDao = pagoDao;
        this.modoSimulacion = modoSimulacion;
    }

    public int registrarPedido(int clienteId, int direccionId, List<LineaSolicitada> lineas) throws SQLException {
        comprobarLineasSolicitadas(lineas);
        log.info("Inicio pedido cliente={} lineas={}", clienteId, lineas.size());

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                comprobarClienteYDireccion(conexion, clienteId, direccionId);
                List<Producto> productos = comprobarProductosYStock(conexion, lineas);

                LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                int pedidoId = pedidoDao.insertar(conexion,
                        new NuevoPedido(clienteId, direccionId, fecha, EstadoPedido.PENDIENTE));

                for (int i = 0; i < lineas.size(); i++) {
                    LineaSolicitada linea = lineas.get(i);
                    Producto producto = productos.get(i);

                    lineaPedidoDao.insertar(conexion,
                            LineaPedido.nueva(pedidoId, producto.id(), linea.cantidad(), producto.precio()));
                    if (inventarioDao.decrementarStock(conexion, producto.id(), linea.cantidad()) == 0) {
                        throw new NegocioException("Stock insuficiente de " + producto.nombre());
                    }
                    movimientoStockDao.insertar(conexion, new NuevoMovimientoStock(
                            producto.id(), TipoMovimiento.SALIDA_PEDIDO, linea.cantidad(), fecha, pedidoId));
                }

                if (modoSimulacion == SimulationMode.FALLO_TRANSACCION) {
                    log.warn("Fallo controlado; se revierte pedido={}", pedidoId);
                    throw new FalloSimuladoException(pedidoId);
                }

                conexion.commit();
                log.info("Pedido registrado id={} (commit)", pedidoId);
                return pedidoId;
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Registro de pedido revertido (rollback): {}", fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public int pagarPedido(int pedidoId, MetodoPago metodo) throws SQLException {
        log.info("Inicio pago pedido={} metodo={}", pedidoId, metodo);

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                Pedido pedido = pedidoDao.buscarPorId(conexion, pedidoId);
                if (pedido == null) {
                    throw new NegocioException("No existe el pedido " + pedidoId);
                }
                if (pedido.estado() != EstadoPedido.PENDIENTE) {
                    throw new NegocioException("El pedido " + pedidoId + " no se puede pagar porque está " + pedido.estado());
                }

                BigDecimal total = lineaPedidoDao.calcularTotal(conexion, pedidoId);
                LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                int pagoId = pagoDao.insertar(conexion,
                        new NuevoPago(pedidoId, metodo, total, EstadoPago.COMPLETADO, fecha));
                pedidoDao.actualizarEstado(conexion, pedidoId, EstadoPedido.PAGADO);

                conexion.commit();
                log.info("Pedido pagado id={} pago={} (commit)", pedidoId, pagoId);
                return pagoId;
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Pago revertido (rollback) pedido={}: {}", pedidoId, fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public void cancelarPedido(int pedidoId) throws SQLException {
        log.info("Inicio cancelación pedido={}", pedidoId);

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                Pedido pedido = pedidoDao.buscarPorId(conexion, pedidoId);
                if (pedido == null) {
                    throw new NegocioException("No existe el pedido " + pedidoId);
                }
                if (pedido.estado() == EstadoPedido.CANCELADO) {
                    throw new NegocioException("El pedido " + pedidoId + " ya está cancelado; no se devuelve el stock otra vez");
                }

                LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                for (LineaPedido linea : lineaPedidoDao.buscarPorPedido(conexion, pedidoId)) {
                    inventarioDao.incrementarStock(conexion, linea.productoId(), linea.cantidad());
                    movimientoStockDao.insertar(conexion, new NuevoMovimientoStock(
                            linea.productoId(), TipoMovimiento.DEVOLUCION_CANCELACION, linea.cantidad(), fecha, pedidoId));
                }

                if (pedido.estado() == EstadoPedido.PAGADO) {
                    Pago pago = pagoDao.buscarPorPedido(conexion, pedidoId);
                    if (pago == null) {
                        throw new IllegalStateException("El pedido " + pedidoId + " está PAGADO pero no tiene pago");
                    }
                    pagoDao.actualizarEstado(conexion, pago.id(), EstadoPago.REEMBOLSADO);
                }
                pedidoDao.actualizarEstado(conexion, pedidoId, EstadoPedido.CANCELADO);

                conexion.commit();
                log.info("Pedido cancelado id={} (commit)", pedidoId);
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Cancelación revertida (rollback) pedido={}: {}", pedidoId, fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public Pedido buscarPedido(int pedidoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return pedidoDao.buscarPorId(conexion, pedidoId);
        }
    }

    public List<LineaPedido> lineasDe(int pedidoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return lineaPedidoDao.buscarPorPedido(conexion, pedidoId);
        }
    }

    public Pago pagoDe(int pedidoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return pagoDao.buscarPorPedido(conexion, pedidoId);
        }
    }

    private void comprobarLineasSolicitadas(List<LineaSolicitada> lineas) {
        if (lineas.isEmpty()) {
            throw new NegocioException("Un pedido debe tener al menos un producto");
        }
        Set<Integer> productos = new HashSet<>();
        for (LineaSolicitada linea : lineas) {
            if (linea.cantidad() <= 0) {
                throw new NegocioException("La cantidad debe ser mayor que 0");
            }
            if (!productos.add(linea.productoId())) {
                throw new NegocioException("El producto " + linea.productoId() + " aparece en dos líneas del pedido");
            }
        }
    }

    private void comprobarClienteYDireccion(Connection conexion, int clienteId, int direccionId) throws SQLException {
        if (clienteDao.buscarPorId(conexion, clienteId) == null) {
            throw new NegocioException("No existe el cliente " + clienteId);
        }
        Direccion direccion = direccionDao.buscarPorId(conexion, direccionId);
        if (direccion == null || direccion.clienteId() != clienteId) {
            throw new NegocioException("La dirección " + direccionId + " no pertenece al cliente " + clienteId);
        }
    }

    private List<Producto> comprobarProductosYStock(Connection conexion, List<LineaSolicitada> lineas) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        for (LineaSolicitada linea : lineas) {
            Producto producto = productoDao.buscarPorId(conexion, linea.productoId());
            if (producto == null) {
                throw new NegocioException("No existe el producto " + linea.productoId());
            }
            if (!producto.activo()) {
                throw new NegocioException(producto.nombre() + " está retirado del catálogo");
            }
            Inventario inventario = inventarioDao.buscarPorProducto(conexion, producto.id());
            if (inventario.stockActual() < linea.cantidad()) {
                throw new NegocioException("Stock insuficiente de " + producto.nombre() + ": pedidas "
                        + linea.cantidad() + ", disponibles " + inventario.stockActual());
            }
            productos.add(producto);
        }
        return productos;
    }
}