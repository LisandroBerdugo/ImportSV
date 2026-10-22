package com.importsv.app.model

// Clase que representa una solicitud realizada por un cliente
// Contiene la informacion del producto que se desea importar
data class Solicitud(
    val id: Int,
    val cliente: Cliente,
    var enlaceProducto: String,
    var nombreProducto: String,
    var categoria: String,
    var cantidad: Int,
    var observaciones: String? = null
)