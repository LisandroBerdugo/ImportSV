package com.importsv.app.model

// Clase abstracta que contiene los datos generales de los usuarios del sistema
// Sirve como base para las clases Cliente y Administrador
abstract class Usuario(
    open val id: Int,
    open var nombre: String,
    open var correo: String
) {

    // Metodo que obliga a las clases hijas a mostrar su propia informacion
    abstract fun mostrarInformacion()
}