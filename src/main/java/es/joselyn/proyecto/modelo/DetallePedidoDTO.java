package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DetallePedidoDTO(int pedidoId, String cliente, LocalDateTime fecha, EstadoPedido estado,
                               String calle, String ciudad, BigDecimal total, List<Linea> lineas) {

    public record Linea(String producto, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
    }
}