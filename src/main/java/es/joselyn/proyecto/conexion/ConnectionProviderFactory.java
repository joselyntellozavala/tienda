package es.joselyn.proyecto.conexion;

import es.joselyn.proyecto.config.AppConfig;

public final class ConnectionProviderFactory {

    private ConnectionProviderFactory(){}

    public static ConnectionProvider crear(AppConfig config) {
        return switch (config.modoConexion()) {
            case DRIVER_MANAGER -> new DriverManagerConnectionProvider(config.url(), config.usuario(), config.contrasena());
            case POOL -> new HikariConnectionProvider(config.url(), config.usuario(), config.contrasena(), config.maxConexionesPool(), config.esperaMaximaMs());
        };
    }
}
