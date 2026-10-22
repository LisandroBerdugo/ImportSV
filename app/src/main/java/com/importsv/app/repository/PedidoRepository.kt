package com.importsv.app.repository

import com.importsv.app.model.EstadoPedido
import com.importsv.app.model.Pedido

// Repositorio encargado de gestionar los pedidos almacenados en memoria
// Implementa las operaciones definidas en la interfaz Repository
class PedidoRepository : Repository<Pedido> {

    // Lista mutable que almacena los pedidos registrados durante la ejecucion
    private val pedidos = mutableListOf<Pedido>()

    // Guarda un nuevo pedido dentro de la lista
    override fun guardar(elemento: Pedido) {
        pedidos.add(elemento)
    }

    // Retorna una copia de la lista de pedidos registrados
    override fun listar(): List<Pedido> {
        return pedidos.toList()
    }

    // Busca un pedido utilizando su identificador
    override fun buscarPorId(id: Int): Pedido? {
        return pedidos.find { it.id == id }
    }

    // Actualiza la informacion de un pedido existente
    override fun actualizar(elemento: Pedido): Boolean {

        // Busca la posicion del pedido dentro de la lista
        val indice = pedidos.indexOfFirst { it.id == elemento.id }

        return if (indice != -1) {

            // Reemplaza el pedido existente por la nueva informacion
            pedidos[indice] = elemento
            true

        } else {

            // Retorna false si el pedido no fue encontrado
            false
        }
    }

    // Elimina un pedido utilizando su identificador
    override fun eliminar(id: Int): Boolean {
        return pedidos.removeIf { it.id == id }
    }

    // Filtra y retorna los pedidos que tengan el estado indicado
    fun buscarPorEstado(estado: EstadoPedido): List<Pedido> {
        return pedidos.filter {
            it.estado == estado
        }
    }

    // Retorna todos los pedidos que aun no han sido entregados
    fun obtenerPedidosActivos(): List<Pedido> {
        return pedidos.filter {
            it.estado != EstadoPedido.ENTREGADO
        }
    }

    // Ordena los pedidos de mayor a menor segun el total de la cotizacion
    fun obtenerPedidosOrdenadosPorTotal(): List<Pedido> {
        return pedidos.sortedByDescending {
            it.cotizacion.calcularTotal()
        }
    }
}