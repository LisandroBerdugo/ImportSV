package com.importsv.app.model

import java.time.LocalDateTime

// Clase que representa la entrega final de un pedido
// Guarda la fecha de entrega y observaciones adicionales si existen
data class Entrega(
    val id: Int,
    val pedido: Pedido,
    val fechaEntrega: LocalDateTime,
    var observaciones: String? = null
)