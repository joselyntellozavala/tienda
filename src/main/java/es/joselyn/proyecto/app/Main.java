package es.joselyn.proyecto.app;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.conexion.ConnectionProviderFactory;
import es.joselyn.proyecto.config.AppConfig;
import es.joselyn.proyecto.database.InicializadorEsquema;
import es.joselyn.proyecto.database.CargaInicial;
import es.joselyn.proyecto.excepcion.ConfiguracionException;
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
            }
            log.info("Fin de la aplicación");

        } catch (ConfiguracionException e) {
            log.error("Error de configuración: {}", e.getMessage());
        } catch (SQLException e) {
            log.error("Error SQL: {} | SQLState={} | código={}",
                    e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        } catch (RuntimeException e) {
            log.error("Error de infraestructura al arrancar: {}", e.getMessage(), e);
        }
    }
}