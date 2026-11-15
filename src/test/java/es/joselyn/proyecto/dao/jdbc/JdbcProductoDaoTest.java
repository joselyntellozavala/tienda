package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.BaseDatosPrueba;
import es.joselyn.proyecto.modelo.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JdbcProductoDao")
class JdbcProductoDaoTest extends BaseDatosPrueba {

    @Test
    @DisplayName("Cambiar el precio modifica el valor guardado y solo ese campo")
    void actualizarPrecioCambiaElPrecioGuardado() throws SQLException {
        Producto antes = producto("Taza Luna");
        assertEquals(new BigDecimal("14.90"), antes.precio());

        int filas;
        try (Connection conexion = proveedor.getConnection()) {
            filas = productoDao.actualizarPrecio(conexion, antes.id(), new BigDecimal("15.90"));
        }

        assertEquals(1, filas);
        Producto esperado = new Producto(antes.id(), antes.nombre(), antes.artesanoId(),
                new BigDecimal("15.90"), antes.tipoPieza(), antes.activo());
        assertEquals(esperado, producto("Taza Luna"));
    }
}