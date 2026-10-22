package com.importsv.app.repository

import com.importsv.app.model.Cliente

// Repositorio encargado de gestionar los clientes almacenados en memoria
// Implementa las operaciones definidas en la interfaz Repository
class ClienteRepository : Repository<Cliente> {

    // Lista mutable que almacena los clientes registrados durante la ejecucion
    private val clientes = mutableListOf<Cliente>()

    // Guarda un nuevo cliente dentro de la lista
    override fun guardar(elemento: Cliente) {
        clientes.add(elemento)
    }

    // Retorna una copia de la lista de clientes registrados
    override fun listar(): List<Cliente> {
        return clientes.toList()
    }

    // Busca un cliente por medio de su identificador
    override fun buscarPorId(id: Int): Cliente? {
        return clientes.find { it.id == id }
    }

    // Actualiza los datos de un cliente existente
    override fun actualizar(elemento: Cliente): Boolean {

        // Busca la posicion del cliente dentro de la lista
        val indice = clientes.indexOfFirst { it.id == elemento.id }

        return if (indice != -1) {

            // Reemplaza el cliente existente por la nueva informacion
            clientes[indice] = elemento
            true

        } else {

            // Retorna false si el cliente no fue encontrado
            false
        }
    }

    // Elimina un cliente utilizando su identificador
    override fun eliminar(id: Int): Boolean {
        return clientes.removeIf { it.id == id }
    }
}