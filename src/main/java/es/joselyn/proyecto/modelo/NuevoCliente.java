package es.joselyn.proyecto.modelo;

import java.time.LocalDate;

public record NuevoCliente(String nombre, String apellidos, String email, LocalDate fechaAlta) {
}