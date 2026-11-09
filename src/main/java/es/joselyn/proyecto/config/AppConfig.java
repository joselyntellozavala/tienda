package es.joselyn.proyecto.config;

import es.joselyn.proyecto.excepcion.ConfiguracionException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public record AppConfig(
        DatabaseEngine gestorBaseDatos,
        ConnectionMode modoConexion,
        SimulationMode modoSimulacion,
        String url,
        String usuario,
        String contrasena,
        int maxConexionesPool,
        long esperaMaximaMs
) {


    public static final Path RUTA_POR_DEFECTO = Path.of("application.properties");

    public static AppConfig cargar() {
        return cargar(RUTA_POR_DEFECTO);
    }

    public static AppConfig cargar(Path ruta) {
        if (!Files.exists(ruta)) {
            throw new ConfiguracionException("No se encuentra " + ruta.toAbsolutePath() + ". Cópialo desde application.properties.example y complétalo.");
        }

        Properties propiedades = new Properties();
        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            propiedades.load(lector);
        } catch (IOException e) {
            throw new ConfiguracionException("No se pudo leer " + ruta.toAbsolutePath(), e);
        }

        DatabaseEngine gestorBaseDatos = enumerado(propiedades, "database.engine", DatabaseEngine.class);
        ConnectionMode modoConexion = enumerado(propiedades, "connection.mode", ConnectionMode.class);
        SimulationMode modoSimulacion = enumerado(propiedades, "simulation.mode", SimulationMode.class);

        String prefijo = gestorBaseDatos.name().toLowerCase();
        String url = texto(propiedades, prefijo + ".url", false);
        String usuario = texto(propiedades, prefijo + ".user", false);
        String contrasena = texto(propiedades, prefijo + ".password", true);
        int maxConexionesPool = enteroPositivo(propiedades, "pool.maximumPoolSize");
        long esperaMaximaMs = enteroPositivo(propiedades, "pool.connectionTimeout");

        return new AppConfig(gestorBaseDatos, modoConexion, modoSimulacion, url, usuario, contrasena, maxConexionesPool, esperaMaximaMs);
    }


    private static String texto(Properties props, String clave, boolean permitirVacio) {
        String valor = props.getProperty(clave);
        if (valor == null) {
            throw new ConfiguracionException("Falta la clave '" + clave + "' en application.properties");
        }
        valor = valor.trim();
        if (valor.isEmpty() && !permitirVacio) {
            throw new ConfiguracionException("Falta la clave '" + clave + "' en application.properties");
        }
        return valor;
    }

    private static <E extends Enum<E>> E enumerado(Properties props, String clave, Class<E> tipo) {
        String valor = texto(props, clave, false);
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            throw new ConfiguracionException("Valor no válido en '" + clave + "': '" + valor + "'. Valores admitidos: " + Arrays.toString(tipo.getEnumConstants()));
        }
    }

    private static int enteroPositivo(Properties props, String clave) {
        String valor = texto(props, clave, false);
        int numero;
        try {
            numero = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new ConfiguracionException("La clave '" + clave + "' debe ser un número entero: '" + valor + "'");
        }
        if (numero <= 0) {
            throw new ConfiguracionException("La clave '" + clave + "' debe ser mayor que 0: " + numero);
        }
        return numero;
    }

    @Override
    public String toString() {
        return "AppConfig[gestorBaseDatos=" + gestorBaseDatos
                + ", conexiones=" + modoConexion
                + ", simulacion=" + modoSimulacion
                + ", url=" + url
                + ", usuario=" + usuario
                + ", pool=" + maxConexionesPool + "]";
    }
}