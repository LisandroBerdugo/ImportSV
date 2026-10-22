
package com.importsv.app.data

import androidx.compose.runtime.*
import com.importsv.app.auth.LocalUser
import com.importsv.app.model.*
import com.importsv.app.repository.*
import com.importsv.app.service.*

/**
 * Almacenamiento compartido para el Foro 1.
 * Los datos se mantienen mientras la aplicación está en memoria.
 */
object DemoStore {

    private val solicitudesRepo = SolicitudRepository()
    private val pedidosRepo = PedidoRepository()
    private val clientesRepo = ClienteRepository()

    private val solicitudesService = SolicitudService(solicitudesRepo)
    private val pedidosService = PedidoService(pedidosRepo)
    private val clientesService = ClienteService(clientesRepo)
    private val cotizacionesService = CotizacionService()

    private val listaCotizaciones = mutableListOf<Cotizacion>()

    var revision by mutableIntStateOf(0)
        private set

    private var solicitudId = 1
    private var cotizacionId = 1
    private var pedidoId = 1

    fun solicitudes(): List<Solicitud> {
        revision
        return solicitudesService.listarSolicitudes()
    }

    fun misSolicitudes(clienteId: Int): List<Solicitud> {
        revision
        return solicitudesService.buscarPorCliente(clienteId)
    }

    fun pedidos(): List<Pedido> {
        revision
        return pedidosService.listarPedidos()
    }

    fun misPedidos(clienteId: Int): List<Pedido> {
        revision
        return pedidosService.listarPedidos().filter {
            it.solicitud.cliente.id == clienteId
        }
    }

    fun cotizaciones(): List<Cotizacion> {
        revision
        return listaCotizaciones.toList()
    }

    fun cotizacionPara(idSolicitud: Int): Cotizacion? {
        revision
        return listaCotizaciones.find {
            it.solicitud.id == idSolicitud
        }
    }

    fun clientes(): List<Cliente> {
        revision
        return clientesService.listarClientes()
    }

    fun crearSolicitud(
        usuario: LocalUser,
        enlace: String,
        nombre: String,
        categoria: String,
        cantidad: Int,
        notas: String
    ): Boolean {

        val cliente = clientesService.buscarCliente(usuario.id)
            ?: Cliente(
                usuario.id,
                usuario.nombre,
                usuario.correo,
                ""
            ).also {
                clientesService.registrarCliente(it)
            }

        val solicitud = Solicitud(
            solicitudId,
            cliente,
            enlace.trim(),
            nombre.trim(),
            categoria.trim(),
            cantidad,
            notas.trim().ifBlank { null }
        )

        if (!solicitudesService.registrarSolicitud(solicitud)) {
            return false
        }

        solicitudId++
        revision++

        return true
    }

    fun crearCotizacion(
        idSolicitud: Int,
        importes: List<Double>
    ): String {

        val solicitud = solicitudesService.buscarSolicitud(idSolicitud)
            ?: return "Solicitud no encontrada"

        if (cotizacionPara(idSolicitud) != null) {
            return "Esta solicitud ya tiene cotización"
        }

        if (
            importes.size != 5 ||
            importes.any { !it.isFinite() || it < 0.0 }
        ) {
            return "Los importes deben ser válidos y no negativos"
        }

        val cotizacion = Cotizacion(
            cotizacionId,
            solicitud,
            importes[0],
            importes[1],
            importes[2],
            importes[3],
            importes[4]
        )

        if (!cotizacionesService.validarCotizacion(cotizacion)) {
            return "Cotización inválida"
        }

        listaCotizaciones.add(cotizacion)

        cotizacionId++
        revision++

        return "Cotización creada correctamente"
    }

    fun aceptarCotizacion(
        idCotizacion: Int,
        clienteId: Int
    ): String {

        val cotizacion = listaCotizaciones.find {
            it.id == idCotizacion
        } ?: return "Cotización no encontrada"

        if (cotizacion.solicitud.cliente.id != clienteId) {
            return "No tienes acceso a esta cotización"
        }

        if (cotizacion.aceptada) {
            return "Cotización ya aceptada"
        }

        cotizacionesService.aceptarCotizacion(cotizacion)

        val pedido = Pedido(
            pedidoId,
            cotizacion.solicitud,
            cotizacion
        )

        if (!pedidosService.registrarPedido(pedido)) {
            return "No se pudo registrar el pedido"
        }

        pedidoId++
        revision++

        return "Cotización aceptada. Pedido creado"
    }

    fun avanzarPedido(idPedido: Int): String {

        val pedido = pedidosService.buscarPedido(idPedido)
            ?: return "Pedido no encontrado"

        val estados = EstadoPedido.entries

        val siguiente = estados.getOrNull(
            estados.indexOf(pedido.estado) + 1
        ) ?: return "El pedido ya fue entregado"

        val exito = pedidosService.actualizarEstado(
            pedido,
            siguiente
        )

        if (exito) {
            revision++
        }

        return if (exito) {
            "Pedido actualizado a ${siguiente.name}"
        } else {
            "Transición no permitida"
        }
    }
}
