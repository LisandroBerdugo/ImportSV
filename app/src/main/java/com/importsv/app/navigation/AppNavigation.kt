
package com.importsv.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.importsv.app.firebase.FirebaseRepository
import com.importsv.app.firebase.PerfilFb

import com.importsv.app.ui.screens.FirebaseLoginScreen
import com.importsv.app.ui.screens.FirebaseRegisterScreen
import com.importsv.app.ui.screens.FirebasePanelConPedidos
import com.importsv.app.ui.screens.FirebaseNuevaSolicitud
import com.importsv.app.ui.screens.FirebaseListadoSolicitudes
import com.importsv.app.ui.screens.FirebaseMisCotizaciones
import com.importsv.app.ui.screens.FirebaseAdminCotizaciones
import com.importsv.app.ui.screens.FirebaseMisPedidos
import com.importsv.app.ui.screens.FirebaseAdminPedidos

@Composable
fun AppNavigation() {

    var pantalla by remember {
        mutableStateOf("cargando")
    }

    var perfil by remember {
        mutableStateOf<PerfilFb?>(null)
    }

    var error by remember {
        mutableStateOf("")
    }

    var revisionSesion by remember {
        mutableIntStateOf(0)
    }

    // =====================================
    // COMPROBAR SESIÓN FIREBASE
    // =====================================

    LaunchedEffect(revisionSesion) {

        val usuario = FirebaseRepository.usuarioActual()

        if (usuario == null) {

            perfil = null
            error = ""
            pantalla = "login"

        } else {

            pantalla = "cargando"
            error = ""

            FirebaseRepository.leerPerfil { resultado, mensaje ->

                if (
                    FirebaseRepository.usuarioActual()?.uid ==
                    usuario.uid
                ) {

                    if (resultado != null) {
                        perfil = resultado
                        pantalla = "home"
                    } else {
                        perfil = null

                        error = mensaje
                            ?: "No fue posible consultar el perfil."

                        pantalla = "error"
                    }
                }
            }
        }
    }

    // =====================================
    // BOTÓN ATRÁS DE ANDROID
    // =====================================

    BackHandler(
        enabled = pantalla in listOf(
            "registro",
            "nueva",
            "solicitudes",
            "cotizaciones",
            "pedidos",
            "adminSolicitudes",
            "adminCotizaciones",
            "adminPedidos"
        )
    ) {

        pantalla = if (pantalla == "registro") {
            "login"
        } else {
            "home"
        }
    }

    // =====================================
    // NAVEGACIÓN
    // =====================================

    when (pantalla) {

        "cargando" -> {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator()

                Spacer(Modifier.height(16.dp))

                Text("Verificando sesión...")
            }
        }

        // =================================
        // LOGIN
        // =================================

        "login" -> {

            FirebaseLoginScreen(
                onIngresar = {
                    revisionSesion++
                },
                onRegistro = {
                    pantalla = "registro"
                }
            )
        }

        // =================================
        // REGISTRO
        // =================================

        "registro" -> {

            FirebaseRegisterScreen(
                onRegistrado = {
                    revisionSesion++
                },
                onVolver = {
                    pantalla = "login"
                }
            )
        }

        // =================================
        // PANEL PRINCIPAL
        // =================================

        "home" -> {

            val usuario = perfil

            if (usuario != null) {

                FirebasePanelConPedidos(
                    perfil = usuario,

                    onNueva = {
                        pantalla = "nueva"
                    },

                    onSolicitudes = {
                        pantalla = "solicitudes"
                    },

                    onCotizaciones = {
                        pantalla = "cotizaciones"
                    },

                    onPedidos = {
                        pantalla = "pedidos"
                    },

                    onAdminSolicitudes = {
                        if (usuario.rol == "admin") {
                            pantalla = "adminSolicitudes"
                        }
                    },

                    onAdminCotizaciones = {
                        if (usuario.rol == "admin") {
                            pantalla = "adminCotizaciones"
                        }
                    },

                    onAdminPedidos = {
                        if (usuario.rol == "admin") {
                            pantalla = "adminPedidos"
                        }
                    },

                    onSalir = {
                        perfil = null
                        error = ""
                        pantalla = "login"
                    }
                )
            }
        }

        // =================================
        // NUEVA SOLICITUD
        // =================================

        "nueva" -> {

            if (perfil != null) {
                FirebaseNuevaSolicitud(
                    onVolver = {
                        pantalla = "home"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    pantalla = "login"
                }
            }
        }

        // =================================
        // MIS SOLICITUDES
        // =================================

        "solicitudes" -> {

            if (perfil != null) {
                FirebaseListadoSolicitudes(
                    esAdmin = false,
                    onVolver = {
                        pantalla = "home"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    pantalla = "login"
                }
            }
        }

        // =================================
        // MIS COTIZACIONES
        // =================================

        "cotizaciones" -> {

            if (perfil != null) {
                FirebaseMisCotizaciones(
                    onVolver = {
                        pantalla = "home"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    pantalla = "login"
                }
            }
        }

        // =================================
        // MIS PEDIDOS
        // =================================

        "pedidos" -> {

            if (perfil != null) {
                FirebaseMisPedidos(
                    onVolver = {
                        pantalla = "home"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    pantalla = "login"
                }
            }
        }

        // =================================
        // ADMINISTRAR SOLICITUDES
        // =================================

        "adminSolicitudes" -> {

            if (perfil?.rol == "admin") {

                FirebaseListadoSolicitudes(
                    esAdmin = true,
                    onVolver = {
                        pantalla = "home"
                    }
                )

            } else {
                LaunchedEffect(Unit) {
                    pantalla = "home"
                }
            }
        }

        // =================================
        // ADMINISTRAR COTIZACIONES
        // =================================

        "adminCotizaciones" -> {

            if (perfil?.rol == "admin") {

                FirebaseAdminCotizaciones(
                    onVolver = {
                        pantalla = "home"
                    }
                )

            } else {
                LaunchedEffect(Unit) {
                    pantalla = "home"
                }
            }
        }

        // =================================
        // ADMINISTRAR PEDIDOS
        // =================================

        "adminPedidos" -> {

            if (perfil?.rol == "admin") {

                FirebaseAdminPedidos(
                    onVolver = {
                        pantalla = "home"
                    }
                )

            } else {
                LaunchedEffect(Unit) {
                    pantalla = "home"
                }
            }
        }

        // =================================
        // ERROR DE PERFIL
        // =================================

        "error" -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Error al cargar la cuenta",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        revisionSesion++
                    }
                ) {
                    Text("Reintentar")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        FirebaseRepository.salir()
                        perfil = null
                        error = ""
                        pantalla = "login"
                    }
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}
