package es.joselyn.proyecto.modelo;

public record Direccion(int id, int clienteId, String calle, String ciudad,
                        String codigoPostal, boolean principal) {
}