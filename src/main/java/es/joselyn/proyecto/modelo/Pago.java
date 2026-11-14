package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pago(int id, int pedidoId, MetodoPago metodo, BigDecimal importe,
                   EstadoPago estado, LocalDateTime fecha) {
}