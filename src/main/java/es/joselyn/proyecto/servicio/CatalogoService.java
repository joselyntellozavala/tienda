package es.joselyn.proyecto.servicio;

import es.joselyn.proyecto.conexion.ConnectionProvider;
import es.joselyn.proyecto.dao.CategoriaDao;
import es.joselyn.proyecto.dao.InventarioDao;
import es.joselyn.proyecto.dao.ProductoCategoriaDao;
import es.joselyn.proyecto.dao.ProductoDao;
import es.joselyn.proyecto.excepcion.NegocioException;
import es.joselyn.proyecto.modelo.Categoria;
import es.joselyn.proyecto.modelo.Inventario;
import es.joselyn.proyecto.modelo.NuevoProducto;
import es.joselyn.proyecto.modelo.Producto;
import es.joselyn.proyecto.modelo.TipoPieza;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogoService {

    private static final Logger log = LogManager.getLogger(CatalogoService.class);

    private final ConnectionProvider proveedor;
    private final ProductoDao productoDao;
    private final CategoriaDao categoriaDao;
    private final ProductoCategoriaDao productoCategoriaDao;
    private final InventarioDao inventarioDao;

    public CatalogoService(ConnectionProvider proveedor, ProductoDao productoDao, CategoriaDao categoriaDao,
                           ProductoCategoriaDao productoCategoriaDao, InventarioDao inventarioDao) {
        this.proveedor = proveedor;
        this.productoDao = productoDao;
        this.categoriaDao = categoriaDao;
        this.productoCategoriaDao = productoCategoriaDao;
        this.inventarioDao = inventarioDao;
    }

    public int registrarProducto(NuevoProducto producto, int stockInicial, int stockMinimo,
                                 List<String> categorias) throws SQLException {
        if (producto.precio().signum() <= 0) {
            throw new NegocioException("El precio debe ser mayor que 0");
        }
        if (stockInicial < 0 || stockMinimo < 0) {
            throw new NegocioException("El stock no puede ser negativo");
        }
        if (producto.tipoPieza() == TipoPieza.UNICA && (stockInicial != 1 || stockMinimo != 0)) {
            throw new NegocioException("Una pieza única se registra con stock 1 y stock mínimo 0");
        }
        log.info("Inicio registro de producto categorias={}", categorias.size());

        try (Connection conexion = proveedor.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);

                List<Categoria> encontradas = new ArrayList<>();
                for (String nombre : categorias) {
                    Categoria categoria = categoriaDao.buscarPorNombre(conexion, nombre);
                    if (categoria == null) {
                        throw new NegocioException("No existe la categoría " + nombre);
                    }
                    encontradas.add(categoria);
                }

                int productoId = productoDao.insertar(conexion, producto);
                inventarioDao.insertar(conexion, new Inventario(productoId, stockInicial, stockMinimo));
                for (Categoria categoria : encontradas) {
                    productoCategoriaDao.asignar(conexion, productoId, categoria.id());
                }

                conexion.commit();
                log.info("Producto registrado id={} (commit)", productoId);
                return productoId;
            } catch (SQLException | RuntimeException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }
                log.warn("Registro de producto revertido (rollback): {}", fallo.getMessage());
                throw fallo;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public void cambiarPrecio(int productoId, BigDecimal nuevoPrecio) throws SQLException {
        if (nuevoPrecio.signum() <= 0) {
            throw new NegocioException("El precio debe ser mayor que 0");
        }
        try (Connection conexion = proveedor.getConnection()) {
            if (productoDao.actualizarPrecio(conexion, productoId, nuevoPrecio) == 0) {
                throw new NegocioException("No existe el producto " + productoId);
            }
        }
        log.info("Precio actualizado producto={}", productoId);
    }

    public Producto buscarProductoPorNombre(String nombre) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return productoDao.buscarPorNombre(conexion, nombre);
        }
    }

    public List<Categoria> categoriasDe(int productoId) throws SQLException {
        try (Connection conexion = proveedor.getConnection()) {
            return categoriaDao.buscarPorProducto(conexion, productoId);
        }
    }
}