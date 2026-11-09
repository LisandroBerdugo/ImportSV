
package com.importsv.app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.importsv.app.R
import com.importsv.app.firebase.FirebaseRepository
import com.importsv.app.firebase.PerfilFb
import kotlinx.coroutines.launch

// ========================================
// CONTENEDOR GENERAL DE LAS PANTALLAS
// ========================================

@Composable
private fun FirebaseContenedor(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(32.dp))

        Text(
            text = "ImportSV",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        contenido()
    }
}

// ========================================
// MENSAJES DE ERROR
// ========================================

@Composable
private fun FirebaseMensajeError(mensaje: String) {
    if (mensaje.isNotBlank()) {

        Spacer(Modifier.height(12.dp))

        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// ========================================
// PANTALLA DE INICIO DE SESIÓN
// ========================================

@Composable
fun FirebaseLoginScreen(
    onIngresar: () -> Unit,
    onRegistro: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val credentialManager = remember(context) {
        CredentialManager.create(context)
    }

    var correo by remember {
        mutableStateOf("")
    }

    var clave by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    FirebaseContenedor(
        titulo = "Iniciar sesión"
    ) {

        Text(
            "Bienvenido a ImportSV. Ingresa con tu cuenta."
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                error = ""
            },
            label = {
                Text("Correo electrónico")
            },
            enabled = !cargando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = {
                clave = it
                error = ""
            },
            label = {
                Text("Contraseña")
            },
            enabled = !cargando,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        FirebaseMensajeError(error)

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
            onClick = {

                when {

                    correo.isBlank() -> {
                        error = "Debes ingresar tu correo."
                    }

                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim())
                        .matches() -> {
                        error = "El correo electrónico no es válido."
                    }

                    clave.isBlank() -> {
                        error = "Debes ingresar tu contraseña."
                    }

                    else -> {

                        cargando = true
                        error = ""

                        FirebaseRepository.entrar(
                            correo = correo.trim(),
                            clave = clave
                        ) { resultado ->

                            cargando = false

                            if (resultado == null) {
                                onIngresar()
                            } else {
                                error = resultado
                            }
                        }
                    }
                }
            }
        ) {
            Text("Ingresar con correo")
        }

        Spacer(Modifier.height(16.dp))

        Text("O continúa con")

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
            onClick = {

                cargando = true
                error = ""

                scope.launch {

                    try {

                        val googleOption =
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(
                                    context.getString(
                                        R.string.default_web_client_id
                                    )
                                )
                                .build()

                        val request =
                            GetCredentialRequest.Builder()
                                .addCredentialOption(
                                    googleOption
                                )
                                .build()

                        val result =
                            credentialManager.getCredential(
                                context = context,
                                request = request
                            )

                        val credential = result.credential

                        if (
                            credential is CustomCredential &&
                            credential.type ==
                            GoogleIdTokenCredential
                                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {

                            val googleCredential =
                                GoogleIdTokenCredential
                                    .createFrom(credential.data)

                            FirebaseRepository.entrarConGoogle(
                                token = googleCredential.idToken
                            ) { resultado ->

                                cargando = false

                                if (resultado == null) {
                                    onIngresar()
                                } else {
                                    error = resultado
                                }
                            }

                        } else {

                            cargando = false

                            error =
                                "No fue posible obtener la cuenta de Google."
                        }

                    } catch (e: GetCredentialException) {

                        cargando = false

                        error = e.localizedMessage
                            ?: "No fue posible iniciar sesión con Google."

                    } catch (e: Exception) {

                        cargando = false

                        error = e.localizedMessage
                            ?: "Ocurrió un error durante la autenticación."
                    }
                }
            }
        ) {
            Text("Continuar con Google")
        }

        if (cargando) {

            Spacer(Modifier.height(20.dp))

            CircularProgressIndicator()
        }

        Spacer(Modifier.height(24.dp))

        TextButton(
            enabled = !cargando,
            onClick = onRegistro
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

// ========================================
// PANTALLA DE REGISTRO
// ========================================

@Composable
fun FirebaseRegisterScreen(
    onRegistrado: () -> Unit,
    onVolver: () -> Unit
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var clave by remember {
        mutableStateOf("")
    }

    var confirmar by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    FirebaseContenedor(
        titulo = "Crear cuenta"
    ) {

        Text(
            "Completa el formulario para registrarte en ImportSV."
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                error = ""
            },
            label = {
                Text("Nombre completo")
            },
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                error = ""
            },
            label = {
                Text("Correo electrónico")
            },
            singleLine = true,
            enabled = !cargando,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = {
                clave = it
                error = ""
            },
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmar,
            onValueChange = {
                confirmar = it
                error = ""
            },
            label = {
                Text("Confirmar contraseña")
            },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        FirebaseMensajeError(error)

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
            onClick = {

                error = when {

                    nombre.trim().length < 3 ->
                        "El nombre debe tener al menos 3 caracteres."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo.trim())
                        .matches() ->
                        "Debes ingresar un correo electrónico válido."

                    clave.length < 6 ->
                        "La contraseña debe tener al menos 6 caracteres."

                    clave != confirmar ->
                        "Las contraseñas no coinciden."

                    else -> ""
                }

                if (error.isEmpty()) {

                    cargando = true

                    FirebaseRepository.registrar(
                        nombre = nombre.trim(),
                        correo = correo.trim(),
                        clave = clave
                    ) { resultado ->

                        cargando = false

                        if (resultado == null) {
                            onRegistrado()
                        } else {
                            error = resultado
                        }
                    }
                }
            }
        ) {
            Text("Registrarse")
        }

        if (cargando) {

            Spacer(Modifier.height(16.dp))

            CircularProgressIndicator()
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
            onClick = onVolver
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}

// ========================================
// PANEL PRINCIPAL TEMPORAL DE FIREBASE
// ========================================

@Composable
fun FirebaseHomeScreen(
    perfil: PerfilFb,
    onSalir: () -> Unit
) {

    FirebaseContenedor(
        titulo = "Panel principal"
    ) {

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

                Spacer(Modifier.height(12.dp))

                Text(
                    "Correo: ${perfil.correo}"
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Rol: ${perfil.rol}"
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                FirebaseRepository.salir()
                onSalir()
            }
        ) {
            Text("Cerrar sesión")
        }
    }
}
