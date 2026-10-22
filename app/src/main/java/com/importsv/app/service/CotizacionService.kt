package com.importsv.app.service

import com.importsv.app.model.Cotizacion

// Servicio encargado de manejar la logica relacionada con las cotizaciones
// Permite calcular, aceptar, rechazar y validar una cotizacion
class CotizacionService {

    // Calcula el total de una cotizacion utilizando los valores definidos en el modelo
    fun calcularTotal(cotizacion: Cotizacion): Double {
        return cotizacion.calcularTotal()
    }

    // Marca una cotizacion como aceptada
    fun aceptarCotizacion(cotizacion: Cotizacion) {
        cotizacion.aceptada = true
    }

    // Marca una cotizacion como rechazada
    fun rechazarCotizacion(cotizacion: Cotizacion) {
        cotizacion.aceptada = false
    }

    // Valida que todos los valores de la cotizacion sean mayores o iguales a cero
    fun validarCotizacion(cotizacion: Cotizacion): Boolean {

        return cotizacion.precioProducto >= 0 &&
                cotizacion.envioInternacional >= 0 &&
                cotizacion.cargosImportacion >= 0 &&
                cotizacion.comision >= 0 &&
                cotizacion.entregaLocal >= 0
    }
}