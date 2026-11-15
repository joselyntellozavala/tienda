package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.BaseDatosPrueba;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.NuevoCliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("JdbcClienteDao")
class JdbcClienteDaoTest extends BaseDatosPrueba {

    @Test
    @DisplayName("Un cliente insertado se recupera con los mismos datos por id y por email")
    void insertarYBuscarDevuelveLosDatosGuardados() throws SQLException {
        NuevoCliente sara = new NuevoCliente("Sara", "Lozano Prieto", "sara.lozano@correo.es",
                LocalDate.of(2026, 11, 14));

        int id;
        try (Connection conexion = proveedor.getConnection()) {
            id = clienteDao.insertar(conexion, sara);
        }

        try (Connection conexion = proveedor.getConnection()) {
            Cliente esperado = new Cliente(id, "Sara", "Lozano Prieto", "sara.lozano@correo.es",
                    LocalDate.of(2026, 11, 14));
            assertEquals(esperado, clienteDao.buscarPorId(conexion, id));
            assertEquals(esperado, clienteDao.buscarPorEmail(conexion, "sara.lozano@correo.es"));
        }
    }

    @Test
    @DisplayName("Buscar un id que no existe devuelve null")
    void buscarPorIdInexistenteDevuelveNull() throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            assertNull(clienteDao.buscarPorId(conexion, -1));
        }
    }
}