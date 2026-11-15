package es.joselyn.proyecto.modelo;

import java.util.List;

public record PaginaCatalogoDTO(int pagina, int tamanoPagina, int totalProductos,
                                List<ProductoCatalogoDTO> productos) {

    public int totalPaginas() {
        return (totalProductos + tamanoPagina - 1) / tamanoPagina;
    }
}