package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record NuevoProducto(String nombre, int artesanoId, BigDecimal precio,
                            TipoPieza tipoPieza, boolean activo) {
}