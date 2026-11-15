package es.joselyn.proyecto.dao.jdbc;

import es.joselyn.proyecto.BaseDatosPrueba;
import es.joselyn.proyecto.modelo.DetallePedidoDTO;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.LineaPedido;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.NuevoPedido;
import es.joselyn.proyecto.modelo.Pedido;
import es.joselyn.proyecto.modelo.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JdbcPedidoDao")
class JdbcPedidoDaoTest extends BaseDatosPrueba {

    @Test
    @DisplayName("El id generado del pedido relaciona sus líneas")
    void elIdGeneradoRelacionaLasLineasConSuPedido() throws SQLException {
        int anaId = cliente("ana.morales@correo.es").id();
        int direccionId = direccionPrincipal(anaId);
        Producto tazaLuna = producto("Taza Luna");
        Producto bowlMarea = producto("Bowl Marea");
        LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        int pedidoId;
        try (Connection conexion = proveedor.getConnection()) {
            pedidoId = pedidoDao.insertar(conexion,
                    new NuevoPedido(anaId, direccionId, fecha, EstadoPedido.PENDIENTE));
            lineaPedidoDao.insertar(conexion, LineaPedido.nueva(pedidoId, tazaLuna.id(), 2, tazaLuna.precio()));
            lineaPedidoDao.insertar(conexion, LineaPedido.nueva(pedidoId, bowlMarea.id(), 1, bowlMarea.precio()));
        }

        assertTrue(pedidoId > 0);
        try (Connection conexion = proveedor.getConnection()) {
            assertEquals(new Pedido(pedidoId, anaId, direccionId, fecha, EstadoPedido.PENDIENTE),
                    pedidoDao.buscarPorId(conexion, pedidoId));

            List<LineaPedido> lineas = lineaPedidoDao.buscarPorPedido(conexion, pedidoId);
            assertEquals(2, lineas.size());
            for (LineaPedido linea : lineas) {
                assertEquals(pedidoId, linea.pedidoId());
            }
        }
    }

    @Test
    @DisplayName("El detalle de un pedido se mapea a un DTO con cabecera, líneas y total calculado en SQL")
    void buscarDetalleConstruyeElDto() throws SQLException {
        int anaId = cliente("ana.morales@correo.es").id();
        int pedidoId = insertarPedido(anaId, EstadoPedido.PENDIENTE, List.of(
                new LineaSolicitada(producto("Taza Luna").id(), 2),
                new LineaSolicitada(producto("Bowl Marea").id(), 1)));

        DetallePedidoDTO detalle;
        try (Connection conexion = proveedor.getConnection()) {
            detalle = pedidoDao.buscarDetalle(conexion, pedidoId);
        }

        assertEquals(pedidoId, detalle.pedidoId());
        assertEquals("Ana Morales Gil", detalle.cliente());
        assertEquals(EstadoPedido.PENDIENTE, detalle.estado());
        assertEquals("Calle Mayor 12", detalle.calle());
        assertEquals("Madrid", detalle.ciudad());
        assertEquals(new BigDecimal("45.80"), detalle.total());
        assertEquals(List.of(
                        new DetallePedidoDTO.Linea("Bowl Marea", 1, new BigDecimal("16.00"), new BigDecimal("16.00")),
                        new DetallePedidoDTO.Linea("Taza Luna", 2, new BigDecimal("14.90"), new BigDecimal("29.80"))),
                detalle.lineas());
    }
}