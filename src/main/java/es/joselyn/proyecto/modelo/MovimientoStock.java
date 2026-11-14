package es.joselyn.proyecto.modelo;

import java.time.LocalDateTime;

public record MovimientoStock(int id, int productoId, TipoMovimiento tipo, int cantidad,
                              LocalDateTime fecha, Integer pedidoId) {
}