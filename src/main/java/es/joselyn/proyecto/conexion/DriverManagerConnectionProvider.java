package es.joselyn.proyecto.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DriverManagerConnectionProvider implements ConnectionProvider {

    private final String url;
    private final String usuario;
    private final String contrasena;

    public DriverManagerConnectionProvider(String url, String usuario, String contrasena) {
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    @Override
    public void close() {

    }
}
