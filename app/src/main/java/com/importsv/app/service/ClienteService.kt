package com.importsv.app.service

import com.importsv.app.model.Cliente
import com.importsv.app.repository.ClienteRepository

// Servicio encargado de manejar la logica relacionada con los clientes
// Utiliza el repositorio para consultar, guardar, actualizar y eliminar datos
class ClienteService(
    private val clienteRepository: ClienteRepository
) {

    // Registra un nuevo cliente siempre que el correo no este repetido
    fun registrarCliente(cliente: Cliente): Boolean {

        // Verifica si ya existe un cliente con el mismo correo
        val existeCorreo = clienteRepository
            .listar()
            .any { it.correo.equals(cliente.correo, ignoreCase = true) }

        // Si el correo ya existe no se permite registrar el cliente
        if (existeCorreo) {
            return false
        }

        // Guarda el cliente en el repositorio
        clienteRepository.guardar(cliente)
        return true
    }

    // Retorna la lista de todos los clientes registrados
    fun listarClientes(): List<Cliente> {
        return clienteRepository.listar()
    }

    // Busca un cliente utilizando su identificador
    fun buscarCliente(id: Int): Cliente? {
        return clienteRepository.buscarPorId(id)
    }

    // Actualiza la informacion de un cliente existente
    fun actualizarCliente(cliente: Cliente): Boolean {
        return clienteRepository.actualizar(cliente)
    }

    // Elimina un cliente utilizando su identificador
    fun eliminarCliente(id: Int): Boolean {
        return clienteRepository.eliminar(id)
    }
}