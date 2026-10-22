package com.importsv.app.exception

// Excepcion utilizada cuando un dato no cumple con las reglas de validacion del sistema
class ValidacionException(
    message: String
) : Exception(message)