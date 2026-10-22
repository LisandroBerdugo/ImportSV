
package com.importsv.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.importsv.app.auth.LocalAuthRepository
import com.importsv.app.ui.screens.*

@Composable
fun AppNavigation() {

    val context = LocalContext.current

    val auth = remember {
        LocalAuthRepository(context)
    }

    var usuario by remember {
        mutableStateOf(auth.usuarioActual())
    }

    var pantalla by remember {
        mutableStateOf(
            if (usuario == null) "login" else "cliente"
        )
    }

    BackHandler(
        enabled = pantalla !in listOf("login", "cliente")
    ) {
        pantalla = when (pantalla) {
            "registro" -> "login"
            "admin" -> "cliente"
            else -> {
                if (pantalla.startsWith("admin") ||
                    pantalla in listOf("reportes", "clientes")
                ) {
                    "admin"
                } else {
                    "cliente"
                }
            }
        }
    }

    when (pantalla) {

        "login" -> LoginScreen(
            auth = auth,
            onIngresar = {
                usuario = auth.usuarioActual()
                pantalla = "cliente"
            },
            onRegistro = {
                pantalla = "registro"
            }
        )

        "registro" -> RegisterScreen(
            auth = auth,
            onRegistrado = {
                usuario = auth.usuarioActual()
                pantalla = "cliente"
            },
            onVolver = {
                pantalla = "login"
            }
        )

        else -> {
            val actual = usuario

            if (actual == null) {

                LaunchedEffect(Unit) {
                    pantalla = "login"
                }

            } else {

                when (pantalla) {

                    "cliente" -> ClientePanel(
                        usuario = actual,
                        onNueva = {
                            pantalla = "nueva"
                        },
                        onMisSolicitudes = {
                            pantalla = "mis"
                        },
                        onPedidos = {
                            pantalla = "mispedidos"
                        },
                        onAdmin = {
                            pantalla = "admin"
                        },
                        onSalir = {
                            auth.cerrarSesion()
                            usuario = null
                            pantalla = "login"
                        }
                    )

                    "nueva" -> NuevaSolicitudPanel(
                        usuario = actual,
                        onVolver = {
                            pantalla = "cliente"
                        }
                    )

                    "mis" -> MisSolicitudesPanel(
                        usuario = actual,
                        onVolver = {
                            pantalla = "cliente"
                        }
                    )

                    "mispedidos" -> MisPedidosPanel(
                        usuario = actual,
                        onVolver = {
                            pantalla = "cliente"
                        }
                    )

                    "admin" -> AdminPanel(
                        onSolicitudes = {
                            pantalla = "adminsol"
                        },
                        onPedidos = {
                            pantalla = "adminped"
                        },
                        onReportes = {
                            pantalla = "reportes"
                        },
                        onClientes = {
                            pantalla = "clientes"
                        },
                        onVolver = {
                            pantalla = "cliente"
                        }
                    )

                    "adminsol" -> GestionCotizacionesPanel(
                        onVolver = {
                            pantalla = "admin"
                        }
                    )

                    "adminped" -> GestionPedidosPanel(
                        onVolver = {
                            pantalla = "admin"
                        }
                    )

                    "reportes" -> ReportesPanel(
                        onVolver = {
                            pantalla = "admin"
                        }
                    )

                    "clientes" -> ClientesPanel(
                        onVolver = {
                            pantalla = "admin"
                        }
                    )
                }
            }
        }
    }
}
