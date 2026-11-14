package es.joselyn.proyecto.database;

import es.joselyn.proyecto.modelo.TipoPieza;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static es.joselyn.proyecto.modelo.TipoPieza.SERIE;
import static es.joselyn.proyecto.modelo.TipoPieza.UNICA;

public final class DatosIniciales {

    private DatosIniciales() {
    }

    public record NuevoArtesano(String nombre, String especialidad) {
    }

    public record NuevoProducto(String nombre, String artesano, BigDecimal precio, TipoPieza tipoPieza,
                                boolean activo, List<String> categorias, int stockActual, int stockMinimo) {
    }

    public record NuevaDireccion(String calle, String ciudad, String codigoPostal, boolean principal) {
    }

    public record NuevoCliente(String nombre, String apellidos, String email, LocalDate fechaAlta,
                               List<NuevaDireccion> direcciones) {
    }

    public static final List<NuevoArtesano> ARTESANOS = List.of(
            new NuevoArtesano("Lucía Ferrer", "Torno y tazas"),
            new NuevoArtesano("Mateo Ruiz", "Vajilla esmaltada"),
            new NuevoArtesano("Inés Calvo", "Piezas decorativas"),
            new NuevoArtesano("Tomás Vidal", "Gres de alta temperatura")
    );

    public static final List<String> CATEGORIAS = List.of("Mesa", "Café y té", "Decoración", "Regalos");

    public static final List<NuevoProducto> PRODUCTOS = List.of(
            producto("Taza Luna", "Lucía Ferrer", "14.90", SERIE, true, List.of("Café y té", "Regalos"), 25, 10),
            producto("Taza Bruma", "Lucía Ferrer", "13.50", SERIE, true, List.of("Café y té"), 4, 8),
            producto("Tetera Alba", "Lucía Ferrer", "42.00", SERIE, true, List.of("Café y té", "Mesa"), 6, 3),
            producto("Plato llano Arena", "Mateo Ruiz", "18.00", SERIE, true, List.of("Mesa"), 30, 12),
            producto("Plato hondo Arena", "Mateo Ruiz", "19.50", SERIE, true, List.of("Mesa"), 5, 12),
            producto("Bowl Marea", "Mateo Ruiz", "16.00", SERIE, true, List.of("Mesa", "Regalos"), 18, 8),
            producto("Fuente Salitre", "Tomás Vidal", "38.00", SERIE, false, List.of("Mesa"), 3, 0),
            producto("Jarra Olivo", "Tomás Vidal", "29.90", SERIE, true, List.of("Mesa", "Café y té"), 2, 5),
            producto("Portavelas Duna", "Inés Calvo", "9.50", SERIE, true, List.of("Decoración", "Regalos"), 40, 15),
            producto("Jarrón Raíz", "Inés Calvo", "64.00", SERIE, true, List.of("Decoración"), 9, 3),
            producto("Jarrón Eclipse", "Inés Calvo", "120.00", UNICA, true, List.of("Decoración"), 1, 0),
            producto("Escultura Ola", "Inés Calvo", "95.00", UNICA, true, List.of("Decoración", "Regalos"), 1, 0),
            producto("Fuente Volcán", "Tomás Vidal", "85.00", UNICA, true, List.of("Mesa", "Decoración"), 1, 0),
            producto("Taza Cometa", "Lucía Ferrer", "22.00", UNICA, true, List.of("Café y té", "Regalos"), 1, 0)
    );

    public static final List<NuevoCliente> CLIENTES = List.of(
            new NuevoCliente("Ana", "Morales Gil", "ana.morales@correo.es", LocalDate.of(2025, 3, 12), List.of(
                    new NuevaDireccion("Calle Mayor 12", "Madrid", "28013", true),
                    new NuevaDireccion("Calle de Alcalá 200", "Madrid", "28028", false))),
            new NuevoCliente("Diego", "Navarro Ruiz", "diego.navarro@correo.es", LocalDate.of(2025, 6, 2), List.of(
                    new NuevaDireccion("Avenida Diagonal 450", "Barcelona", "08006", true))),
            new NuevoCliente("Carmen", "Soto Díaz", "carmen.soto@correo.es", LocalDate.of(2025, 9, 21), List.of(
                    new NuevaDireccion("Calle Colón 8", "Valencia", "46004", true),
                    new NuevaDireccion("Calle de la Paz 3", "Valencia", "46003", false))),
            new NuevoCliente("Hugo", "Martín Pérez", "hugo.martin@correo.es", LocalDate.of(2026, 1, 15), List.of(
                    new NuevaDireccion("Calle Sierpes 25", "Sevilla", "41004", true))),
            new NuevoCliente("Elena", "Ríos Blanco", "elena.rios@correo.es", LocalDate.of(2026, 4, 30), List.of(
                    new NuevaDireccion("Gran Vía 1", "Bilbao", "48001", true)))
    );

    private static NuevoProducto producto(String nombre, String artesano, String precio, TipoPieza tipo,
                                          boolean activo, List<String> categorias, int stock, int minimo) {
        return new NuevoProducto(nombre, artesano, new BigDecimal(precio), tipo, activo, categorias, stock, minimo);
    }
}