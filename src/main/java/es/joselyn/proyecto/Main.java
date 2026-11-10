package es.joselyn.proyecto;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.conexion.ConnectionProviderFactory;
import es.joselyn.proyecto.config.AppConfig;
import es.joselyn.proyecto.excepcion.ConfiguracionException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.cargar();
            System.out.println("Configuración cargada: " + config);

            try (ConnectionProvider proveedor = ConnectionProviderFactory.crear(config);
                 Connection conexion = proveedor.getConnection()) {

                DatabaseMetaData meta = conexion.getMetaData();
                System.out.println("Proveedor: " + proveedor.getClass().getSimpleName());
                System.out.println("Gestor:    " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                System.out.println("Driver:    " + meta.getDriverName() + " " + meta.getDriverVersion());
                System.out.println("URL:       " + meta.getURL());
                System.out.println("Conexión correcta.");
            }

        } catch (ConfiguracionException e) {
            System.err.println("Error de configuración: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState() + " | Código: " + e.getErrorCode());
        } catch (RuntimeException e) {
            System.err.println("No se pudo iniciar el pool: " + e.getMessage());
        }
    }
}