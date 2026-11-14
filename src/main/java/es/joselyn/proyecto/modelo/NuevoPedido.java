package es.joselyn.proyecto.modelo;

import java.time.LocalDateTime;

public record NuevoPedido(int clienteId, int direccionId, LocalDateTime fecha, EstadoPedido estado) {
}