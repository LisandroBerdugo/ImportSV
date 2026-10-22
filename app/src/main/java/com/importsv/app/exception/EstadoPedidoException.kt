package com.importsv.app.exception

// Excepcion utilizada cuando se intenta realizar un cambio de estado no permitido en un pedido
class EstadoPedidoException(
    message: String
) : Exception(message)