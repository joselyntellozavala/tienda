package es.joselyn.proyecto.database;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.config.DatabaseEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InicializadorEsquema {

    private static final Logger log = LogManager.getLogger(InicializadorEsquema.class);

    private final ConnectionProvider proveedor;
    private final DatabaseEngine gestor;

    public InicializadorEsquema(ConnectionProvider proveedor, DatabaseEngine gestor) {
        this.proveedor = proveedor;
        this.gestor = gestor;
    }

    public void reconstruir() throws SQLException {
        String ruta = gestor.getScriptEsquema();
        log.info("Reconstruyendo el esquema con {}", ruta);

        List<String> sentencias = separarSentencias(leerScript(ruta));

        try (Connection conexion = proveedor.getConnection();
             Statement sentencia = conexion.createStatement()) {

            for (String sql : sentencias) {
                log.debug("Ejecutando: {}", primeraLinea(sql));
                sentencia.executeUpdate(sql);
            }
        }
        log.info("Esquema reconstruido: {} sentencias ejecutadas", sentencias.size());
    }

    private String leerScript(String ruta) {
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream(ruta)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encuentra el script " + ruta + " en los recursos del programa");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el script " + ruta, e);
        }
    }

    private List<String> separarSentencias(String script) {
        String sinComentarios = script.lines()
                .filter(linea -> !linea.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));

        return Arrays.stream(sinComentarios.split(";"))
                .map(String::trim)
                .filter(sql -> !sql.isEmpty())
                .toList();
    }

    private String primeraLinea(String sql) {
        int salto = sql.indexOf('\n');
        return salto == -1 ? sql : sql.substring(0, salto);
    }
}