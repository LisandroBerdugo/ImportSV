package com.importsv.app.exception

// Excepcion utilizada cuando no se encuentra un registro solicitado dentro del sistema
class RegistroNoEncontradoException(
    message: String
) : Exception(message)