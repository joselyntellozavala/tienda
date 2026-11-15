package es.joselyn.proyecto.modelo;

public record ProductoStockBajoDTO(int productoId, String nombre, int stockActual, int stockMinimo,
                                   int unidadesAReponer) {
}