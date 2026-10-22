package com.importsv.app.validation

import android.util.Patterns

object FormValidator {
    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Escribe al menos 3 caracteres"
        else -> null
    }
    fun correo(valor: String): String? = if (!Patterns.EMAIL_ADDRESS.matcher(valor.trim()).matches())
        "Ingresa un correo válido" else null
    fun clave(valor: String): String? = if (valor.length < 6)
        "La contraseña debe contener al menos 6 caracteres" else null
    fun cantidad(valor: String): String? = if ((valor.toIntOrNull() ?: 0) <= 0)
        "La cantidad debe ser un entero mayor que cero" else null
    fun enlace(valor: String): String? {
        val uri = android.net.Uri.parse(valor.trim())
        return if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank())
            "Ingresa un enlace HTTP o HTTPS válido" else null
    }
}
