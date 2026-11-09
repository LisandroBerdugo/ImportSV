
package com.importsv.app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// ========================================
// MODELOS PARA FIRESTORE
// ========================================

data class PerfilFb(
    val uid: String,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class SolicitudFb(
    val id: String,
    val ownerUid: String,
    val cliente: String,
    val enlace: String,
    val producto: String,
    val categoria: String,
    val cantidad: Int,
    val observaciones: String,
    val creado: Long
)

data class CotizacionFb(
    val id: String,
    val solicitudId: String,
    val ownerUid: String,
    val producto: String,
    val precio: Double,
    val envio: Double,
    val importacion: Double,
    val comision: Double,
    val entrega: Double,
    val aceptada: Boolean
) {
    val total: Double
        get() = precio + envio + importacion + comision + entrega
}

data class PedidoFb(
    val id: String,
    val solicitudId: String,
    val ownerUid: String,
    val producto: String,
    val total: Double,
    val estado: String,
    val historial: List<String>
)

// ========================================
// REPOSITORIO PRINCIPAL
// ========================================

object FirebaseRepository {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    val estados = listOf(
        "APROBADO",
        "COMPRADO",
        "EN_TRANSITO",
        "EN_ADUANA",
        "RECIBIDO",
        "LISTO_ENTREGA",
        "ENTREGADO"
    )

    fun usuarioActual(): FirebaseUser? {
        return auth.currentUser
    }

    fun salir() {
        auth.signOut()
    }

    private fun mensaje(e: Exception?): String {
        return e?.localizedMessage
            ?: "No fue posible completar la operación."
    }

    // ========================================
    // REGISTRO CON CORREO
    // ========================================

    fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        fin: (String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(
            correo.trim(),
            clave
        ).addOnSuccessListener { resultado ->

            val usuario = resultado.user

            if (usuario == null) {
                fin("No fue posible obtener el usuario.")
                return@addOnSuccessListener
            }

            crearPerfilSiNoExiste(
                usuario,
                nombre.trim(),
                fin
            )

        }.addOnFailureListener {
            fin(mensaje(it))
        }
    }

    // ========================================
    // LOGIN CON CORREO
    // ========================================

    fun entrar(
        correo: String,
        clave: String,
        fin: (String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(
            correo.trim(),
            clave
        ).addOnSuccessListener { resultado ->

            val usuario = resultado.user

            if (usuario == null) {
                fin("Sesión inválida.")
                return@addOnSuccessListener
            }

            crearPerfilSiNoExiste(
                usuario,
                usuario.displayName ?: "Cliente",
                fin
            )

        }.addOnFailureListener {
            fin(mensaje(it))
        }
    }

    // ========================================
    // LOGIN CON GOOGLE
    // ========================================

    fun entrarConGoogle(
        token: String,
        fin: (String?) -> Unit
    ) {
        val credencial =
            GoogleAuthProvider.getCredential(token, null)

        auth.signInWithCredential(credencial)
            .addOnSuccessListener { resultado ->

                val usuario = resultado.user

                if (usuario == null) {
                    fin("No se pudo autenticar con Google.")
                    return@addOnSuccessListener
                }

                crearPerfilSiNoExiste(
                    usuario,
                    usuario.displayName ?: "Cliente",
                    fin
                )

            }.addOnFailureListener {
                fin(mensaje(it))
            }
    }

    // ========================================
    // CREAR PERFIL DE USUARIO
    // ========================================

    fun crearPerfilSiNoExiste(
        usuario: FirebaseUser,
        nombre: String,
        fin: (String?) -> Unit
    ) {
        val referencia = db.collection("usuarios")
            .document(usuario.uid)

        referencia.get().addOnSuccessListener { documento ->

            if (documento.exists()) {
                fin(null)
                return@addOnSuccessListener
            }

            val datos = mapOf(
                "nombre" to nombre.ifBlank { "Cliente" },
                "correo" to (usuario.email ?: ""),
                "rol" to "cliente",
                "creado" to FieldValue.serverTimestamp()
            )

            referencia.set(datos)
                .addOnSuccessListener {
                    fin(null)
                }
                .addOnFailureListener {
                    fin(mensaje(it))
                }

        }.addOnFailureListener {
            fin(mensaje(it))
        }
    }

    // ========================================
    // CONSULTAR PERFIL
    // ========================================

    fun leerPerfil(
        fin: (PerfilFb?, String?) -> Unit
    ) {
        val usuario = auth.currentUser

        if (usuario == null) {
            fin(null, "No existe una sesión activa.")
            return
        }

        db.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    fin(null, "No existe el perfil del usuario.")
                    return@addOnSuccessListener
                }

                val perfil = PerfilFb(
                    uid = usuario.uid,
                    nombre = documento.getString("nombre")
                        ?: "Cliente",
                    correo = documento.getString("correo")
                        ?: "",
                    rol = documento.getString("rol")
                        ?: "cliente"
                )

                fin(perfil, null)

            }.addOnFailureListener {
                fin(null, mensaje(it))
            }
    }

    // ========================================
    // REGISTRAR SOLICITUD
    // ========================================

    fun crearSolicitud(
        enlace: String,
        producto: String,
        categoria: String,
        cantidad: Int,
        observaciones: String,
        fin: (String?) -> Unit
    ) {
        val usuario = auth.currentUser

        if (usuario == null) {
            fin("Debes iniciar sesión.")
            return
        }

        if (
            enlace.isBlank() ||
            producto.isBlank() ||
            categoria.isBlank() ||
            cantidad <= 0
        ) {
            fin("Verifica los datos de la solicitud.")
            return
        }

        val referencia = db.collection("solicitudes")
            .document()

        val datos = mapOf(
            "ownerUid" to usuario.uid,
            "cliente" to (
                    usuario.displayName
                        ?: usuario.email
                        ?: "Cliente"
                    ),
            "enlace" to enlace.trim(),
            "producto" to producto.trim(),
            "categoria" to categoria.trim(),
            "cantidad" to cantidad,
            "observaciones" to observaciones.trim(),
            "creado" to System.currentTimeMillis()
        )

        referencia.set(datos)
            .addOnSuccessListener {
                fin(null)
            }
            .addOnFailureListener {
                fin(mensaje(it))
            }
    }

    // ========================================
    // GENERAR COTIZACIÓN
    // ========================================

    fun crearCotizacion(
        solicitud: SolicitudFb,
        importes: List<Double>,
        fin: (String?) -> Unit
    ) {
        if (
            importes.size != 5 ||
            importes.any { !it.isFinite() || it < 0.0 }
        ) {
            fin("Los importes son inválidos.")
            return
        }

        val datos = mapOf(
            "solicitudId" to solicitud.id,
            "ownerUid" to solicitud.ownerUid,
            "producto" to solicitud.producto,
            "precio" to importes[0],
            "envio" to importes[1],
            "importacion" to importes[2],
            "comision" to importes[3],
            "entrega" to importes[4],
            "aceptada" to false
        )

        db.collection("cotizaciones")
            .document(solicitud.id)
            .set(datos)
            .addOnSuccessListener {
                fin(null)
            }
            .addOnFailureListener {
                fin(mensaje(it))
            }
    }

    // ========================================
    // ACEPTAR COTIZACIÓN
    // ========================================

    fun aceptarCotizacion(
        cotizacion: CotizacionFb,
        fin: (String?) -> Unit
    ) {
        db.collection("cotizaciones")
            .document(cotizacion.id)
            .update("aceptada", true)
            .addOnSuccessListener {
                fin(null)
            }
            .addOnFailureListener {
                fin(mensaje(it))
            }
    }

    // ========================================
    // CREAR PEDIDO
    // ========================================

    fun crearPedido(
        cotizacion: CotizacionFb,
        fin: (String?) -> Unit
    ) {
        if (!cotizacion.aceptada) {
            fin("La cotización todavía no fue aceptada.")
            return
        }

        val datos = mapOf(
            "solicitudId" to cotizacion.solicitudId,
            "ownerUid" to cotizacion.ownerUid,
            "producto" to cotizacion.producto,
            "total" to cotizacion.total,
            "estado" to "APROBADO",
            "historial" to listOf("APROBADO")
        )

        db.collection("pedidos")
            .document(cotizacion.solicitudId)
            .set(datos)
            .addOnSuccessListener {
                fin(null)
            }
            .addOnFailureListener {
                fin(mensaje(it))
            }
    }

    // ========================================
    // ACTUALIZAR ESTADO DEL PEDIDO
    // ========================================

    fun avanzarPedido(
        pedido: PedidoFb,
        fin: (String?) -> Unit
    ) {
        val posicion = estados.indexOf(pedido.estado)

        if (posicion == -1 || posicion >= estados.lastIndex) {
            fin("El pedido no puede avanzar a otro estado.")
            return
        }

        val siguiente = estados[posicion + 1]

        val referencia = db.collection("pedidos")
            .document(pedido.id)

        db.runTransaction { transaccion ->

            val documento = transaccion.get(referencia)

            val estadoActual =
                documento.getString("estado") ?: ""

            if (estadoActual != pedido.estado) {
                throw IllegalStateException(
                    "El pedido fue actualizado. Recarga los datos."
                )
            }

            val historial = (
                    documento.get("historial") as? List<*>
                    )?.filterIsInstance<String>().orEmpty()

            transaccion.update(
                referencia,
                mapOf(
                    "estado" to siguiente,
                    "historial" to (historial + siguiente)
                )
            )

        }.addOnSuccessListener {
            fin(null)
        }.addOnFailureListener {
            fin(mensaje(it))
        }
    }

    // ========================================
    // ESCUCHAR SOLICITUDES EN TIEMPO REAL
    // ========================================

    fun escucharSolicitudes(
        admin: Boolean,
        fin: (List<SolicitudFb>, String?) -> Unit
    ): ListenerRegistration {

        val uid = auth.currentUser?.uid.orEmpty()
        val coleccion = db.collection("solicitudes")

        val consulta = if (admin) {
            coleccion
        } else {
            coleccion.whereEqualTo("ownerUid", uid)
        }

        return consulta.addSnapshotListener { snapshot, error ->

            if (error != null) {
                fin(emptyList(), mensaje(error))
                return@addSnapshotListener
            }

            val lista = snapshot?.documents?.map { doc ->
                SolicitudFb(
                    id = doc.id,
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    cliente = doc.getString("cliente").orEmpty(),
                    enlace = doc.getString("enlace").orEmpty(),
                    producto = doc.getString("producto").orEmpty(),
                    categoria = doc.getString("categoria").orEmpty(),
                    cantidad = (doc.getLong("cantidad") ?: 0L).toInt(),
                    observaciones = doc.getString("observaciones").orEmpty(),
                    creado = doc.getLong("creado") ?: 0L
                )
            }.orEmpty().sortedByDescending { it.creado }

            fin(lista, null)
        }
    }

    // ========================================
    // ESCUCHAR COTIZACIONES
    // ========================================

    fun escucharCotizaciones(
        admin: Boolean,
        fin: (List<CotizacionFb>, String?) -> Unit
    ): ListenerRegistration {

        val uid = auth.currentUser?.uid.orEmpty()
        val coleccion = db.collection("cotizaciones")

        val consulta = if (admin) {
            coleccion
        } else {
            coleccion.whereEqualTo("ownerUid", uid)
        }

        return consulta.addSnapshotListener { snapshot, error ->

            if (error != null) {
                fin(emptyList(), mensaje(error))
                return@addSnapshotListener
            }

            val lista = snapshot?.documents?.map { doc ->
                CotizacionFb(
                    id = doc.id,
                    solicitudId = doc.getString("solicitudId").orEmpty(),
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    producto = doc.getString("producto").orEmpty(),
                    precio = doc.getDouble("precio") ?: 0.0,
                    envio = doc.getDouble("envio") ?: 0.0,
                    importacion = doc.getDouble("importacion") ?: 0.0,
                    comision = doc.getDouble("comision") ?: 0.0,
                    entrega = doc.getDouble("entrega") ?: 0.0,
                    aceptada = doc.getBoolean("aceptada") ?: false
                )
            }.orEmpty()

            fin(lista, null)
        }
    }

    // ========================================
    // ESCUCHAR PEDIDOS
    // ========================================

    fun escucharPedidos(
        admin: Boolean,
        fin: (List<PedidoFb>, String?) -> Unit
    ): ListenerRegistration {

        val uid = auth.currentUser?.uid.orEmpty()
        val coleccion = db.collection("pedidos")

        val consulta = if (admin) {
            coleccion
        } else {
            coleccion.whereEqualTo("ownerUid", uid)
        }

        return consulta.addSnapshotListener { snapshot, error ->

            if (error != null) {
                fin(emptyList(), mensaje(error))
                return@addSnapshotListener
            }

            val lista = snapshot?.documents?.map { doc ->
                PedidoFb(
                    id = doc.id,
                    solicitudId = doc.getString("solicitudId").orEmpty(),
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    producto = doc.getString("producto").orEmpty(),
                    total = doc.getDouble("total") ?: 0.0,
                    estado = doc.getString("estado") ?: "APROBADO",
                    historial = (
                            doc.get("historial") as? List<*>
                            )?.filterIsInstance<String>().orEmpty()
                )
            }.orEmpty()

            fin(lista, null)
        }
    }

    // ========================================
    // ESCUCHAR CLIENTES / USUARIOS
    // ========================================

    fun escucharUsuarios(
        fin: (List<PerfilFb>, String?) -> Unit
    ): ListenerRegistration {

        return db.collection("usuarios")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    fin(emptyList(), mensaje(error))
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.map { doc ->
                    PerfilFb(
                        uid = doc.id,
                        nombre = doc.getString("nombre").orEmpty(),
                        correo = doc.getString("correo").orEmpty(),
                        rol = doc.getString("rol") ?: "cliente"
                    )
                }.orEmpty()

                fin(lista, null)
            }
    }
}
