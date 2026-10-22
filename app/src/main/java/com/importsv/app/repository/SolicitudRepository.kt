package com.importsv.app.repository

import com.importsv.app.model.Solicitud

// Repositorio encargado de gestionar las solicitudes almacenadas en memoria
// Implementa las operaciones definidas en la interfaz Repository
class SolicitudRepository : Repository<Solicitud> {

    // Lista mutable que almacena las solicitudes registradas durante la ejecucion
    private val solicitudes = mutableListOf<Solicitud>()

    // Guarda una nueva solicitud dentro de la lista
    override fun guardar(elemento: Solicitud) {
        solicitudes.add(elemento)
    }

    // Retorna una copia de la lista de solicitudes registradas
    override fun listar(): List<Solicitud> {
        return solicitudes.toList()
    }

    // Busca una solicitud utilizando su identificador
    override fun buscarPorId(id: Int): Solicitud? {
        return solicitudes.find { it.id == id }
    }

    // Actualiza la informacion de una solicitud existente
    override fun actualizar(elemento: Solicitud): Boolean {

        // Busca la posicion de la solicitud dentro de la lista
        val indice = solicitudes.indexOfFirst { it.id == elemento.id }

        return if (indice != -1) {

            // Reemplaza la solicitud existente por la nueva informacion
            solicitudes[indice] = elemento
            true

        } else {

            // Retorna false si la solicitud no fue encontrada
            false
        }
    }

    // Elimina una solicitud utilizando su identificador
    override fun eliminar(id: Int): Boolean {
        return solicitudes.removeIf { it.id == id }
    }

    // Filtra y retorna las solicitudes que pertenecen a un cliente especifico
    fun buscarPorCliente(idCliente: Int): List<Solicitud> {
        return solicitudes.filter {
            it.cliente.id == idCliente
        }
    }

    // Filtra y retorna las solicitudes que pertenecen a una categoria especifica
    // La comparacion ignora diferencias entre mayusculas y minusculas
    fun buscarPorCategoria(categoria: String): List<Solicitud> {
        return solicitudes.filter {
            it.categoria.equals(categoria, ignoreCase = true)
        }
    }
}