package com.importsv.app.service

import com.importsv.app.model.Solicitud
import com.importsv.app.repository.SolicitudRepository

// Servicio encargado de manejar la logica relacionada con las solicitudes
// Permite registrar, consultar, actualizar, eliminar y filtrar solicitudes
class SolicitudService(
    private val solicitudRepository: SolicitudRepository
) {

    // Registra una nueva solicitud si cumple con las validaciones basicas
    fun registrarSolicitud(solicitud: Solicitud): Boolean {

        // Verifica que el enlace del producto no este vacio
        if (solicitud.enlaceProducto.isBlank()) {
            return false
        }

        // Verifica que el nombre del producto no este vacio
        if (solicitud.nombreProducto.isBlank()) {
            return false
        }

        // Verifica que la categoria no este vacia
        if (solicitud.categoria.isBlank()) {
            return false
        }

        // Verifica que la cantidad sea mayor que cero
        if (solicitud.cantidad <= 0) {
            return false
        }

        // Guarda la solicitud en el repositorio
        solicitudRepository.guardar(solicitud)

        return true
    }

    // Retorna la lista de todas las solicitudes registradas
    fun listarSolicitudes(): List<Solicitud> {
        return solicitudRepository.listar()
    }

    // Busca una solicitud utilizando su identificador
    fun buscarSolicitud(id: Int): Solicitud? {
        return solicitudRepository.buscarPorId(id)
    }

    // Actualiza una solicitud si la cantidad ingresada es valida
    fun actualizarSolicitud(solicitud: Solicitud): Boolean {

        // No permite cantidades menores o iguales a cero
        if (solicitud.cantidad <= 0) {
            return false
        }

        // Actualiza la solicitud en el repositorio
        return solicitudRepository.actualizar(solicitud)
    }

    // Elimina una solicitud utilizando su identificador
    fun eliminarSolicitud(id: Int): Boolean {
        return solicitudRepository.eliminar(id)
    }

    // Retorna las solicitudes asociadas a un cliente especifico
    fun buscarPorCliente(idCliente: Int): List<Solicitud> {
        return solicitudRepository.buscarPorCliente(idCliente)
    }

    // Retorna las solicitudes que pertenecen a una categoria especifica
    fun buscarPorCategoria(categoria: String): List<Solicitud> {
        return solicitudRepository.buscarPorCategoria(categoria)
    }
}