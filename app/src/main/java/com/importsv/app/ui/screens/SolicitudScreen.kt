
package com.importsv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.importsv.app.auth.LocalUser
import com.importsv.app.model.Cliente
import com.importsv.app.model.Solicitud
import com.importsv.app.repository.SolicitudRepository
import com.importsv.app.service.SolicitudService
import com.importsv.app.validation.FormValidator

private object SolicitudesSesion {
    val servicio = SolicitudService(SolicitudRepository())
    var proximoId = 1
}

@Composable
fun SolicitudScreen(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    var enlace by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "Nueva solicitud",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Ingresa los datos del producto que deseas importar."
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = enlace,
            onValueChange = { enlace = it },
            label = { Text("Enlace del producto") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = producto,
            onValueChange = { producto = it },
            label = { Text("Nombre del producto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones (opcional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = if (guardado) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val error = FormValidator.enlace(enlace)
                    ?: if (producto.isBlank()) {
                        "El nombre del producto es obligatorio."
                    } else null
                        ?: if (categoria.isBlank()) {
                            "La categoría es obligatoria."
                        } else null
                            ?: FormValidator.cantidad(cantidad)

                if (error != null) {
                    mensaje = error
                    guardado = false
                } else {
                    val cliente = Cliente(
                        id = usuario.id,
                        nombre = usuario.nombre,
                        correo = usuario.correo,
                        telefono = ""
                    )

                    val solicitud = Solicitud(
                        id = SolicitudesSesion.proximoId,
                        cliente = cliente,
                        enlaceProducto = enlace.trim(),
                        nombreProducto = producto.trim(),
                        categoria = categoria.trim(),
                        cantidad = cantidad.toInt(),
                        observaciones = observaciones
                            .takeIf { it.isNotBlank() }
                    )

                    val resultado = SolicitudesSesion.servicio
                        .registrarSolicitud(solicitud)

                    if (resultado) {
                        mensaje = "Solicitud #${solicitud.id} " +
                                "registrada correctamente."
                        guardado = true

                        SolicitudesSesion.proximoId++

                        enlace = ""
                        producto = ""
                        categoria = ""
                        cantidad = ""
                        observaciones = ""
                    } else {
                        mensaje = "No se pudo registrar la solicitud."
                        guardado = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar solicitud")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio")
        }
    }
}
