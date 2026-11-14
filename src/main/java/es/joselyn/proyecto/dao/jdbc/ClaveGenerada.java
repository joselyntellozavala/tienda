package es.joselyn.proyecto.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ClaveGenerada {

    private ClaveGenerada() {
    }

    static int leer(PreparedStatement ps, String tabla) throws SQLException {
        try (ResultSet generadas = ps.getGeneratedKeys()) {
            if (generadas.next()) {
                return generadas.getInt(1);
            }
        }
        throw new SQLException("El gestor no devolvió el id generado al insertar en " + tabla);
    }
}