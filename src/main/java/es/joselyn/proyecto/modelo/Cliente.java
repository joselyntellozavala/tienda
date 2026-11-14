package es.joselyn.proyecto.modelo;

import java.time.LocalDate;

public record Cliente(int id, String nombre, String apellidos, String email, LocalDate fechaAlta) {
}