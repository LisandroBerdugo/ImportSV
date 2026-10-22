package com.importsv.app.model

// Clase que representa a un usuario con rol de administrador
// Hereda los datos generales definidos en la clase Usuario
class Administrador(
    override val id: Int,
    override var nombre: String,
    override var correo: String
) : Usuario(id, nombre, correo) {

    // Muestra en consola la informacion principal del administrador
    override fun mostrarInformacion() {
        println("ID: $id")
        println("Administrador: $nombre")
        println("Correo: $correo")
    }
}