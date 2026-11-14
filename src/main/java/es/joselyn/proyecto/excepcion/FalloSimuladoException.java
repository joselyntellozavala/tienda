package es.joselyn.proyecto.excepcion;

public class FalloSimuladoException extends RuntimeException {

    private final int pedidoId;

    public FalloSimuladoException(int pedidoId) {
        super("Fallo provocado por FALLO_TRANSACCION antes de confirmar el pedido " + pedidoId);
        this.pedidoId = pedidoId;
    }

    public int getPedidoId() {
        return pedidoId;
    }
}