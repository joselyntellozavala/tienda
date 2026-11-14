package es.joselyn.proyecto.modelo;

public record NuevaDireccion(int clienteId, String calle, String ciudad,
                             String codigoPostal, boolean principal) {
}