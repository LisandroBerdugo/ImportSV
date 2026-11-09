
package com.importsv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import com.importsv.app.firebase.FirebaseRepository
import com.importsv.app.firebase.PerfilFb
import com.importsv.app.firebase.PedidoFb
import com.importsv.app.firebase.CotizacionFb

import java.util.Locale

// ==========================================
// SERVICIO DE PEDIDOS EN FIRESTORE
// ==========================================

private object ServicioPedidosFirebase {

    private val db = FirebaseFirestore.getInstance()

    val estados = listOf(
        "APROBADO",
        "COMPRADO",
        "EN_TRANSITO",
        "EN_ADUANA",
        "RECIBIDO",
        "LISTO_ENTREGA",
        "ENTREGADO"
    )

    // ======================================
    // GENERAR PEDIDO
    // ======================================

    fun generarPedido(
        cotizacion: CotizacionFb,
        fin: (String?) -> Unit
    ) {
        val uid = FirebaseRepository.usuarioActual()?.uid

        if (uid == null) {
            fin("Debes iniciar sesión.")
            return
        }

        if (cotizacion.ownerUid != uid) {
            fin("No tienes acceso a esta cotización.")
            return
        }

        val cotizacionRef = db.collection("cotizaciones")
            .document(cotizacion.id)

        val pedidoRef = db.collection("pedidos")
            .document(cotizacion.solicitudId)

        db.runTransaction { transaction ->

            // Consultar únicamente la cotización.
            // No leer el pedido inexistente, porque
            // sus reglas requieren un propietario.
            val documentoCotizacion =
                transaction.get(cotizacionRef)

            if (!documentoCotizacion.exists()) {
                throw IllegalStateException(
                    "La cotización no existe."
                )
            }

            val propietario =
                documentoCotizacion.getString("ownerUid")

            if (propietario != uid) {
                throw IllegalStateException(
                    "La cotización pertenece a otro usuario."
                )
            }

            if (
                documentoCotizacion.getBoolean("aceptada")
                != true
            ) {
                throw IllegalStateException(
                    "Primero debes aceptar la cotización."
                )
            }

            val solicitudId =
                documentoCotizacion.getString("solicitudId")
                    .orEmpty()

            if (solicitudId != cotizacion.solicitudId) {
                throw IllegalStateException(
                    "Identificador de solicitud incorrecto."
                )
            }

            fun importe(nombre: String): Double {
                return documentoCotizacion
                    .getDouble(nombre) ?: 0.0
            }

            val total =
                importe("precio") +
                        importe("envio") +
                        importe("importacion") +
                        importe("comision") +
                        importe("entrega")

            if (!total.isFinite() || total <= 0.0) {
                throw IllegalStateException(
                    "El total del pedido no es válido."
                )
            }

            val datos = mapOf(
                "solicitudId" to solicitudId,
                "ownerUid" to uid,
                "producto" to
                        documentoCotizacion.getString("producto")
                            .orEmpty(),
                "total" to total,
                "estado" to "APROBADO",
                "historial" to listOf("APROBADO")
            )

            // Firestore aplicará las reglas de
            // creación o actualización según
            // exista o no el documento.
            //
            // Si ya existe, la operación será
            // rechazada porque las reglas de
            // update no permiten reiniciarlo.

            transaction.set(pedidoRef, datos)

            null

        }.addOnSuccessListener {
            fin(null)
        }.addOnFailureListener { error ->

            val detalle = error.localizedMessage.orEmpty()

            val mensaje = when {
                detalle.contains(
                    "PERMISSION_DENIED",
                    ignoreCase = true
                ) ->
                    "Firestore rechazó la operación. " +
                            "Verifica las reglas publicadas " +
                            "y que el pedido no exista previamente."

                else ->
                    detalle.ifBlank {
                        "No fue posible generar el pedido."
                    }
            }

            fin(mensaje)
        }
    }

    // ======================================
    // AVANZAR ESTADO DEL PEDIDO
    // ======================================

    fun avanzarPedido(
        pedido: PedidoFb,
        fin: (String?) -> Unit
    ) {
        val uid = FirebaseRepository.usuarioActual()?.uid

        if (uid == null) {
            fin("Debes iniciar sesión.")
            return
        }

        val referencia = db.collection("pedidos")
            .document(pedido.id)

        db.runTransaction { transaction ->

            val documento = transaction.get(referencia)

            if (!documento.exists()) {
                throw IllegalStateException(
                    "Pedido no encontrado."
                )
            }

            val estadoActual =
                documento.getString("estado").orEmpty()

            val indice = estados.indexOf(estadoActual)

            if (indice == -1) {
                throw IllegalStateException(
                    "El estado actual no es válido."
                )
            }

            if (indice >= estados.lastIndex) {
                throw IllegalStateException(
                    "El pedido ya está entregado."
                )
            }

            val siguiente = estados[indice + 1]

            val historial =
                (documento.get("historial") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()

            if (historial.lastOrNull() != estadoActual) {
                throw IllegalStateException(
                    "El historial del pedido es inconsistente."
                )
            }

            transaction.update(
                referencia,
                mapOf(
                    "estado" to siguiente,
                    "historial" to (historial + siguiente)
                )
            )

            null

        }.addOnSuccessListener {
            fin(null)
        }.addOnFailureListener { error ->
            fin(
                error.localizedMessage
                    ?: "No fue posible actualizar el pedido."
            )
        }
    }

    // ======================================
    // ESCUCHAR PEDIDOS EN TIEMPO REAL
    // ======================================

    fun escucharPedidos(
        admin: Boolean,
        fin: (List<PedidoFb>, String?) -> Unit
    ): ListenerRegistration {

        val uid = FirebaseRepository.usuarioActual()?.uid
            .orEmpty()

        val coleccion = db.collection("pedidos")

        val consulta = if (admin) {
            coleccion
        } else {
            coleccion.whereEqualTo("ownerUid", uid)
        }

        return consulta.addSnapshotListener { snapshot, error ->

            if (error != null) {
                fin(
                    emptyList(),
                    error.localizedMessage
                        ?: "Error consultando los pedidos."
                )
                return@addSnapshotListener
            }

            val registros = snapshot?.documents?.map {
                    documento ->

                PedidoFb(
                    id = documento.id,
                    solicitudId = documento
                        .getString("solicitudId").orEmpty(),
                    ownerUid = documento
                        .getString("ownerUid").orEmpty(),
                    producto = documento
                        .getString("producto").orEmpty(),
                    total = documento
                        .getDouble("total") ?: 0.0,
                    estado = documento
                        .getString("estado") ?: "APROBADO",
                    historial =
                        (documento.get("historial") as? List<*>)
                            ?.filterIsInstance<String>()
                            .orEmpty()
                )
            }.orEmpty()

            fin(registros, null)
        }
    }
}

// ==========================================
// UTILIDADES Y COMPONENTES
// ==========================================

private fun importePedido(valor: Double): String {
    return String.format(Locale.US, "$%.2f", valor)
}

private fun estadoLegible(estado: String): String {
    return estado.replace("_", " ")
}

@Composable
private fun PantallaPedidos(
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
            titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OpcionPedido(
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
                titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(descripcion)
        }
    }
}

// ==========================================
// PANEL PRINCIPAL
// ==========================================

@Composable
fun FirebasePanelConPedidos(
    perfil: PerfilFb,
    onNueva: () -> Unit,
    onSolicitudes: () -> Unit,
    onCotizaciones: () -> Unit,
    onPedidos: () -> Unit,
    onAdminSolicitudes: () -> Unit,
    onAdminCotizaciones: () -> Unit,
    onAdminPedidos: () -> Unit,
    onSalir: () -> Unit
) {
    PantallaPedidos("ImportSV") {

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

        Spacer(Modifier.height(10.dp))

        OpcionPedido(
            "Nueva solicitud",
            "Solicitar la importación de un producto",
            onNueva
        )

        OpcionPedido(
            "Mis solicitudes",
            "Consultar solicitudes registradas",
            onSolicitudes
        )

        OpcionPedido(
            "Mis cotizaciones",
            "Consultar y aceptar cotizaciones",
            onCotizaciones
        )

        OpcionPedido(
            "Mis pedidos",
            "Generar pedidos y consultar seguimiento",
            onPedidos
        )

        if (perfil.rol == "admin") {

            Spacer(Modifier.height(18.dp))

            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            Text(
                "Administración",
                style = MaterialTheme.typography.titleLarge
            )

            OpcionPedido(
                "Solicitudes de clientes",
                "Consultar solicitudes registradas",
                onAdminSolicitudes
            )

            OpcionPedido(
                "Gestionar cotizaciones",
                "Crear cotizaciones",
                onAdminCotizaciones
            )

            OpcionPedido(
                "Gestionar pedidos",
                "Actualizar estados de pedidos",
                onAdminPedidos
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

// ==========================================
// HISTORIAL DEL PEDIDO
// ==========================================

@Composable
private fun HistorialPedido(
    pedido: PedidoFb
) {
    Text(
        "Historial de estados",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.height(8.dp))

    pedido.historial.forEachIndexed { indice, estado ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Text(
                "${indice + 1}. ${estadoLegible(estado)}"
            )
        }
    }
}

// ==========================================
// TARJETA DE PEDIDO
// ==========================================

@Composable
private fun TarjetaPedidoFb(
    pedido: PedidoFb,
    admin: Boolean
) {
    var mensaje by remember(pedido.id) {
        mutableStateOf("")
    }

    var procesando by remember(pedido.id) {
        mutableStateOf(false)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                "Pedido #${pedido.id.take(8)}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Producto: ${pedido.producto}")

            Text("Total: ${importePedido(pedido.total)}")

            Text(
                "Estado: ${estadoLegible(pedido.estado)}"
            )

            Spacer(Modifier.height(14.dp))

            HorizontalDivider()

            Spacer(Modifier.height(14.dp))

            HistorialPedido(pedido)

            if (admin) {

                Spacer(Modifier.height(18.dp))

                val terminado =
                    pedido.estado == "ENTREGADO"

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !terminado && !procesando,
                    onClick = {
                        procesando = true
                        mensaje = ""

                        ServicioPedidosFirebase.avanzarPedido(
                            pedido
                        ) { resultado ->

                            procesando = false

                            mensaje = resultado
                                ?: "Estado actualizado correctamente."
                        }
                    }
                ) {
                    Text(
                        when {
                            procesando -> "Actualizando..."
                            terminado -> "Pedido entregado"
                            else -> "Avanzar al siguiente estado"
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

// ==========================================
// MIS PEDIDOS - CLIENTE
// ==========================================

@Composable
fun FirebaseMisPedidos(
    onVolver: () -> Unit
) {
    var pedidos by remember {
        mutableStateOf<List<PedidoFb>>(emptyList())
    }

    var cotizaciones by remember {
        mutableStateOf<List<CotizacionFb>>(emptyList())
    }

    var cargandoPedidos by remember {
        mutableStateOf(true)
    }

    var cargandoCotizaciones by remember {
        mutableStateOf(true)
    }

    var errorPedidos by remember {
        mutableStateOf("")
    }

    var errorCotizaciones by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        val listenerPedidos =
            ServicioPedidosFirebase.escucharPedidos(
                admin = false
            ) { lista, error ->

                cargandoPedidos = false
                errorPedidos = error.orEmpty()

                if (error == null) {
                    pedidos = lista
                }
            }

        val listenerCotizaciones =
            FirebaseRepository.escucharCotizaciones(
                admin = false
            ) { lista, error ->

                cargandoCotizaciones = false
                errorCotizaciones = error.orEmpty()

                if (error == null) {
                    cotizaciones = lista
                }
            }

        onDispose {
            listenerPedidos.remove()
            listenerCotizaciones.remove()
        }
    }

    PantallaPedidos(
        titulo = "Mis pedidos",
        onVolver = onVolver
    ) {
        if (cargandoPedidos || cargandoCotizaciones) {

            CircularProgressIndicator()

            Text("Cargando información...")

        } else if (
            errorPedidos.isNotBlank() ||
            errorCotizaciones.isNotBlank()
        ) {

            Text(
                errorPedidos.ifBlank { errorCotizaciones },
                color = MaterialTheme.colorScheme.error
            )

        } else {

            val pendientes = cotizaciones.filter { cotizacion ->

                cotizacion.aceptada &&
                        pedidos.none {
                            it.solicitudId == cotizacion.solicitudId
                        }
            }

            if (pendientes.isNotEmpty()) {

                Text(
                    "Cotizaciones listas para generar pedido",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                pendientes.forEach { cotizacion ->

                    key(cotizacion.id) {

                        var generando by remember {
                            mutableStateOf(false)
                        }

                        var mensaje by remember {
                            mutableStateOf("")
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {

                                Text(
                                    cotizacion.producto,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Total: ${importePedido(cotizacion.total)}"
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    enabled = !generando,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {

                                        generando = true
                                        mensaje = ""

                                        ServicioPedidosFirebase.generarPedido(
                                            cotizacion
                                        ) { resultado ->

                                            generando = false

                                            mensaje = resultado
                                                ?: "Pedido generado correctamente."
                                        }
                                    }
                                ) {
                                    Text(
                                        if (generando) {
                                            "Generando..."
                                        } else {
                                            "Generar pedido"
                                        }
                                    )
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

            Spacer(Modifier.height(16.dp))

            Text(
                "Historial y seguimiento",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            if (pedidos.isEmpty()) {
                Text("Todavía no tienes pedidos registrados.")
            }

            pedidos.forEach { pedido ->
                key(pedido.id) {
                    TarjetaPedidoFb(
                        pedido = pedido,
                        admin = false
                    )
                }
            }
        }
    }
}

// ==========================================
// GESTIONAR PEDIDOS - ADMINISTRADOR
// ==========================================

@Composable
fun FirebaseAdminPedidos(
    onVolver: () -> Unit
) {
    var pedidos by remember {
        mutableStateOf<List<PedidoFb>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        val listener =
            ServicioPedidosFirebase.escucharPedidos(
                admin = true
            ) { lista, mensaje ->

                cargando = false
                error = mensaje.orEmpty()

                if (mensaje == null) {
                    pedidos = lista
                }
            }

        onDispose {
            listener.remove()
        }
    }

    PantallaPedidos(
        titulo = "Gestionar pedidos",
        onVolver = onVolver
    ) {
        when {
            cargando -> {

                CircularProgressIndicator()

                Text("Cargando pedidos...")
            }

            error.isNotBlank() -> {

                Text(
                    error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            pedidos.isEmpty() -> {

                Text("No existen pedidos registrados.")
            }

            else -> {

                Text("Total de pedidos: ${pedidos.size}")

                Spacer(Modifier.height(12.dp))

                pedidos.forEach { pedido ->

                    key(pedido.id) {
                        TarjetaPedidoFb(
                            pedido = pedido,
                            admin = true
                        )
                    }
                }
            }
        }
    }
}
