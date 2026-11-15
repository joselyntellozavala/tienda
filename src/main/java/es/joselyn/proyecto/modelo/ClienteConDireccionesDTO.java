package es.joselyn.proyecto.modelo;

import java.util.List;

public record ClienteConDireccionesDTO(Cliente cliente, List<Direccion> direcciones) {
}