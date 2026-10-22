package com.importsv.app.model

// Clase que representa una cotizacion asociada a una solicitud
// Contiene los diferentes costos necesarios para calcular el total
data class Cotizacion(
    val id: Int,
    val solicitud: Solicitud,
    var precioProducto: Double,
    var envioInternacional: Double,
    var cargosImportacion: Double,
    var comision: Double,
    var entregaLocal: Double,
    var aceptada: Boolean = false
) {

    // Calcula el costo total sumando todos los valores de la cotizacion
    fun calcularTotal(): Double {
        return precioProducto +
                envioInternacional +
                cargosImportacion +
                comision +
                entregaLocal
    }
}