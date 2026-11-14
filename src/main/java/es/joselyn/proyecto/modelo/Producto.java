package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record Producto(int id, String nombre, int artesanoId, BigDecimal precio,
                       TipoPieza tipoPieza, boolean activo) {
}