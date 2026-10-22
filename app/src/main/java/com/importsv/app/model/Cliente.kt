package com.importsv.app.model

// Clase que representa a un cliente dentro del sistema
// Hereda los datos generales definidos en la clase Usuario
class Cliente(
    override val id: Int,
    override var nombre: String,
    override var correo: String,
    var telefono: String
) : Usuario(id, nombre, correo) {

    // Muestra en consola la informacion principal del cliente
    override fun mostrarInformacion() {
        println("ID: $id")
        println("Nombre: $nombre")
        println("Correo: $correo")
        println("Telefono: $telefono")
    }
}