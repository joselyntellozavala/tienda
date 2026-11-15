package es.joselyn.proyecto.modelo;

import java.math.BigDecimal;

public record FiltroCatalogo(String categoria, BigDecimal precioMinimo, BigDecimal precioMaximo,
                             boolean soloDisponibles) {
}