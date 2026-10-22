
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
import com.importsv.app.validation.FormValidator
import java.util.Locale

private fun dinero(valor: Double): String {
    return String.format(Locale.US, "$%.2f", valor)
}

// Diseño general
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
        Spacer(Modifier.height(18.dp))

        Text(
            titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

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
    }
}

// Tarjetas reutilizables del menú
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
            Modifier.padding(16.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// PANEL DEL CLIENTE
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

        Text("Bienvenido, ${usuario.nombre}")

        Text(
            usuario.correo,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        Opcion(
            "Nueva solicitud",
            "Solicitar la importación de un producto",
            onNueva
        )

        Opcion(
            "Mis solicitudes y cotizaciones",
            "Revisar solicitudes, ver y aceptar cotizaciones",
            onMisSolicitudes
        )

        Opcion(
            "Mis pedidos",
            "Consultar estado e historial",
            onPedidos
        )

        Spacer(Modifier.height(18.dp))

        HorizontalDivider()

        Text(
            "Opciones de demostración",
            style = MaterialTheme.typography.titleSmall
        )

        Opcion(
            "Entrar al panel administrativo",
            "Acceso académico sin control real de permisos",
            onAdmin
        )

        OutlinedButton(
            onClick = onSalir,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// NUEVA SOLICITUD
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

    Pantalla("Nueva solicitud", onVolver) {

        OutlinedTextField(
            value = enlace,
            onValueChange = { enlace = it },
            label = { Text("Enlace del producto") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            )
        )

        OutlinedTextField(
            value = producto,
            onValueChange = { producto = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val cantidadNumero = cantidad.toIntOrNull()

                mensaje = when {

                    FormValidator.enlace(enlace) != null ->
                        FormValidator.enlace(enlace)!!

                    producto.isBlank() ->
                        "El nombre del producto es obligatorio"

                    categoria.isBlank() ->
                        "La categoría es obligatoria"

                    cantidadNumero == null || cantidadNumero <= 0 ->
                        "La cantidad debe ser mayor que cero"

                    DemoStore.crearSolicitud(
                        usuario,
                        enlace,
                        producto,
                        categoria,
                        cantidadNumero,
                        notas
                    ) -> {
                        enlace = ""
                        producto = ""
                        categoria = ""
                        cantidad = ""
                        notas = ""

                        "Solicitud registrada correctamente"
                    }

                    else ->
                        "No fue posible registrar la solicitud"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar solicitud")
        }

        Text(mensaje)
    }
}

// MIS SOLICITUDES Y COTIZACIONES
@Composable
fun MisSolicitudesPanel(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.misSolicitudes(usuario.id)

    Pantalla("Mis solicitudes", onVolver) {

        if (solicitudes.isEmpty()) {
            Text("Todavía no tienes solicitudes")
        }

        solicitudes.forEach { solicitud ->

            val cotizacion = DemoStore.cotizacionPara(
                solicitud.id
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "#${solicitud.id} · ${solicitud.nombreProducto}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Categoría: ${solicitud.categoria}"
                    )

                    Text(
                        "Cantidad: ${solicitud.cantidad}"
                    )

                    Spacer(Modifier.height(8.dp))

                    if (cotizacion == null) {

                        Text("Pendiente de cotización")

                    } else {

                        Text(
                            "Cotización #${cotizacion.id}: " +
                                    dinero(cotizacion.calcularTotal())
                        )

                        Text(
                            "Producto: " +
                                    dinero(cotizacion.precioProducto)
                        )

                        Text(
                            "Envío: " +
                                    dinero(cotizacion.envioInternacional)
                        )

                        Text(
                            "Importación: " +
                                    dinero(cotizacion.cargosImportacion)
                        )

                        Text(
                            "Comisión: " +
                                    dinero(cotizacion.comision)
                        )

                        Text(
                            "Entrega local: " +
                                    dinero(cotizacion.entregaLocal)
                        )

                        var mensaje by remember(solicitud.id) {
                            mutableStateOf("")
                        }

                        Spacer(Modifier.height(10.dp))

                        if (cotizacion.aceptada) {

                            Text("Cotización aceptada")

                        } else {

                            Button(
                                onClick = {
                                    mensaje = DemoStore.aceptarCotizacion(
                                        cotizacion.id,
                                        usuario.id
                                    )
                                }
                            ) {
                                Text(
                                    "Aceptar cotización y generar pedido"
                                )
                            }
                        }

                        if (mensaje.isNotEmpty()) {
                            Text(mensaje)
                        }
                    }
                }
            }
        }
    }
}

// MIS PEDIDOS
@Composable
fun MisPedidosPanel(
    usuario: LocalUser,
    onVolver: () -> Unit
) {
    val pedidos = DemoStore.misPedidos(usuario.id)

    Pantalla("Mis pedidos", onVolver) {

        if (pedidos.isEmpty()) {
            Text("Todavía no tienes pedidos aprobados")
        }

        pedidos.forEach { pedido ->

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Pedido #${pedido.id} · " +
                                pedido.solicitud.nombreProducto,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Estado: " +
                                pedido.estado.name.replace('_', ' ')
                    )

                    Text(
                        "Total: " +
                                dinero(pedido.cotizacion.calcularTotal())
                    )

                    Text(
                        "Historial: " +
                                pedido.historialEstados.joinToString(" → ") {
                                    it.name.replace('_', ' ')
                                }
                    )
                }
            }
        }
    }
}

// PANEL ADMINISTRATIVO
@Composable
fun AdminPanel(
    onSolicitudes: () -> Unit,
    onPedidos: () -> Unit,
    onReportes: () -> Unit,
    onClientes: () -> Unit,
    onVolver: () -> Unit
) {
    Pantalla("Administración", onVolver) {

        Text(
            "Modo académico sin permisos reales. " +
                    "No utilizar con información privada."
        )

        Spacer(Modifier.height(12.dp))

        Opcion(
            "Solicitudes y cotizaciones",
            "Crear cotizaciones para solicitudes de clientes",
            onSolicitudes
        )

        Opcion(
            "Gestión de pedidos",
            "Avanzar pedidos siguiendo estados válidos",
            onPedidos
        )

        Opcion(
            "Clientes",
            "Consultar los clientes con solicitudes registradas",
            onClientes
        )

        Opcion(
            "Reportes",
            "Visualizar estadísticas de las operaciones",
            onReportes
        )
    }
}

// GESTIÓN DE COTIZACIONES
@Composable
fun GestionCotizacionesPanel(
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.solicitudes()

    Pantalla("Gestionar cotizaciones", onVolver) {

        if (solicitudes.isEmpty()) {
            Text(
                "No hay solicitudes. Registra una " +
                        "desde el panel del cliente."
            )
        }

        solicitudes.forEach { solicitud ->

            val creada = DemoStore.cotizacionPara(
                solicitud.id
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Solicitud #${solicitud.id}: " +
                                solicitud.nombreProducto,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Cliente: ${solicitud.cliente.nombre}"
                    )

                    Text(
                        "Cantidad: ${solicitud.cantidad}"
                    )

                    Spacer(Modifier.height(8.dp))

                    if (creada != null) {

                        Text(
                            "Cotización creada: " +
                                    dinero(creada.calcularTotal())
                        )

                    } else {

                        val nombres = listOf(
                            "Precio del producto",
                            "Envío internacional",
                            "Cargos de importación",
                            "Comisión",
                            "Entrega local"
                        )

                        val campos = remember(solicitud.id) {
                            mutableStateListOf(
                                "", "", "", "", ""
                            )
                        }

                        var mensaje by remember(solicitud.id) {
                            mutableStateOf("")
                        }

                        nombres.forEachIndexed { i, nombre ->

                            OutlinedTextField(
                                value = campos[i],
                                onValueChange = {
                                    campos[i] = it
                                },
                                label = {
                                    Text(nombre)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val valores = campos.map {
                                    it.toDoubleOrNull()
                                }

                                mensaje = if (
                                    valores.any { it == null }
                                ) {
                                    "Ingresa cinco importes numéricos"
                                } else {
                                    DemoStore.crearCotizacion(
                                        solicitud.id,
                                        valores.filterNotNull()
                                    )
                                }
                            }
                        ) {
                            Text("Generar cotización")
                        }

                        if (mensaje.isNotEmpty()) {
                            Text(mensaje)
                        }
                    }
                }
            }
        }
    }
}

// GESTIÓN DE PEDIDOS
@Composable
fun GestionPedidosPanel(
    onVolver: () -> Unit
) {
    val pedidos = DemoStore.pedidos()

    Pantalla("Administrar pedidos", onVolver) {

        if (pedidos.isEmpty()) {
            Text(
                "No hay pedidos. El cliente debe aceptar " +
                        "primero una cotización."
            )
        }

        pedidos.forEach { pedido ->

            var mensaje by remember(pedido.id) {
                mutableStateOf("")
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Pedido #${pedido.id}: " +
                                pedido.solicitud.nombreProducto,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Cliente: ${pedido.solicitud.cliente.nombre}"
                    )

                    Text(
                        "Estado: " +
                                pedido.estado.name.replace('_', ' ')
                    )

                    Text(
                        "Historial: " +
                                pedido.historialEstados.joinToString(" → ") {
                                    it.name.replace('_', ' ')
                                }
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        enabled = pedido.estado != EstadoPedido.ENTREGADO,
                        onClick = {
                            mensaje = DemoStore.avanzarPedido(
                                pedido.id
                            )
                        }
                    ) {
                        Text("Avanzar al siguiente estado")
                    }

                    if (mensaje.isNotBlank()) {
                        Text(mensaje)
                    }
                }
            }
        }
    }
}

// LISTADO DE CLIENTES
@Composable
fun ClientesPanel(
    onVolver: () -> Unit
) {
    val clientes = DemoStore.clientes()

    Pantalla("Clientes", onVolver) {

        if (clientes.isEmpty()) {
            Text("Aún no hay clientes con solicitudes")
        }

        clientes.forEach { cliente ->

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        cliente.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(cliente.correo)

                    Text(
                        "Solicitudes: " +
                                DemoStore.misSolicitudes(cliente.id).size
                    )
                }
            }
        }
    }
}

// REPORTES Y ESTADÍSTICAS
@Composable
fun ReportesPanel(
    onVolver: () -> Unit
) {
    val solicitudes = DemoStore.solicitudes()
    val cotizaciones = DemoStore.cotizaciones()
    val pedidos = DemoStore.pedidos()

    Pantalla("Reportes del avance", onVolver) {

        Text(
            "Datos de demostración en memoria"
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Clientes con solicitudes: " +
                    DemoStore.clientes().size
        )

        Text(
            "Solicitudes: ${solicitudes.size}"
        )

        Text(
            "Cotizaciones: ${cotizaciones.size}"
        )

        Text(
            "Cotizaciones aceptadas: " +
                    cotizaciones.count { it.aceptada }
        )

        Text(
            "Pedidos activos: " +
                    pedidos.count {
                        it.estado != EstadoPedido.ENTREGADO
                    }
        )

        Text(
            "Pedidos entregados: " +
                    pedidos.count {
                        it.estado == EstadoPedido.ENTREGADO
                    }
        )

        Text(
            "Total cotizado: " +
                    dinero(
                        cotizaciones.sumOf {
                            it.calcularTotal()
                        }
                    )
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "Solicitudes por categoría",
            style = MaterialTheme.typography.titleMedium
        )

        solicitudes
            .groupingBy { it.categoria }
            .eachCount()
            .forEach { (categoria, total) ->
                Text("$categoria: $total")
            }

        Spacer(Modifier.height(12.dp))

        Text(
            "Pedidos por estado",
            style = MaterialTheme.typography.titleMedium
        )

        pedidos
            .groupingBy { it.estado }
            .eachCount()
            .forEach { (estado, total) ->
                Text(
                    "${estado.name.replace('_', ' ')}: $total"
                )
            }
    }
}
