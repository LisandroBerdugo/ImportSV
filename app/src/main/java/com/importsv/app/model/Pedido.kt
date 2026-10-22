package com.importsv.app.model

// Clase que representa un pedido generado a partir de una solicitud y una cotizacion
// Mantiene el estado actual del pedido y el historial de cambios realizados
data class Pedido(
    val id: Int,
    val solicitud: Solicitud,
    val cotizacion: Cotizacion,
    var estado: EstadoPedido = EstadoPedido.APROBADO
) {

    // Lista que almacena todos los estados por los que ha pasado el pedido
    val historialEstados = mutableListOf<EstadoPedido>()

    // Agrega el estado inicial del pedido al historial al crear el objeto
    init {
        historialEstados.add(estado)
    }

    // Actualiza el estado actual del pedido y registra el cambio en el historial
    fun actualizarEstado(nuevoEstado: EstadoPedido) {
        estado = nuevoEstado
        historialEstados.add(nuevoEstado)
    }
}