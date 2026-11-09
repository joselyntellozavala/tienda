package es.joselyn.proyecto.excepcion;

public class ConfiguracionException extends RuntimeException {

    public ConfiguracionException(String mensaje) {
        super(mensaje);
    }

    public ConfiguracionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}