DROP TABLE IF EXISTS movimiento_stock;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS linea_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS direccion;
DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS producto_categoria;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS artesano;


CREATE TABLE artesano (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100)
);

CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
);

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    fecha_alta DATE NOT NULL,
    CONSTRAINT uk_cliente_email UNIQUE (email)
);

CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    artesano_id INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    tipo_pieza VARCHAR(10) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_producto_nombre UNIQUE (nombre),
    CONSTRAINT fk_producto_artesano FOREIGN KEY (artesano_id) REFERENCES artesano (id),
    CONSTRAINT ck_producto_precio CHECK (precio >= 0),
    CONSTRAINT ck_producto_tipo_pieza CHECK (tipo_pieza IN ('UNICA', 'SERIE'))
);

CREATE TABLE direccion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    calle VARCHAR(150) NOT NULL,
    ciudad VARCHAR(80) NOT NULL,
    codigo_postal VARCHAR(10) NOT NULL,
    principal BOOLEAN NOT NULL,
    CONSTRAINT fk_direccion_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);


CREATE TABLE producto_categoria (
    producto_id INT NOT NULL,
    categoria_id INT NOT NULL,
    CONSTRAINT pk_producto_categoria PRIMARY KEY (producto_id, categoria_id),
    CONSTRAINT fk_producto_categoria_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_producto_categoria_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);

CREATE TABLE inventario (
    producto_id INT NOT NULL,
    stock_actual INT NOT NULL,
    stock_minimo INT NOT NULL,
    CONSTRAINT pk_inventario PRIMARY KEY (producto_id),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT ck_inventario_stock_actual CHECK (stock_actual >= 0),
    CONSTRAINT ck_inventario_stock_minimo CHECK (stock_minimo >= 0)
);


CREATE TABLE pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    direccion_id INT NOT NULL,
    fecha DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_pedido_direccion FOREIGN KEY (direccion_id) REFERENCES direccion (id),
    CONSTRAINT ck_pedido_estado CHECK (estado IN ('PENDIENTE', 'PAGADO', 'CANCELADO'))
);

CREATE TABLE linea_pedido (
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    importe DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_linea_pedido PRIMARY KEY (pedido_id, producto_id),
    CONSTRAINT fk_linea_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_linea_pedido_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT ck_linea_pedido_cantidad CHECK (cantidad > 0)
);


CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    importe DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT uk_pago_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT ck_pago_metodo CHECK (metodo IN ('TARJETA', 'BIZUM', 'TRANSFERENCIA')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('COMPLETADO', 'REEMBOLSADO'))
);


CREATE TABLE movimiento_stock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL,
    pedido_id INT,
    CONSTRAINT fk_movimiento_stock_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_movimiento_stock_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT ck_movimiento_stock_tipo CHECK (tipo IN ('ENTRADA_REPOSICION', 'SALIDA_PEDIDO', 'DEVOLUCION_CANCELACION')),
    CONSTRAINT ck_movimiento_stock_cantidad CHECK (cantidad > 0)
);