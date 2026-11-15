package es.joselyn.proyecto;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.conexion.DriverManagerConnectionProvider;
import es.joselyn.proyecto.config.DatabaseEngine;
import es.joselyn.proyecto.config.SimulationMode;
import es.joselyn.proyecto.dao.ClienteDao;
import es.joselyn.proyecto.dao.DireccionDao;
import es.joselyn.proyecto.dao.InformeDao;
import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.dao.LineaPedidoDao;
import es.joselyn.proyecto.dao.MovimientoStockDao;
import es.joselyn.proyecto.dao.PagoDao;
import es.joselyn.proyecto.dao.PedidoDao;
import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcClienteDao;
import es.joselyn.proyecto.dao.jdbc.JdbcDireccionDao;
import es.joselyn.proyecto.dao.jdbc.JdbcInformeDao;
import es.joselyn.proyecto.dao.jdbc.JdbcInventarioDao;
import es.joselyn.proyecto.dao.jdbc.JdbcLineaPedidoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcMovimientoStockDao;
import es.joselyn.proyecto.dao.jdbc.JdbcPagoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcPedidoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcProductoDao;
import es.joselyn.proyecto.database.CargaInicial;
import es.joselyn.proyecto.database.InicializadorEsquema;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.LineaPedido;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.servicio.PedidoService;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class BaseDatosPrueba {

    private static final String URL = "jdbc:h2:mem:calmma_pruebas;DB_CLOSE_DELAY=-1";

    protected final ConnectionProvider proveedor = new DriverManagerConnectionProvider(URL, "sa", "");

    protected final ProductoDao productoDao = new JdbcProductoDao();
    protected final InventarioDao inventarioDao = new JdbcInventarioDao();
    protected final MovimientoStockDao movimientoStockDao = new JdbcMovimientoStockDao();
    protected final ClienteDao clienteDao = new JdbcClienteDao();
    protected final DireccionDao direccionDao = new JdbcDireccionDao();
    protected final PedidoDao pedidoDao = new JdbcPedidoDao();
    protected final LineaPedidoDao lineaPedidoDao = new JdbcLineaPedidoDao();
    protected final PagoDao pagoDao = new JdbcPagoDao();
    protected final InformeDao informeDao = new JdbcInformeDao();

    @BeforeEach
    protected void prepararBaseDatos() throws SQLException {
        new InicializadorEsquema(proveedor, DatabaseEngine.H2).reconstruir();
        new CargaInicial(proveedor).cargar();
    }

    protected PedidoService pedidoService(SimulationMode modo) {
        return new PedidoService(proveedor, clienteDao, direccionDao, productoDao, inventarioDao,
                pedidoDao, lineaPedidoDao, movimientoStockDao, pagoDao, modo);
    }

    protected Producto producto(String nombre) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return productoDao.buscarPorNombre(conexion, nombre);
        }
    }

    protected Cliente cliente(String email) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return clienteDao.buscarPorEmail(conexion, email);
        }
    }

    protected int direccionPrincipal(int clienteId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return direccionDao.buscarPorCliente(conexion, clienteId).getFirst().id();
        }
    }

    protected int stockDe(int productoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return inventarioDao.buscarPorProducto(conexion, productoId).stockActual();
        }
    }

    protected int contarFilas(String tabla) throws SQLException {
        try (Connection conexion = proveedor.getConnection();
             Statement sentencia = conexion.createStatement();
             ResultSet rs = sentencia.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    protected int insertarPedido(int clienteId, EstadoPedido estado, List<LineaSolicitada> lineas) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            int direccionId = direccionDao.buscarPorCliente(conexion, clienteId).getFirst().id();
            LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            int pedidoId = pedidoDao.insertar(conexion, new NuevoPedido(clienteId, direccionId, fecha, estado));
            for (LineaSolicitada linea : lineas) {
                Producto producto = productoDao.buscarPorId(conexion, linea.productoId());
                lineaPedidoDao.insertar(conexion,
                        LineaPedido.nueva(pedidoId, producto.id(), linea.cantidad(), producto.precio()));
            }
            return pedidoId;
        }
    }
}