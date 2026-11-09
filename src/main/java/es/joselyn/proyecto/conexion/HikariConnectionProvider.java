package es.joselyn.proyecto.conexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class HikariConnectionProvider implements ConnectionProvider {

    private final HikariDataSource dataSource;

    public HikariConnectionProvider(String url, String usuario, String contrasena, int maxConexionesPool, long esperaMaximaMs) {

        HikariConfig configuracionPool = new HikariConfig();
        configuracionPool.setJdbcUrl(url);
        configuracionPool.setUsername(usuario);
        configuracionPool.setPassword(contrasena);
        configuracionPool.setMaximumPoolSize(maxConexionesPool);
        configuracionPool.setConnectionTimeout(esperaMaximaMs);
        configuracionPool.setPoolName("CalmmaPool");

        this.dataSource = new HikariDataSource(configuracionPool);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}