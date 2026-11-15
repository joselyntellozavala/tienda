package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumenPedidoDTO(int pedidoId, LocalDateTime fecha, EstadoPedido estado, int productos,
                               int unidades, BigDecimal total, EstadoPago estadoPago) {
}