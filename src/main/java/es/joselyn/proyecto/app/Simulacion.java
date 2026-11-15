package es.joselyn.proyecto.app;

import es.joselyn.proyecto.config.SimulationMode;
import es.joselyn.proyecto.excepcion.FalloSimuladoException;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Categoria;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.ClienteConDireccionesDTO;
import es.joselyn.proyecto.modelo.DetallePedidoDTO;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.FiltroCatalogo;
import es.joselyn.proyecto.modelo.LineaSolicitada;
import es.joselyn.proyecto.modelo.MetodoPago;
import es.joselyn.proyecto.modelo.MovimientoStock;
import es.joselyn.proyecto.modelo.NuevoCliente;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Pago;
import es.joselyn.proyecto.modelo.PaginaCatalogoDTO;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.ProductoCatalogoDTO;
import es.joselyn.proyecto.modelo.ProductoStockBajoDTO;
import es.joselyn.proyecto.modelo.ProductoVendidoDTO;
import es.joselyn.proyecto.modelo.ResumenPedidoDTO;
import es.joselyn.proyecto.modelo.ResumenVentasProductoDTO;
import es.joselyn.proyecto.modelo.TipoPieza;
import es.joselyn.proyecto.modelo.VentasArtesanoDTO;
import es.joselyn.proyecto.servicio.CatalogoService;
import es.joselyn.proyecto.servicio.ClienteService;
import es.joselyn.proyecto.servicio.InformeService;
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
    private final InformeService informeService;

    public Simulacion(CatalogoService catalogoService, ClienteService clienteService,
                      InventarioService inventarioService, PedidoService pedidoService,
                      InformeService informeService) {
        this.catalogoService = catalogoService;
        this.clienteService = clienteService;
        this.inventarioService = inventarioService;
        this.pedidoService = pedidoService;
        this.informeService = informeService;
    }

    public void ejecutar(SimulationMode modo) throws SQLException {
        if (modo == SimulationMode.FALLO_TRANSACCION) {
            demostrarRollback();
        } else {
            int pedidoCancelado = operacionesNormales();
            int pedidoPagado = pedidosParaConsultas();
            consultasDeGestion(pedidoCancelado, pedidoPagado);
        }
    }

    private int operacionesNormales() throws SQLException {
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
        return pedidoId;
    }

    private int pedidosParaConsultas() throws SQLException {
        log.info("=== Más pedidos para las consultas ===");
        Cliente carmen = clienteService.buscarPorEmail("carmen.soto@correo.es");
        Cliente hugo = clienteService.buscarPorEmail("hugo.martin@correo.es");
        Cliente ana = clienteService.buscarPorEmail("ana.morales@correo.es");

        int pedidoCarmen = pedidoService.registrarPedido(carmen.id(), direccionPrincipal(carmen), List.of(
                new LineaSolicitada(idDe("Taza Luna"), 3),
                new LineaSolicitada(idDe("Escultura Ola"), 1),
                new LineaSolicitada(idDe("Portavelas Duna"), 2)));
        pedidoService.pagarPedido(pedidoCarmen, MetodoPago.BIZUM);

        int pedidoHugo = pedidoService.registrarPedido(hugo.id(), direccionPrincipal(hugo), List.of(
                new LineaSolicitada(idDe("Plato llano Arena"), 4),
                new LineaSolicitada(idDe("Bowl Marea"), 2)));
        pedidoService.pagarPedido(pedidoHugo, MetodoPago.TARJETA);

        pedidoService.registrarPedido(ana.id(), direccionPrincipal(ana), List.of(
                new LineaSolicitada(idDe("Taza Bruma"), 2),
                new LineaSolicitada(idDe("Jarra Olivo"), 1)));
        return pedidoCarmen;
    }

    private void consultasDeGestion(int pedidoCancelado, int pedidoPagado) throws SQLException {
        log.info("=== Catálogo filtrado y paginado ===");
        mostrarPagina("Categoría Café y té",
                catalogoService.consultarCatalogo(new FiltroCatalogo("Café y té", null, null, false), 2, 2));
        mostrarPagina("Entre 15 y 100 €, solo disponibles",
                catalogoService.consultarCatalogo(
                        new FiltroCatalogo(null, new BigDecimal("15"), new BigDecimal("100"), true), 1, 5));

        log.info("=== Detalle de pedido ===");
        mostrarDetalle(pedidoService.detalleDe(pedidoCancelado));
        mostrarDetalle(pedidoService.detalleDe(pedidoPagado));

        log.info("=== Historial de cliente ===");
        Cliente ana = clienteService.buscarPorEmail("ana.morales@correo.es");
        List<ResumenPedidoDTO> historial = pedidoService.historialDe(ana.id());
        log.info("{} {}: {} pedido(s)", ana.nombre(), ana.apellidos(), historial.size());
        for (ResumenPedidoDTO pedido : historial) {
            log.info("  Pedido {} · {} · {} · {} producto(s), {} unidad(es) · {} € · pago: {}",
                    pedido.pedidoId(), pedido.fecha(), pedido.estado(), pedido.productos(),
                    pedido.unidades(), pedido.total(), pedido.estadoPago() == null ? "sin pago" : pedido.estadoPago());
        }

        log.info("=== Resumen comercial: productos más vendidos ===");
        for (ProductoVendidoDTO producto : informeService.productosMasVendidos(5)) {
            log.info("  {}: {} unidad(es) en {} pedido(s) · {} €",
                    producto.nombre(), producto.unidades(), producto.pedidos(), producto.ingresos());
        }

        log.info("=== Control de existencias ===");
        for (ProductoStockBajoDTO producto : inventarioService.productosConStockBajo()) {
            log.info("  {}: stock {} / mínimo {} → reponer {}",
                    producto.nombre(), producto.stockActual(), producto.stockMinimo(), producto.unidadesAReponer());
        }
        Producto tazaBruma = catalogoService.buscarProductoPorNombre("Taza Bruma");
        log.info("Movimientos de {}:", tazaBruma.nombre());
        for (MovimientoStock movimiento : inventarioService.movimientosDeProducto(tazaBruma.id())) {
            log.info("  {} {} · {} · pedido {}", movimiento.tipo(), movimiento.cantidad(), movimiento.fecha(),
                    movimiento.pedidoId() == null ? "-" : movimiento.pedidoId());
        }

        log.info("=== Consulta propia: ventas por artesano ===");
        for (VentasArtesanoDTO ventas : informeService.ventasPorArtesano()) {
            log.info("  {}: {} pieza(s) única(s), {} unidad(es) de serie · {} €",
                    ventas.artesano(), ventas.piezasUnicas(), ventas.unidadesSerie(), ventas.ingresos());
        }

        log.info("=== Cliente con sus direcciones ===");
        Cliente carmen = clienteService.buscarPorEmail("carmen.soto@correo.es");
        ClienteConDireccionesDTO carmenConDirecciones = clienteService.buscarConDirecciones(carmen.id());
        log.info("{} {} ({} dirección(es))", carmenConDirecciones.cliente().nombre(),
                carmenConDirecciones.cliente().apellidos(), carmenConDirecciones.direcciones().size());
        for (Direccion direccion : carmenConDirecciones.direcciones()) {
            log.info("  {}, {} {}{}", direccion.calle(), direccion.codigoPostal(), direccion.ciudad(),
                    direccion.principal() ? " (principal)" : "");
        }

        log.info("=== Procedimiento almacenado ===");
        Producto tazaLuna = catalogoService.buscarProductoPorNombre("Taza Luna");
        if (informeService.procedimientoDisponible()) {
            ResumenVentasProductoDTO resumen = informeService.resumenVentasProducto(tazaLuna.id());
            log.info("resumen_ventas_producto({}): unidades={} ingresos={} €",
                    tazaLuna.nombre(), resumen.unidades(), resumen.ingresos());
        } else {
            log.info("resumen_ventas_producto es específico de MySQL: con este gestor no se ejecuta");
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

    private int idDe(String nombreProducto) throws SQLException {
        return catalogoService.buscarProductoPorNombre(nombreProducto).id();
    }

    private int direccionPrincipal(Cliente cliente) throws SQLException {
        return clienteService.direccionesDe(cliente.id()).get(0).id();
    }

    private void mostrarPagina(String descripcion, PaginaCatalogoDTO pagina) {
        log.info("{}: página {} de {} ({} productos)", descripcion, pagina.pagina(), pagina.totalPaginas(),
                pagina.totalProductos());
        for (ProductoCatalogoDTO producto : pagina.productos()) {
            log.info("  {} · {} · {} € · {} · stock {}", producto.nombre(), producto.artesano(),
                    producto.precio(), producto.tipoPieza(), producto.stock());
        }
    }

    private void mostrarDetalle(DetallePedidoDTO detalle) {
        log.info("Pedido {} · {} · {} · {} · envío a {}, {}", detalle.pedidoId(), detalle.cliente(),
                detalle.fecha(), detalle.estado(), detalle.calle(), detalle.ciudad());
        for (DetallePedidoDTO.Linea linea : detalle.lineas()) {
            log.info("  {} × {} a {} € = {} €", linea.cantidad(), linea.producto(),
                    linea.precioUnitario(), linea.importe());
        }
        log.info("  Total: {} €", detalle.total());
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