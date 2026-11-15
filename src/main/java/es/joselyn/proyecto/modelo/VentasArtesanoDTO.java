package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record VentasArtesanoDTO(String artesano, int piezasUnicas, int unidadesSerie, BigDecimal ingresos) {
}