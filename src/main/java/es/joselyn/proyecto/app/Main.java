package es.joselyn.proyecto.app;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.conexion.ConnectionProviderFactory;
import es.joselyn.proyecto.config.AppConfig;
import es.joselyn.proyecto.dao.CategoriaDao;
import es.joselyn.proyecto.dao.ClienteDao;
import es.joselyn.proyecto.dao.DireccionDao;
import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.dao.LineaPedidoDao;
import es.joselyn.proyecto.dao.MovimientoStockDao;
import es.joselyn.proyecto.dao.PagoDao;
import es.joselyn.proyecto.dao.PedidoDao;
import es.joselyn.proyecto.dao.ProductoCategoriaDao;
import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcCategoriaDao;
import es.joselyn.proyecto.dao.jdbc.JdbcClienteDao;
import es.joselyn.proyecto.dao.jdbc.JdbcDireccionDao;
import es.joselyn.proyecto.dao.jdbc.JdbcInventarioDao;
import es.joselyn.proyecto.dao.jdbc.JdbcLineaPedidoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcMovimientoStockDao;
import es.joselyn.proyecto.dao.jdbc.JdbcPagoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcPedidoDao;
import es.joselyn.proyecto.dao.jdbc.JdbcProductoCategoriaDao;
import es.joselyn.proyecto.dao.jdbc.JdbcProductoDao;
import es.joselyn.proyecto.dao.InformeDao;
import es.joselyn.proyecto.dao.jdbc.JdbcInformeDao;
import es.joselyn.proyecto.servicio.InformeService;
import es.joselyn.proyecto.database.InicializadorEsquema;
import es.joselyn.proyecto.database.CargaInicial;
import es.joselyn.proyecto.excepcion.ConfiguracionException;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.servicio.CatalogoService;
import es.joselyn.proyecto.servicio.ClienteService;
import es.joselyn.proyecto.servicio.InventarioService;
import es.joselyn.proyecto.servicio.PedidoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Arranque de la aplicación");
        try {
            AppConfig config = AppConfig.cargar();
            log.info("Gestor={}, conexiones={}, simulacion={}", config.gestorBaseDatos(), config.modoConexion(), config.modoSimulacion());
            log.debug("Configuración completa: {}", config);

            try (ConnectionProvider proveedor = ConnectionProviderFactory.crear(config)) {

                try (Connection conexion = proveedor.getConnection()) {
                    DatabaseMetaData meta = conexion.getMetaData();
                    log.info("Conexión establecida con {} {} mediante {}",
                            meta.getDatabaseProductName(), meta.getDatabaseProductVersion(),
                            proveedor.getClass().getSimpleName());
                    log.debug("Driver: {} {} | URL: {}", meta.getDriverName(), meta.getDriverVersion(), meta.getURL());
                }

                new InicializadorEsquema(proveedor, config.gestorBaseDatos()).reconstruir();
                new CargaInicial(proveedor).cargar();

                ProductoDao productoDao = new JdbcProductoDao();
                CategoriaDao categoriaDao = new JdbcCategoriaDao();
                ProductoCategoriaDao productoCategoriaDao = new JdbcProductoCategoriaDao();
                InventarioDao inventarioDao = new JdbcInventarioDao();
                MovimientoStockDao movimientoStockDao = new JdbcMovimientoStockDao();
                ClienteDao clienteDao = new JdbcClienteDao();
                DireccionDao direccionDao = new JdbcDireccionDao();
                PedidoDao pedidoDao = new JdbcPedidoDao();
                LineaPedidoDao lineaPedidoDao = new JdbcLineaPedidoDao();
                PagoDao pagoDao = new JdbcPagoDao();
                InformeDao informeDao = new JdbcInformeDao();

                CatalogoService catalogoService = new CatalogoService(proveedor, productoDao, categoriaDao,
                        productoCategoriaDao, inventarioDao);
                ClienteService clienteService = new ClienteService(proveedor, clienteDao, direccionDao);
                InventarioService inventarioService = new InventarioService(proveedor, productoDao,
                        inventarioDao, movimientoStockDao);
                PedidoService pedidoService = new PedidoService(proveedor, clienteDao, direccionDao, productoDao,
                        inventarioDao, pedidoDao, lineaPedidoDao, movimientoStockDao, pagoDao,
                        config.modoSimulacion());
                InformeService informeService = new InformeService(proveedor, informeDao, config.gestorBaseDatos());

                new Simulacion(catalogoService, clienteService, inventarioService, pedidoService, informeService)
                        .ejecutar(config.modoSimulacion());
            }
            log.info("Fin de la aplicación");

        } catch (ConfiguracionException e) {
            log.error("Error de configuración: {}", e.getMessage());
        } catch (NegocioException e) {
            log.error("Operación rechazada sin tratar: {}", e.getMessage());
        } catch (SQLException e) {
            log.error("Error SQL: {} | SQLState={} | código={}",
                    e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        } catch (RuntimeException e) {
            log.error("Error de infraestructura: {}", e.getMessage(), e);
        }
    }
}