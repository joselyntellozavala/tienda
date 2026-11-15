# Uso de inteligencia artificial

## Herramientas utilizadas

- Nombre: Claude (Anthropic), en claude.ai.
- Forma de uso: chat, un chat por fase dentro de un Proyecto con el enunciado, los apuntes y el repositorio en modo lectura. Claude proponía y explicaba el diseño y el código; yo lo revisaba, lo incorporaba en IntelliJ y hacía los commits.
- Autocompletado en el IDE y agentes: no se han usado.

## Prompts principales

1. Fase 1 - Modelo de datos: 
- "voy a hacer la práctica UD2 de JDBC y quiero que la tienda online sea Calmma, una marca de cerámica hecha a mano que ya usé en otro curso. Calmma vende tazas, platos, bowls, jarrones y piezas decorativas elaboradas por distintos artesanos y cada producto puede pertenecer a varias categorías a la vez, por ejemplo Mesa, Café y té, Decoración o Regalos. Hay dos tipos de piezas: las de serie, que se fabrican por tandas y se pueden reponer y las piezas únicas, que se hacen una sola vez y, cuando se venden, ya no se pueden reponer. Quiero que esa diferencia influya en las reglas de negocio y en alguna consulta. Ayúdame a organizar el proyecto por fases según el enunciado
- "cuando dices eso de borrado lógico, quiere decir que las piezas únicas no se borrarán sino que se irán almacenando como activo=false? pero no haría crecer indeterminadamente la bbdd?"
- "la regla de las piezas únicas no estaría en la BBDD?"

2. Fase 2 - Configuración y conexiones:
- "ahora explícame lo que tenga que saber sobre application.properties es un fichero de configuración?"
- "simulation.mode tiene que ver con los argumentos? en mi anterior proyecto le pasaba por argumentos los modos de ejecución y por qué en este proyecto no estamos usando argumentos para arrancar en diferentes modos?"

3. Fase 3 - Esquema e inicialización: 
- "en movimiento_stock que es el registro, creo que lo mejor es que la carga inicial no genere movimientos que solo se cargue a inventario y que a partir de ahí ya se registren los cambios, tiene más sentido así para mí o habría problema?"

4. Fase 4 - Modelo y DAO: 
- "tú propones optional, pero en el ejemplo del ejercicio dice que se evita optional en su unidad. Hice JdbcProductoDao siguiendo el UsuarioDao de mi profesor, revísalo ..."

5. Fase 5 - Servicios y transacciones: 
- "en esta fase quiero los servicios de la tienda: registrar, pagar y cancelar pedidos, reponer stock, registrar productos y clientes, qué propones?."
- "este es mi registrarPedido, dime si hay algún caso en el que quede un cambio parcial o propones una mejora que hacer?"
- "la excepcion del negocio es solo una y en el mensaje se va especificando, no? me refiero a que no debería llamarse por ej StockInsuficienteException?"

6. Fase 6 - Consultas, DTO y procedimiento almacenado: 
- "en esta fase quiero las consultas del apartado 8 con al menos un DTO, y el procedimiento almacenado con CallableStatement. No me des el código si no te lo pido, cómo propones la estructura?"
- "los nombres que propones no llevan DTO al final, creo que está bien incluirlo para identificarlos mejor. Hice así la clase ProductoCatalogoDTO, está bien que todos sean tipo records no?..."

7. Fase 7 - Pruebas JUnit 5: 
- "qué pruebas propones para mi proyecto y por qué?"
- "quiero probar que el rollback no deja cambios, explícame qué comprueba mi prueba falloTransaccionNoDejaCambiosParciales y por qué cada aserción es necesaria"

8. Fase 8 - Entrega final: 
- "genérame un readme con simbolos arriba de las tecnologias utilizadas algo así como en mi proyecto anterior"

