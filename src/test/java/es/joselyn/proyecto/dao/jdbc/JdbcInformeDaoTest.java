package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.BaseDatosPrueba;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.ProductoVendidoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JdbcInformeDao")
class JdbcInformeDaoTest extends BaseDatosPrueba {

    @Test
    @DisplayName("El resumen comercial agrupa las ventas y no cuenta los pedidos cancelados")
    void buscarMasVendidosAgrupaYExcluyeCancelados() throws SQLException {
        int tazaLuna = producto("Taza Luna").id();
        int bowlMarea = producto("Bowl Marea").id();
        int platoLlano = producto("Plato llano Arena").id();

        insertarPedido(cliente("ana.morales@correo.es").id(), EstadoPedido.PAGADO, List.of(
                new LineaSolicitada(tazaLuna, 3),
                new LineaSolicitada(bowlMarea, 1)));
        insertarPedido(cliente("carmen.soto@correo.es").id(), EstadoPedido.PENDIENTE, List.of(
                new LineaSolicitada(tazaLuna, 2)));
        insertarPedido(cliente("hugo.martin@correo.es").id(), EstadoPedido.CANCELADO, List.of(
                new LineaSolicitada(platoLlano, 5)));

        List<ProductoVendidoDTO> resumen;
        try (Connection conexion = proveedor.getConnection()) {
            resumen = informeDao.buscarMasVendidos(conexion, 5);
        }

        assertEquals(List.of(
                        new ProductoVendidoDTO(tazaLuna, "Taza Luna", 5, new BigDecimal("74.50"), 2),
                        new ProductoVendidoDTO(bowlMarea, "Bowl Marea", 1, new BigDecimal("16.00"), 1)),
                resumen);
    }
}