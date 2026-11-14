package es.joselyn.proyecto.modelo;

import java.time.LocalDateTime;

public record Pedido(int id, int clienteId, int direccionId, LocalDateTime fecha, EstadoPedido estado) {
}