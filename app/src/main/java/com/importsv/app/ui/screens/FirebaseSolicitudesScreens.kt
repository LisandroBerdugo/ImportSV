
package com.importsv.app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.importsv.app.firebase.FirebaseRepository
import com.importsv.app.firebase.PerfilFb
import com.importsv.app.firebase.SolicitudFb
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// COMPONENTES REUTILIZABLES
// ==========================================

@Composable
private fun ContenedorSolicitudes(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Spacer(Modifier.height(20.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(18.dp))

        contenido()

        if (onVolver != null) {
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OpcionSolicitudes(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatearFecha(valor: Long): String {
    if (valor <= 0L) {
        return "Fecha no disponible"
    }

    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(valor))
}

private fun validarEnlace(valor: String): Boolean {
    val enlace = valor.trim()

    if (enlace.length > 2000) return false

    return try {
        val uri = java.net.URI(enlace)

        uri.scheme?.lowercase(Locale.ROOT) in
                listOf("https", "http") &&
                !uri.host.isNullOrBlank() &&
                uri.host!!.contains(".") &&
                !enlace.contains(" ")
    } catch (_: Exception) {
        false
    }
}

// ==========================================
// PANEL PRINCIPAL
// ==========================================

@Composable
fun FirebasePanelImportSV(
    perfil: PerfilFb,
    onNuevaSolicitud: () -> Unit,
    onMisSolicitudes: () -> Unit,
    onAdministrarSolicitudes: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    ContenedorSolicitudes("ImportSV") {

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Bienvenido, ${perfil.nombre}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(8.dp))

                Text(perfil.correo)

                Spacer(Modifier.height(6.dp))

                Text(
                    "Rol: ${
                        if (perfil.rol == "admin")
                            "Administrador"
                        else
                            "Cliente"
                    }"
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Gestión de importaciones",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        OpcionSolicitudes(
            titulo = "Nueva solicitud",
            descripcion = "Registrar un producto para importar",
            onClick = onNuevaSolicitud
        )

        OpcionSolicitudes(
            titulo = "Mis solicitudes",
            descripcion = "Consultar mis registros en Firestore",
            onClick = onMisSolicitudes
        )

        if (perfil.rol == "admin") {

            Spacer(Modifier.height(18.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Administración",
                style = MaterialTheme.typography.titleLarge
            )

            OpcionSolicitudes(
                titulo = "Solicitudes de clientes",
                descripcion = "Consultar las solicitudes registradas",
                onClick = onAdministrarSolicitudes
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                FirebaseRepository.salir()
                onCerrarSesion()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// ==========================================
// NUEVA SOLICITUD
// ==========================================

@Composable
fun FirebaseNuevaSolicitud(
    onVolver: () -> Unit
) {
    var enlace by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    ContenedorSolicitudes(
        titulo = "Nueva solicitud",
        onVolver = onVolver
    ) {

        Text(
            "Completa la información del producto " +
                    "que deseas importar."
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = enlace,
            onValueChange = {
                enlace = it
                mensaje = ""
            },
            label = { Text("Enlace del producto") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = producto,
            onValueChange = {
                producto = it
                mensaje = ""
            },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = {
                categoria = it
                mensaje = ""
            },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = cantidad,
            onValueChange = {
                cantidad = it
                mensaje = ""
            },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = observaciones,
            onValueChange = {
                observaciones = it
                mensaje = ""
            },
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            minLines = 3
        )

        Spacer(Modifier.height(22.dp))

        Button(
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                val numero = cantidad.toIntOrNull()

                val validacion = when {

                    !validarEnlace(enlace) ->
                        "Ingresa un enlace HTTP o HTTPS válido."

                    producto.isBlank() ->
                        "El nombre del producto es obligatorio."

                    producto.trim().length > 200 ->
                        "El nombre del producto es demasiado largo."

                    categoria.isBlank() ->
                        "La categoría es obligatoria."

                    categoria.trim().length > 100 ->
                        "La categoría es demasiado larga."

                    numero == null || numero <= 0 ->
                        "La cantidad debe ser mayor que cero."

                    numero > 10000 ->
                        "La cantidad máxima es 10000."

                    observaciones.length > 1000 ->
                        "Las observaciones son demasiado largas."

                    else -> null
                }

                if (validacion != null) {
                    esError = true
                    mensaje = validacion
                } else {
                    guardando = true
                    esError = false
                    mensaje = ""

                    FirebaseRepository.crearSolicitud(
                        enlace = enlace.trim(),
                        producto = producto.trim(),
                        categoria = categoria.trim(),
                        cantidad = numero!!,
                        observaciones = observaciones.trim()
                    ) { resultado ->

                        guardando = false

                        if (resultado == null) {
                            esError = false
                            mensaje =
                                "Solicitud registrada correctamente."

                            enlace = ""
                            producto = ""
                            categoria = ""
                            cantidad = ""
                            observaciones = ""
                        } else {
                            esError = true
                            mensaje = resultado
                        }
                    }
                }
            }
        ) {
            Text(
                if (guardando) "Guardando..."
                else "Registrar solicitud"
            )
        }

        if (guardando) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = mensaje,
                color = if (esError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==========================================
// TARJETA PARA MOSTRAR SOLICITUD
// ==========================================

@Composable
private fun TarjetaSolicitud(
    solicitud: SolicitudFb,
    mostrarCliente: Boolean
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = solicitud.producto,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (mostrarCliente) {
                Text("Cliente: ${solicitud.cliente}")
            }

            Text("Categoría: ${solicitud.categoria}")

            Text("Cantidad: ${solicitud.cantidad}")

            Text("Enlace: ${solicitud.enlace}")

            if (solicitud.observaciones.isNotBlank()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "Observaciones: ${solicitud.observaciones}"
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Registrado: ${formatearFecha(solicitud.creado)}"
            )

            Text(
                "ID: ${solicitud.id}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ==========================================
// LISTADO DE SOLICITUDES
// ==========================================

@Composable
fun FirebaseListadoSolicitudes(
    esAdmin: Boolean,
    onVolver: () -> Unit
) {
    var solicitudes by remember {
        mutableStateOf<List<SolicitudFb>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var mensajeError by remember {
        mutableStateOf("")
    }

    DisposableEffect(esAdmin) {

        cargando = true
        mensajeError = ""

        val listener = FirebaseRepository.escucharSolicitudes(
            admin = esAdmin
        ) { registros, error ->

            cargando = false

            if (error != null) {
                mensajeError = error
                solicitudes = emptyList()
            } else {
                solicitudes = registros
                mensajeError = ""
            }
        }

        onDispose {
            listener.remove()
        }
    }

    ContenedorSolicitudes(
        titulo = if (esAdmin)
            "Solicitudes de clientes"
        else
            "Mis solicitudes",
        onVolver = onVolver
    ) {
        if (cargando) {
            CircularProgressIndicator()

            Spacer(Modifier.height(12.dp))

            Text("Cargando solicitudes...")

        } else if (mensajeError.isNotBlank()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )

        } else if (solicitudes.isEmpty()) {
            Text("No hay solicitudes registradas.")

        } else {

            Text(
                "Total de solicitudes: ${solicitudes.size}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            solicitudes.forEach { solicitud ->
                key(solicitud.id) {
                    TarjetaSolicitud(
                        solicitud = solicitud,
                        mostrarCliente = esAdmin
                    )
                }
            }
        }
    }
}
