package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.BaseDatosPrueba;
import es.joselyn.proyecto.config.SimulationMode;
import es.joselyn.proyecto.excepcion.FalloSimuladoException;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.EstadoPago;
import es.joselyn.proyecto.modelo.EstadoPedido;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.MetodoPago;
import es.joselyn.proyecto.modelo.MovimientoStock;
import es.joselyn.proyecto.modelo.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PedidoService")
class PedidoServiceTest extends BaseDatosPrueba {

    private int anaId;
    private int direccionId;
    private int tazaLuna;
    private int bowlMarea;

    @BeforeEach
    void prepararDatosDelPedido() throws SQLException {
        anaId = cliente("ana.morales@correo.es").id();
        direccionId = direccionPrincipal(anaId);
        tazaLuna = producto("Taza Luna").id();
        bowlMarea = producto("Bowl Marea").id();
    }

    @Test
    @DisplayName("Commit: registrar un pedido confirma el pedido, sus líneas, el stock y los movimientos")
    void registrarPedidoConfirmaTodasLasModificaciones() throws SQLException {
        PedidoService servicio = pedidoService(SimulationMode.NORMAL);

        int pedidoId = servicio.registrarPedido(anaId, direccionId, List.of(
                new LineaSolicitada(tazaLuna, 2),
                new LineaSolicitada(bowlMarea, 1)));

        List<MovimientoStock> movimientos;
        try (Connection conexion = proveedor.getConnection()) {
            movimientos = movimientoStockDao.buscarPorPedido(conexion, pedidoId);
        }
        assertAll(
                () -> assertEquals(EstadoPedido.PENDIENTE, servicio.buscarPedido(pedidoId).estado()),
                () -> assertEquals(2, servicio.lineasDe(pedidoId).size()),
                () -> assertEquals(23, stockDe(tazaLuna)),
                () -> assertEquals(17, stockDe(bowlMarea)),
                () -> assertEquals(2, movimientos.size()),
                () -> assertTrue(movimientos.stream().allMatch(m -> m.tipo() == TipoMovimiento.SALIDA_PEDIDO)),
                () -> assertEquals(1, contarFilas("pedido")),
                () -> assertEquals(2, contarFilas("linea_pedido")),
                () -> assertEquals(2, contarFilas("movimiento_stock")));
    }

    @Test
    @DisplayName("Rollback: con FALLO_TRANSACCION no queda ninguna modificación del pedido")
    void falloTransaccionNoDejaCambiosParciales() throws SQLException {
        PedidoService servicio = pedidoService(SimulationMode.FALLO_TRANSACCION);
        int stockTazaAntes = stockDe(tazaLuna);
        int stockBowlAntes = stockDe(bowlMarea);

        FalloSimuladoException fallo = assertThrows(FalloSimuladoException.class,
                () -> servicio.registrarPedido(anaId, direccionId, List.of(
                        new LineaSolicitada(tazaLuna, 2),
                        new LineaSolicitada(bowlMarea, 1))));

        int pedidoId = fallo.getPedidoId();
        assertAll(
                () -> assertNull(servicio.buscarPedido(pedidoId)),
                () -> assertEquals(0, contarFilas("pedido")),
                () -> assertEquals(0, contarFilas("linea_pedido")),
                () -> assertEquals(0, contarFilas("movimiento_stock")),
                () -> assertEquals(stockTazaAntes, stockDe(tazaLuna)),
                () -> assertEquals(stockBowlAntes, stockDe(bowlMarea)));
    }

    @Test
    @DisplayName("Un pedido sin stock suficiente se rechaza y no modifica nada")
    void stockInsuficienteSeRechazaSinCambios() throws SQLException {
        PedidoService servicio = pedidoService(SimulationMode.NORMAL);
        int tazaBruma = producto("Taza Bruma").id();

        NegocioException rechazo = assertThrows(NegocioException.class,
                () -> servicio.registrarPedido(anaId, direccionId, List.of(
                        new LineaSolicitada(tazaLuna, 1),
                        new LineaSolicitada(tazaBruma, 5))));

        assertEquals("Stock insuficiente de Taza Bruma: pedidas 5, disponibles 4", rechazo.getMessage());
        assertEquals(0, contarFilas("pedido"));
        assertEquals(25, stockDe(tazaLuna));
        assertEquals(4, stockDe(tazaBruma));
    }

    @Test
    @DisplayName("Cancelar un pedido pagado devuelve el stock, reembolsa el pago y no lo repite")
    void cancelarDevuelveElStockUnaSolaVez() throws SQLException {
        PedidoService servicio = pedidoService(SimulationMode.NORMAL);
        int pedidoId = servicio.registrarPedido(anaId, direccionId, List.of(new LineaSolicitada(tazaLuna, 2)));
        servicio.pagarPedido(pedidoId, MetodoPago.TARJETA);

        servicio.cancelarPedido(pedidoId);

        assertEquals(EstadoPedido.CANCELADO, servicio.buscarPedido(pedidoId).estado());
        assertEquals(EstadoPago.REEMBOLSADO, servicio.pagoDe(pedidoId).estado());
        assertEquals(new BigDecimal("29.80"), servicio.pagoDe(pedidoId).importe());
        assertEquals(25, stockDe(tazaLuna));

        assertThrows(NegocioException.class, () -> servicio.cancelarPedido(pedidoId));
        assertEquals(25, stockDe(tazaLuna));
        assertEquals(2, contarFilas("movimiento_stock"));
    }
}