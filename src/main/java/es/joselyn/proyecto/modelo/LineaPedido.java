package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record LineaPedido(int pedidoId, int productoId, int cantidad,
                          BigDecimal precioUnitario, BigDecimal importe) {

    public static LineaPedido nueva(int pedidoId, int productoId, int cantidad, BigDecimal precioUnitario) {
        BigDecimal importe = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return new LineaPedido(pedidoId, productoId, cantidad, precioUnitario, importe);
    }
}