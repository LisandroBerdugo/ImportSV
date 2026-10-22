package com.importsv.app.model

// Enum que contiene los diferentes estados que puede tener un pedido
// Permite controlar el avance del pedido dentro del proceso de importacion
enum class EstadoPedido {
    SOLICITADO,
    COTIZADO,
    APROBADO,
    COMPRADO,
    EN_TRANSITO,
    EN_ADUANA,
    RECIBIDO,
    LISTO_ENTREGA,
    ENTREGADO
}