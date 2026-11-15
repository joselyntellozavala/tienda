DROP PROCEDURE IF EXISTS resumen_ventas_producto
//
CREATE PROCEDURE resumen_ventas_producto(
    IN p_producto_id INT,
    OUT p_unidades INT,
    OUT p_ingresos DECIMAL(10,2)
)
BEGIN
SELECT COALESCE(SUM(lp.cantidad), 0), COALESCE(SUM(lp.importe), 0)
INTO p_unidades, p_ingresos
FROM linea_pedido lp
         JOIN pedido pe ON pe.id = lp.pedido_id
WHERE lp.producto_id = p_producto_id
  AND pe.estado <> 'CANCELADO';
END
//