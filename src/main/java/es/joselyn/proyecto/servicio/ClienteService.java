package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.dao.ClienteDao;
import es.joselyn.proyecto.dao.DireccionDao;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Cliente;
import es.joselyn.proyecto.modelo.Direccion;
import es.joselyn.proyecto.modelo.NuevaDireccion;
import es.joselyn.proyecto.modelo.NuevoCliente;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private static final Logger log = LogManager.getLogger(ClienteService.class);

    private final ConnectionProvider proveedor;
    private final ClienteDao clienteDao;
    private final DireccionDao direccionDao;

    public ClienteService(ConnectionProvider proveedor, ClienteDao clienteDao, DireccionDao direccionDao) {
        this.proveedor = proveedor;
        this.clienteDao = clienteDao;
        this.direccionDao = direccionDao;
    }

    public int registrarCliente(NuevoCliente cliente, String calle, String ciudad,
                                String codigoPostal) throws SQLException {
        if (cliente.email() == null || cliente.email().isBlank()) {
            throw new NegocioException("El cliente necesita un email");
        }
        log.info("Inicio registro de cliente");

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                int clienteId = clienteDao.insertar(conexion, cliente);
                direccionDao.insertar(conexion, new NuevaDireccion(clienteId, calle, ciudad, codigoPostal, true));

                conexion.commit();
                log.info("Cliente registrado id={} (commit)", clienteId);
                return clienteId;
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Registro de cliente revertido (rollback): {}", fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public Cliente buscarPorEmail(String email) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return clienteDao.buscarPorEmail(conexion, email);
        }
    }

    public List<Direccion> direccionesDe(int clienteId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return direccionDao.buscarPorCliente(conexion, clienteId);
        }
    }
}