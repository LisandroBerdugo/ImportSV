package com.importsv.app.service

import com.importsv.app.model.EstadoPedido
import com.importsv.app.model.Pedido
import com.importsv.app.repository.PedidoRepository

// Servicio encargado de manejar la logica relacionada con los pedidos
// Permite registrar, consultar, eliminar y actualizar el estado de un pedido
class PedidoService(
    private val pedidoRepository: PedidoRepository
) {

    // Registra un pedido solamente si la cotizacion fue aceptada
    fun registrarPedido(pedido: Pedido): Boolean {

        // Verifica que la cotizacion asociada al pedido haya sido aceptada
        if (!pedido.cotizacion.aceptada) {
            return false
        }

        // Guarda el pedido en el repositorio
        pedidoRepository.guardar(pedido)

        return true
    }

    // Retorna la lista de todos los pedidos registrados
    fun listarPedidos(): List<Pedido> {
        return pedidoRepository.listar()
    }

    // Busca un pedido utilizando su identificador
    fun buscarPedido(id: Int): Pedido? {
        return pedidoRepository.buscarPorId(id)
    }

    // Elimina un pedido utilizando su identificador
    fun eliminarPedido(id: Int): Boolean {
        return pedidoRepository.eliminar(id)
    }

    // Actualiza el estado de un pedido si la transicion es valida
    fun actualizarEstado(
        pedido: Pedido,
        nuevoEstado: EstadoPedido
    ): Boolean {

        // Verifica que el cambio de estado siga el flujo permitido
        if (!esTransicionValida(pedido.estado, nuevoEstado)) {
            return false
        }

        // Actualiza el estado del pedido y guarda el cambio en el repositorio
        pedido.actualizarEstado(nuevoEstado)
        pedidoRepository.actualizar(pedido)

        return true
    }

    // Verifica si el cambio entre el estado actual y el nuevo estado esta permitido
    private fun esTransicionValida(
        estadoActual: EstadoPedido,
        nuevoEstado: EstadoPedido
    ): Boolean {

        return when (estadoActual) {

            // Un pedido solicitado solamente puede pasar a cotizado
            EstadoPedido.SOLICITADO ->
                nuevoEstado == EstadoPedido.COTIZADO

            // Un pedido cotizado solamente puede pasar a aprobado
            EstadoPedido.COTIZADO ->
                nuevoEstado == EstadoPedido.APROBADO

            // Un pedido aprobado solamente puede pasar a comprado
            EstadoPedido.APROBADO ->
                nuevoEstado == EstadoPedido.COMPRADO

            // Un pedido comprado solamente puede pasar a en transito
            EstadoPedido.COMPRADO ->
                nuevoEstado == EstadoPedido.EN_TRANSITO

            // Un pedido en transito solamente puede pasar a en aduana
            EstadoPedido.EN_TRANSITO ->
                nuevoEstado == EstadoPedido.EN_ADUANA

            // Un pedido en aduana solamente puede pasar a recibido
            EstadoPedido.EN_ADUANA ->
                nuevoEstado == EstadoPedido.RECIBIDO

            // Un pedido recibido solamente puede pasar a listo para entrega
            EstadoPedido.RECIBIDO ->
                nuevoEstado == EstadoPedido.LISTO_ENTREGA

            // Un pedido listo para entrega solamente puede pasar a entregado
            EstadoPedido.LISTO_ENTREGA ->
                nuevoEstado == EstadoPedido.ENTREGADO

            // Un pedido entregado ya no puede cambiar de estado
            EstadoPedido.ENTREGADO ->
                false
        }
    }

    // Retorna todos los pedidos que aun no han sido entregados
    fun obtenerPedidosActivos(): List<Pedido> {
        return pedidoRepository.obtenerPedidosActivos()
    }

    // Retorna los pedidos que coinciden con un estado especifico
    fun buscarPorEstado(
        estado: EstadoPedido
    ): List<Pedido> {
        return pedidoRepository.buscarPorEstado(estado)
    }
}