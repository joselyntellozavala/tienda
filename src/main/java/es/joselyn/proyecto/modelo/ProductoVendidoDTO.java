package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record ProductoVendidoDTO(int productoId, String nombre, int unidades, BigDecimal ingresos, int pedidos) {
}