package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record ResumenVentasProductoDTO(int productoId, int unidades, BigDecimal ingresos) {
}