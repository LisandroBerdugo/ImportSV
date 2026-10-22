package com.importsv.app.repository

// Interfaz que define las operaciones basicas de un repositorio
// Puede ser utilizada con diferentes tipos de datos mediante el parametro T
interface Repository<T> {

    // Guarda un nuevo elemento
    fun guardar(elemento: T)

    // Retorna todos los elementos almacenados
    fun listar(): List<T>

    // Busca un elemento utilizando su identificador
    fun buscarPorId(id: Int): T?

    // Actualiza un elemento existente y retorna true si fue encontrado
    fun actualizar(elemento: T): Boolean

    // Elimina un elemento por su identificador y retorna true si fue eliminado
    fun eliminar(id: Int): Boolean
}