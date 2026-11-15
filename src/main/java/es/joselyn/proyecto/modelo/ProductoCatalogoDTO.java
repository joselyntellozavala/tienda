package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record ProductoCatalogoDTO(int id, String nombre, String artesano, BigDecimal precio,
                                  TipoPieza tipoPieza, int stock) {
}