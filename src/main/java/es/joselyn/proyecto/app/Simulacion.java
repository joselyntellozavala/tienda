package es.joselyn.proyecto.app;

import es.joselyn.proyecto.config.SimulationMode;
import es.joselyn.proyecto.excepcion.FalloSimuladoException;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Categoria;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.MetodoPago;
import es.joselyn.proyecto.modelo.NuevoCliente;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Pago;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.TipoPieza;
import es.joselyn.proyecto.servicio.CatalogoService;
import es.joselyn.proyecto.servicio.ClienteService;
import es.joselyn.proyecto.servicio.InventarioService;
import es.joselyn.proyecto.servicio.PedidoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Simulacion {

    private static final Logger log = LogManager.getLogger(Simulacion.class);

    private final CatalogoService catalogoService;
    private final ClienteService clienteService;
    private final InventarioService inventarioService;
    private final PedidoService pedidoService;

    public Simulacion(CatalogoService catalogoService, ClienteService clienteService,
                      InventarioService inventarioService, PedidoService pedidoService) {
        this.catalogoService = catalogoService;
        this.clienteService = clienteService;
        this.inventarioService = inventarioService;
        this.pedidoService = pedidoService;
    }

    public void ejecutar(SimulationMode modo) throws SQLException {
        if (modo == SimulationMode.FALLO_TRANSACCION) {
            demostrarRollback();
        } else {
            operacionesNormales();
        }
    }

    private void operacionesNormales() throws SQLException {
        Cliente ana = clienteService.buscarPorEmail("ana.morales@correo.es");
        Direccion casaAna = clienteService.direccionesDe(ana.id()).get(0);
        Producto tazaLuna = catalogoService.buscarProductoPorNombre("Taza Luna");
        Producto bowlMarea = catalogoService.buscarProductoPorNombre("Bowl Marea");
        Producto tazaBruma = catalogoService.buscarProductoPorNombre("Taza Bruma");
        Producto jarronEclipse = catalogoService.buscarProductoPorNombre("Jarrón Eclipse");

        log.info("=== Registro de un pedido ===");
        mostrarStock(tazaLuna);
        mostrarStock(bowlMarea);
        int pedidoId = pedidoService.registrarPedido(ana.id(), casaAna.id(), List.of(
                new LineaSolicitada(tazaLuna.id(), 2),
                new LineaSolicitada(bowlMarea.id(), 1)));
        log.info("Pedido {} en estado {}", pedidoId, pedidoService.buscarPedido(pedidoId).estado());
        mostrarStock(tazaLuna);
        mostrarStock(bowlMarea);

        log.info("=== Pago del pedido ===");
        pedidoService.pagarPedido(pedidoId, MetodoPago.TARJETA);
        mostrarPago(pedidoId);
        intentar("Pagar otra vez el pedido " + pedidoId,
                () -> pedidoService.pagarPedido(pedidoId, MetodoPago.BIZUM));

        log.info("=== Cancelación del pedido ===");
        pedidoService.cancelarPedido(pedidoId);
        log.info("Pedido {} en estado {}", pedidoId, pedidoService.buscarPedido(pedidoId).estado());
        mostrarPago(pedidoId);
        mostrarStock(tazaLuna);
        mostrarStock(bowlMarea);
        intentar("Cancelar otra vez el pedido " + pedidoId, () -> pedidoService.cancelarPedido(pedidoId));
        mostrarStock(tazaLuna);

        log.info("=== Stock insuficiente ===");
        intentar("Pedir 5 unidades de Taza Bruma", () -> pedidoService.registrarPedido(ana.id(), casaAna.id(),
                List.of(new LineaSolicitada(tazaBruma.id(), 5))));

        log.info("=== Reposición ===");
        inventarioService.reponerStock(tazaBruma.id(), 10);
        mostrarStock(tazaBruma);
        log.info("Movimientos de Taza Bruma: {}", inventarioService.movimientosDeProducto(tazaBruma.id()).size());
        intentar("Reponer una pieza única", () -> inventarioService.reponerStock(jarronEclipse.id(), 1));

        log.info("=== Catálogo ===");
        int cuencoId = catalogoService.registrarProducto(
                new NuevoProducto("Cuenco Brisa", tazaLuna.artesanoId(), new BigDecimal("17.50"), TipoPieza.SERIE, true),
                12, 4, List.of("Mesa", "Regalos"));
        List<String> categoriasCuenco = catalogoService.categoriasDe(cuencoId).stream()
                .map(Categoria::nombre)
                .toList();
        log.info("Cuenco Brisa id={} stock={} categorías={}", cuencoId,
                inventarioService.consultarStock(cuencoId).stockActual(), categoriasCuenco);
        catalogoService.cambiarPrecio(tazaLuna.id(), new BigDecimal("15.90"));
        log.info("Precio de Taza Luna: {}", catalogoService.buscarProductoPorNombre("Taza Luna").precio());

        log.info("=== Clientes ===");
        NuevoCliente sara = new NuevoCliente("Sara", "Lozano Prieto", "sara.lozano@correo.es", LocalDate.now());
        int saraId = clienteService.registrarCliente(sara, "Calle Alfonso I 5", "Zaragoza", "50003");
        log.info("Cliente {} registrado con {} dirección(es)", saraId, clienteService.direccionesDe(saraId).size());
        try {
            clienteService.registrarCliente(sara, "Calle Alfonso I 5", "Zaragoza", "50003");
        } catch (SQLException e) {
            log.warn("Violación de integridad: mensaje={} | SQLState={} | código={}",
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
        }
    }

    private void demostrarRollback() throws SQLException {
        Cliente diego = clienteService.buscarPorEmail("diego.navarro@correo.es");
        Direccion casaDiego = clienteService.direccionesDe(diego.id()).get(0);
        Producto tetera = catalogoService.buscarProductoPorNombre("Tetera Alba");
        Producto plato = catalogoService.buscarProductoPorNombre("Plato llano Arena");

        int teteraAntes = inventarioService.consultarStock(tetera.id()).stockActual();
        int platoAntes = inventarioService.consultarStock(plato.id()).stockActual();
        log.info("=== FALLO_TRANSACCION ===");
        log.info("Stock antes: {}={}, {}={}", tetera.nombre(), teteraAntes, plato.nombre(), platoAntes);

        int pedidoId;
        try {
            pedidoService.registrarPedido(diego.id(), casaDiego.id(), List.of(
                    new LineaSolicitada(tetera.id(), 2),
                    new LineaSolicitada(plato.id(), 3)));
            log.error("El modo FALLO_TRANSACCION no ha provocado el fallo");
            return;
        } catch (FalloSimuladoException e) {
            pedidoId = e.getPedidoId();
            log.info("Excepción recibida: {}", e.getMessage());
        }

        log.info("Comprobación en la base de datos del pedido {}", pedidoId);
        boolean pedidoBorrado = pedidoService.buscarPedido(pedidoId) == null;
        int lineas = pedidoService.lineasDe(pedidoId).size();
        int movimientos = inventarioService.movimientosDePedido(pedidoId).size();
        int teteraDespues = inventarioService.consultarStock(tetera.id()).stockActual();
        int platoDespues = inventarioService.consultarStock(plato.id()).stockActual();

        log.info("Pedido {} en la tabla pedido: {}", pedidoId, pedidoBorrado ? "no existe" : "EXISTE");
        log.info("Líneas del pedido {}: {}", pedidoId, lineas);
        log.info("Movimientos del pedido {}: {}", pedidoId, movimientos);
        log.info("Stock {}: antes={} después={}", tetera.nombre(), teteraAntes, teteraDespues);
        log.info("Stock {}: antes={} después={}", plato.nombre(), platoAntes, platoDespues);

        if (pedidoBorrado && lineas == 0 && movimientos == 0
                && teteraDespues == teteraAntes && platoDespues == platoAntes) {
            log.info("Rollback verificado: no quedan cambios parciales");
        } else {
            log.error("El rollback ha dejado cambios parciales");
        }
    }

    private void mostrarStock(Producto producto) throws SQLException {
        log.info("Stock de {}: {}", producto.nombre(), inventarioService.consultarStock(producto.id()).stockActual());
    }

    private void mostrarPago(int pedidoId) throws SQLException {
        Pago pago = pedidoService.pagoDe(pedidoId);
        log.info("Pago del pedido {}: {} {} € ({})", pedidoId, pago.metodo(), pago.importe(), pago.estado());
    }

    private void intentar(String descripcion, Operacion operacion) throws SQLException {
        try {
            operacion.ejecutar();
            log.error("{}: se esperaba un rechazo y la operación se ha realizado", descripcion);
        } catch (NegocioException e) {
            log.info("{}: rechazado. {}", descripcion, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws SQLException;
    }
}