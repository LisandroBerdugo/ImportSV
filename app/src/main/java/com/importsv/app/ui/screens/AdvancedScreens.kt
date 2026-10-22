
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
import com.importsv.app.data.DemoStore
import com.importsv.app.model.EstadoPedido
import java.util.Locale

// ===========================================
// UTILIDADES
// ===========================================

private fun dinero(valor: Double): String {
    return String.format(Locale.US, "$%.2f", valor)
}

private fun validarEnlace(enlace: String): String? {
    val valor = enlace.trim()

    if (valor.isEmpty()) {
        return "El enlace del producto es obligatorio."
    }

    val uri = try {
        java.net.URI(valor)
    } catch (e: Exception) {
        return "Ingresa un enlace válido."
    }

    if (
        uri.scheme !in listOf("https", "http") ||
        uri.host.isNullOrBlank() ||
        !uri.host.contains(".")
    ) {
        return "Ingresa un enlace válido que comience con https://"
    }

    return null
}

// ===========================================
// DISEÑO GENERAL
// ===========================================

@Composable
private fun Pantalla(
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
        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        contenido()

        if (onVolver != null) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ===========================================
// TARJETA REUTILIZABLE
// ===========================================

@Composable
private fun Opcion(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ===========================================
// MENSAJES
// ===========================================

@Composable
private fun Mensaje(
    texto: String,
    error: Boolean = false
) {
    if (texto.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = texto,
            color = if (error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

// ===========================================
// PANEL DEL CLIENTE
// ===========================================

@Composable
fun ClientePanel(
    usuario: LocalUser,
    onNueva: () -> Unit,
    onMisSolicitudes: () -> Unit,
    onPedidos: () -> Unit,
    onAdmin: () -> Unit,
    onSalir: () -> Unit
) {
    Pantalla("ImportSV · Cliente") {

        Text(
            text = "Bienvenido, ${usuario.nombre}",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = usuario.correo,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Mis operaciones",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Opcion(
            titulo = "Nueva solicitud",
            descripcion = "Solicitar la importación de un producto",
            onClick = onNueva
        )

        Opcion(
            titulo = "Mis solicitudes y cotizaciones",
            descripcion = "Consultar solicitudes y aceptar cotizaciones",
            onClick = onMisSolicitudes
        )

        Opcion(
            titulo = "Mis pedidos",
            descripcion = "Consultar pedidos, estados e historial",
            onClick = onPedidos
        )

        Spacer(modifier = Modifier.height(18.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Opcion(
            titulo = "Panel de administración",
            descripcion = "Gestionar solicitudes, cotizaciones, pedidos y reportes",
            onClick = onAdmin
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSalir,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// ===========================================
// REGISTRAR NUEVA SOLICITUD
// ===========================================

@Composable
fun NuevaSolicitudPanel(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    var enlace by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Pantalla(
        titulo = "Nueva solicitud",
        onVolver = onVolver
    ) {
        Text(
            "Completa los datos del producto que deseas importar."
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = enlace,
            onValueChange = {
                enlace = it
                mensaje = ""
            },
            label = { Text("Enlace del producto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = producto,
            onValueChange = {
                producto = it
                mensaje = ""
            },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = {
                categoria = it
                mensaje = ""
            },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = cantidad,
            onValueChange = {
                cantidad = it
                mensaje = ""
            },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notas,
            onValueChange = {
                notas = it
            },
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val cantidadNumero = cantidad.toIntOrNull()
                val errorEnlace = validarEnlace(enlace)

                val resultado = when {
                    errorEnlace != null -> errorEnlace

                    producto.isBlank() ->
                        "El nombre del producto es obligatorio."

                    categoria.isBlank() ->
                        "La categoría es obligatoria."

                    cantidadNumero == null || cantidadNumero <= 0 ->
                        "La cantidad debe ser un número mayor que cero."

                    else -> null
                }

                if (resultado != null) {
                    mensaje = resultado
                    error = true
                } else {
                    val guardado = DemoStore.crearSolicitud(
                        usuario = usuario,
                        enlace = enlace.trim(),
                        nombre = producto.trim(),
                        categoria = categoria.trim(),
                        cantidad = cantidadNumero!!,
                        notas = notas.trim()
                    )

                    if (guardado) {
                        mensaje = "Solicitud registrada correctamente."
                        error = false

                        enlace = ""
                        producto = ""
                        categoria = ""
                        cantidad = ""
                        notas = ""
                    } else {
                        mensaje = "No fue posible registrar la solicitud."
                        error = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar solicitud")
        }

        Mensaje(mensaje, error)
    }
}

// ===========================================
// MIS SOLICITUDES Y COTIZACIONES
// ===========================================

@Composable
fun MisSolicitudesPanel(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.misSolicitudes(usuario.id)

    Pantalla(
        titulo = "Mis solicitudes",
        onVolver = onVolver
    ) {
        if (solicitudes.isEmpty()) {
            Text("Todavía no tienes solicitudes registradas.")
        }

        solicitudes.forEach { solicitud ->

            key(solicitud.id) {
                val cotizacion = DemoStore.cotizacionPara(
                    solicitud.id
                )

                var mensaje by remember {
                    mutableStateOf("")
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Solicitud #${solicitud.id}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = solicitud.nombreProducto
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Categoría: ${solicitud.categoria}")
                        Text("Cantidad: ${solicitud.cantidad}")
                        Text("Enlace: ${solicitud.enlaceProducto}")

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(12.dp))

                        if (cotizacion == null) {
                            Text(
                                "Estado: Pendiente de cotización"
                            )
                        } else {
                            Text(
                                text = "Cotización #${cotizacion.id}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Producto: ${dinero(cotizacion.precioProducto)}"
                            )

                            Text(
                                "Envío internacional: ${dinero(cotizacion.envioInternacional)}"
                            )

                            Text(
                                "Importación: ${dinero(cotizacion.cargosImportacion)}"
                            )

                            Text(
                                "Comisión: ${dinero(cotizacion.comision)}"
                            )

                            Text(
                                "Entrega local: ${dinero(cotizacion.entregaLocal)}"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Total: ${dinero(cotizacion.calcularTotal())}",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (cotizacion.aceptada) {
                                Text("Cotización aceptada.")
                            } else {
                                Button(
                                    onClick = {
                                        mensaje = DemoStore.aceptarCotizacion(
                                            cotizacion.id,
                                            usuario.id
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Aceptar cotización")
                                }
                            }

                            Mensaje(mensaje)
                        }
                    }
                }
            }
        }
    }
}

// ===========================================
// MIS PEDIDOS
// ===========================================

@Composable
fun MisPedidosPanel(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    val pedidos = DemoStore.misPedidos(usuario.id)

    Pantalla(
        titulo = "Mis pedidos",
        onVolver = onVolver
    ) {
        if (pedidos.isEmpty()) {
            Text("Todavía no tienes pedidos registrados.")
        }

        pedidos.forEach { pedido ->
            key(pedido.id) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Pedido #${pedido.id}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            pedido.solicitud.nombreProducto
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Estado actual: ${pedido.estado.name.replace('_', ' ')}"
                        )

                        Text(
                            "Total: ${dinero(pedido.cotizacion.calcularTotal())}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Historial de estados",
                            style = MaterialTheme.typography.titleSmall
                        )

                        pedido.historialEstados.forEach { registro ->
                            Text("• $registro")
                        }
                    }
                }
            }
        }
    }
}

// ===========================================
// PANEL ADMINISTRATIVO
// ===========================================

@Composable
fun AdminPanel(
    onSolicitudes: () -> Unit,
    onPedidos: () -> Unit,
    onReportes: () -> Unit,
    onClientes: () -> Unit,
    onVolver: () -> Unit
) {
    Pantalla(
        titulo = "Panel de administración",
        onVolver = onVolver
    ) {
        Opcion(
            titulo = "Solicitudes y cotizaciones",
            descripcion = "Consultar solicitudes y generar cotizaciones",
            onClick = onSolicitudes
        )

        Opcion(
            titulo = "Gestión de pedidos",
            descripcion = "Actualizar estados y consultar historial",
            onClick = onPedidos
        )

        Opcion(
            titulo = "Clientes",
            descripcion = "Consultar clientes registrados en las solicitudes",
            onClick = onClientes
        )

        Opcion(
            titulo = "Reportes",
            descripcion = "Consultar estadísticas de las operaciones",
            onClick = onReportes
        )
    }
}

// ===========================================
// GESTIÓN DE COTIZACIONES
// ===========================================

@Composable
fun GestionCotizacionesPanel(
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.solicitudes()

    Pantalla(
        titulo = "Gestionar cotizaciones",
        onVolver = onVolver
    ) {
        if (solicitudes.isEmpty()) {
            Text("No hay solicitudes pendientes o registradas.")
        }

        solicitudes.forEach { solicitud ->
            key(solicitud.id) {
                val cotizacion = DemoStore.cotizacionPara(
                    solicitud.id
                )

                var precio by remember { mutableStateOf("") }
                var envio by remember { mutableStateOf("") }
                var importacion by remember { mutableStateOf("") }
                var comision by remember { mutableStateOf("") }
                var entrega by remember { mutableStateOf("") }

                var mensaje by remember { mutableStateOf("") }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Solicitud #${solicitud.id}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(solicitud.nombreProducto)
                        Text("Cliente: ${solicitud.cliente.nombre}")
                        Text("Cantidad: ${solicitud.cantidad}")

                        Spacer(modifier = Modifier.height(14.dp))

                        if (cotizacion != null) {
                            Text(
                                "Cotización creada",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Total: ${dinero(cotizacion.calcularTotal())}"
                            )

                            Text(
                                if (cotizacion.aceptada) {
                                    "Estado: Aceptada"
                                } else {
                                    "Estado: Pendiente de aceptación"
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = precio,
                                onValueChange = { precio = it },
                                label = { Text("Precio del producto") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            OutlinedTextField(
                                value = envio,
                                onValueChange = { envio = it },
                                label = { Text("Envío internacional") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            OutlinedTextField(
                                value = importacion,
                                onValueChange = { importacion = it },
                                label = { Text("Cargos de importación") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            OutlinedTextField(
                                value = comision,
                                onValueChange = { comision = it },
                                label = { Text("Comisión") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            OutlinedTextField(
                                value = entrega,
                                onValueChange = { entrega = it },
                                label = { Text("Entrega local") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val valores = listOf(
                                        precio.toDoubleOrNull(),
                                        envio.toDoubleOrNull(),
                                        importacion.toDoubleOrNull(),
                                        comision.toDoubleOrNull(),
                                        entrega.toDoubleOrNull()
                                    )

                                    if (valores.any { it == null }) {
                                        mensaje = "Completa todos los importes con números válidos."
                                    } else if (
                                        valores.any {
                                            it != null &&
                                                    (!it.isFinite() || it < 0.0)
                                        }
                                    ) {
                                        mensaje = "Los importes no pueden ser negativos."
                                    } else {
                                        mensaje = DemoStore.crearCotizacion(
                                            solicitud.id,
                                            valores.filterNotNull()
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Generar cotización")
                            }

                            Mensaje(mensaje)
                        }
                    }
                }
            }
        }
    }
}

// ===========================================
// GESTIÓN DE PEDIDOS
// ===========================================

@Composable
fun GestionPedidosPanel(
    onVolver: () -> Unit
) {
    val pedidos = DemoStore.pedidos()

    Pantalla(
        titulo = "Administrar pedidos",
        onVolver = onVolver
    ) {
        if (pedidos.isEmpty()) {
            Text("No existen pedidos registrados.")
        }

        pedidos.forEach { pedido ->
            key(pedido.id) {
                var mensaje by remember {
                    mutableStateOf("")
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Pedido #${pedido.id}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(pedido.solicitud.nombreProducto)

                        Text(
                            "Cliente: ${pedido.solicitud.cliente.nombre}"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Estado actual: ${pedido.estado.name.replace('_', ' ')}"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Historial",
                            style = MaterialTheme.typography.titleSmall
                        )

                        pedido.historialEstados.forEach { registro ->
                            Text("• $registro")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            enabled = pedido.estado != EstadoPedido.ENTREGADO,
                            onClick = {
                                mensaje = DemoStore.avanzarPedido(
                                    pedido.id
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Avanzar al siguiente estado")
                        }

                        Mensaje(mensaje)
                    }
                }
            }
        }
    }
}

// ===========================================
// CLIENTES
// ===========================================

@Composable
fun ClientesPanel(
    onVolver: () -> Unit
) {
    val clientes = DemoStore.clientes()

    Pantalla(
        titulo = "Clientes",
        onVolver = onVolver
    ) {
        if (clientes.isEmpty()) {
            Text("No existen clientes con solicitudes registradas.")
        }

        clientes.forEach { cliente ->
            key(cliente.id) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            cliente.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(cliente.correo)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Solicitudes: ${
                                DemoStore.misSolicitudes(cliente.id).size
                            }"
                        )
                    }
                }
            }
        }
    }
}

// ===========================================
// REPORTES
// ===========================================

@Composable
fun ReportesPanel(
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.solicitudes()
    val cotizaciones = DemoStore.cotizaciones()
    val pedidos = DemoStore.pedidos()
    val clientes = DemoStore.clientes()

    Pantalla(
        titulo = "Reportes",
        onVolver = onVolver
    ) {
        Text(
            "Resumen de operaciones",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Clientes: ${clientes.size}")
        Text("Solicitudes: ${solicitudes.size}")
        Text("Cotizaciones: ${cotizaciones.size}")

        Text(
            "Cotizaciones aceptadas: ${
                cotizaciones.count { it.aceptada }
            }"
        )

        Text(
            "Pedidos activos: ${
                pedidos.count {
                    it.estado != EstadoPedido.ENTREGADO
                }
            }"
        )

        Text(
            "Pedidos entregados: ${
                pedidos.count {
                    it.estado == EstadoPedido.ENTREGADO
                }
            }"
        )

        Text(
            "Total cotizado: ${
                dinero(
                    cotizaciones.sumOf {
                        it.calcularTotal()
                    }
                )
            }"
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Solicitudes por categoría",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (solicitudes.isEmpty()) {
            Text("No hay información disponible.")
        } else {
            val categorias = solicitudes
                .groupingBy { it.categoria }
                .eachCount()

            categorias.forEach { (categoria, total) ->
                Text("$categoria: $total")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Pedidos por estado",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (pedidos.isEmpty()) {
            Text("No hay pedidos registrados.")
        } else {
            val estados = pedidos
                .groupingBy { it.estado }
                .eachCount()

            estados.forEach { (estado, total) ->
                Text(
                    "${estado.name.replace('_', ' ')}: $total"
                )
            }
        }
    }
}
