
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

import com.importsv.app.firebase.FirebaseRepository
import com.importsv.app.firebase.PerfilFb
import com.importsv.app.firebase.SolicitudFb
import com.importsv.app.firebase.CotizacionFb

import java.util.Locale

// ===========================================
// UTILIDADES GENERALES
// ===========================================

private fun formatoDinero(valor: Double): String {
    return String.format(Locale.US, "$%.2f", valor)
}

@Composable
private fun PantallaCotizacion(
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

        Spacer(Modifier.height(16.dp))

        contenido()

        if (onVolver != null) {
            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OpcionCotizacion(
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
        Column(Modifier.padding(18.dp)) {

            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(descripcion)
        }
    }
}

// ===========================================
// NUEVO PANEL PRINCIPAL
// ===========================================

@Composable
fun FirebasePanelCompleto(
    perfil: PerfilFb,
    onNueva: () -> Unit,
    onSolicitudes: () -> Unit,
    onCotizaciones: () -> Unit,
    onAdminSolicitudes: () -> Unit,
    onAdminCotizaciones: () -> Unit,
    onSalir: () -> Unit
) {
    PantallaCotizacion("ImportSV") {

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    "Bienvenido, ${perfil.nombre}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(8.dp))

                Text(perfil.correo)

                Text(
                    "Rol: " + if (perfil.rol == "admin") {
                        "Administrador"
                    } else {
                        "Cliente"
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Gestión de importaciones",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        OpcionCotizacion(
            titulo = "Nueva solicitud",
            descripcion = "Registrar un producto para importar",
            onClick = onNueva
        )

        OpcionCotizacion(
            titulo = "Mis solicitudes",
            descripcion = "Consultar productos solicitados",
            onClick = onSolicitudes
        )

        OpcionCotizacion(
            titulo = "Mis cotizaciones",
            descripcion = "Consultar y aceptar cotizaciones",
            onClick = onCotizaciones
        )

        if (perfil.rol == "admin") {

            Spacer(Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            Text(
                "Administración",
                style = MaterialTheme.typography.titleLarge
            )

            OpcionCotizacion(
                titulo = "Solicitudes de clientes",
                descripcion = "Consultar todas las solicitudes",
                onClick = onAdminSolicitudes
            )

            OpcionCotizacion(
                titulo = "Gestionar cotizaciones",
                descripcion = "Crear cotizaciones para solicitudes",
                onClick = onAdminCotizaciones
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                FirebaseRepository.salir()
                onSalir()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

// ===========================================
// DESGLOSE DE COSTOS
// ===========================================

@Composable
private fun DetalleCotizacion(
    cotizacion: CotizacionFb
) {
    Text(
        "Cotización del producto",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.height(8.dp))

    Text("Producto: ${formatoDinero(cotizacion.precio)}")

    Text(
        "Envío internacional: " +
                formatoDinero(cotizacion.envio)
    )

    Text(
        "Cargos de importación: " +
                formatoDinero(cotizacion.importacion)
    )

    Text(
        "Comisión: " +
                formatoDinero(cotizacion.comision)
    )

    Text(
        "Entrega local: " +
                formatoDinero(cotizacion.entrega)
    )

    Spacer(Modifier.height(10.dp))

    HorizontalDivider()

    Spacer(Modifier.height(10.dp))

    Text(
        "TOTAL: ${formatoDinero(cotizacion.total)}",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

// ===========================================
// MIS COTIZACIONES
// ===========================================

@Composable
fun FirebaseMisCotizaciones(
    onVolver: () -> Unit
) {
    var cotizaciones by remember {
        mutableStateOf<List<CotizacionFb>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        val listener = FirebaseRepository.escucharCotizaciones(
            admin = false
        ) { lista, mensaje ->

            cargando = false

            if (mensaje != null) {
                error = mensaje
                cotizaciones = emptyList()
            } else {
                error = ""
                cotizaciones = lista
            }
        }

        onDispose {
            listener.remove()
        }
    }

    PantallaCotizacion(
        titulo = "Mis cotizaciones",
        onVolver = onVolver
    ) {
        when {

            cargando -> {
                CircularProgressIndicator()
                Text("Cargando cotizaciones...")
            }

            error.isNotBlank() -> {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            cotizaciones.isEmpty() -> {
                Text("Todavía no tienes cotizaciones.")
            }

            else -> {
                Text(
                    "Total de cotizaciones: ${cotizaciones.size}"
                )

                Spacer(Modifier.height(12.dp))

                cotizaciones.forEach { cotizacion ->

                    key(cotizacion.id) {

                        var mensaje by remember {
                            mutableStateOf("")
                        }

                        var procesando by remember {
                            mutableStateOf(false)
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {

                                Text(
                                    cotizacion.producto,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(12.dp))

                                DetalleCotizacion(cotizacion)

                                Spacer(Modifier.height(14.dp))

                                if (cotizacion.aceptada) {

                                    Text(
                                        "Estado: ACEPTADA",
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                } else {

                                    Text("Estado: Pendiente de aceptación")

                                    Spacer(Modifier.height(10.dp))

                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !procesando,
                                        onClick = {
                                            procesando = true
                                            mensaje = ""

                                            FirebaseRepository.aceptarCotizacion(
                                                cotizacion
                                            ) { resultado ->

                                                procesando = false

                                                mensaje = resultado
                                                    ?: "Cotización aceptada correctamente."
                                            }
                                        }
                                    ) {
                                        Text(
                                            if (procesando) {
                                                "Procesando..."
                                            } else {
                                                "Aceptar cotización"
                                            }
                                        )
                                    }
                                }

                                if (mensaje.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(mensaje)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===========================================
// ADMINISTRAR COTIZACIONES
// ===========================================

@Composable
fun FirebaseAdminCotizaciones(
    onVolver: () -> Unit
) {
    var solicitudes by remember {
        mutableStateOf<List<SolicitudFb>>(emptyList())
    }

    var cotizaciones by remember {
        mutableStateOf<List<CotizacionFb>>(emptyList())
    }

    var cargandoSolicitudes by remember {
        mutableStateOf(true)
    }

    var cargandoCotizaciones by remember {
        mutableStateOf(true)
    }

    var errorSolicitudes by remember {
        mutableStateOf("")
    }

    var errorCotizaciones by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        val listenerSolicitudes =
            FirebaseRepository.escucharSolicitudes(
                admin = true
            ) { lista, mensaje ->

                cargandoSolicitudes = false

                if (mensaje == null) {
                    solicitudes = lista
                    errorSolicitudes = ""
                } else {
                    errorSolicitudes = mensaje
                }
            }

        val listenerCotizaciones =
            FirebaseRepository.escucharCotizaciones(
                admin = true
            ) { lista, mensaje ->

                cargandoCotizaciones = false

                if (mensaje == null) {
                    cotizaciones = lista
                    errorCotizaciones = ""
                } else {
                    errorCotizaciones = mensaje
                }
            }

        onDispose {
            listenerSolicitudes.remove()
            listenerCotizaciones.remove()
        }
    }

    PantallaCotizacion(
        titulo = "Gestionar cotizaciones",
        onVolver = onVolver
    ) {

        when {

            cargandoSolicitudes || cargandoCotizaciones -> {
                CircularProgressIndicator()
                Text("Cargando solicitudes y cotizaciones...")
            }

            errorSolicitudes.isNotBlank() -> {
                Text(
                    errorSolicitudes,
                    color = MaterialTheme.colorScheme.error
                )
            }

            errorCotizaciones.isNotBlank() -> {
                Text(
                    errorCotizaciones,
                    color = MaterialTheme.colorScheme.error
                )
            }

            solicitudes.isEmpty() -> {
                Text("No existen solicitudes registradas.")
            }

            else -> {

                Text(
                    "Solicitudes registradas: ${solicitudes.size}"
                )

                Spacer(Modifier.height(12.dp))

                solicitudes.forEach { solicitud ->

                    key(solicitud.id) {

                        val existente = cotizaciones.find {
                            it.solicitudId == solicitud.id
                        }

                        var precio by remember {
                            mutableStateOf("")
                        }

                        var envio by remember {
                            mutableStateOf("")
                        }

                        var importacion by remember {
                            mutableStateOf("")
                        }

                        var comision by remember {
                            mutableStateOf("")
                        }

                        var entrega by remember {
                            mutableStateOf("")
                        }

                        var mensaje by remember {
                            mutableStateOf("")
                        }

                        var guardando by remember {
                            mutableStateOf(false)
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {

                                Text(
                                    solicitud.producto,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "Cliente: ${solicitud.cliente}"
                                )

                                Text(
                                    "Cantidad: ${solicitud.cantidad}"
                                )

                                Spacer(Modifier.height(14.dp))

                                if (existente != null) {

                                    DetalleCotizacion(existente)

                                    Spacer(Modifier.height(10.dp))

                                    Text(
                                        if (existente.aceptada) {
                                            "Estado: Aceptada"
                                        } else {
                                            "Estado: Pendiente de aceptación"
                                        }
                                    )

                                } else {

                                    val campos = listOf(
                                        "Precio del producto" to precio,
                                        "Envío internacional" to envio,
                                        "Cargos de importación" to importacion,
                                        "Comisión" to comision,
                                        "Entrega local" to entrega
                                    )

                                    campos.forEachIndexed { indice, campo ->

                                        OutlinedTextField(
                                            value = campo.second,
                                            onValueChange = { nuevo ->

                                                when (indice) {
                                                    0 -> precio = nuevo
                                                    1 -> envio = nuevo
                                                    2 -> importacion = nuevo
                                                    3 -> comision = nuevo
                                                    4 -> entrega = nuevo
                                                }

                                                mensaje = ""
                                            },
                                            label = {
                                                Text(campo.first)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            enabled = !guardando,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal
                                            )
                                        )

                                        Spacer(Modifier.height(8.dp))
                                    }

                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !guardando,
                                        onClick = {

                                            val valores = listOf(
                                                precio,
                                                envio,
                                                importacion,
                                                comision,
                                                entrega
                                            ).map {
                                                it.trim().toDoubleOrNull()
                                            }

                                            when {
                                                valores.any { it == null } -> {
                                                    mensaje =
                                                        "Completa todos los importes con números válidos."
                                                }

                                                valores.any {
                                                    it != null &&
                                                            (!it.isFinite() || it < 0.0)
                                                } -> {
                                                    mensaje =
                                                        "Los importes no pueden ser negativos."
                                                }

                                                valores.filterNotNull().sum() <= 0.0 -> {
                                                    mensaje =
                                                        "El total de la cotización debe ser mayor que cero."
                                                }

                                                else -> {

                                                    guardando = true
                                                    mensaje = ""

                                                    FirebaseRepository.crearCotizacion(
                                                        solicitud = solicitud,
                                                        importes = valores.filterNotNull()
                                                    ) { resultado ->

                                                        guardando = false

                                                        mensaje = resultado
                                                            ?: "Cotización creada correctamente."
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            if (guardando) {
                                                "Guardando..."
                                            } else {
                                                "Generar cotización"
                                            }
                                        )
                                    }
                                }

                                if (mensaje.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(mensaje)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
