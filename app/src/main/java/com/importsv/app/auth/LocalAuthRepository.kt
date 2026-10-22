
package com.importsv.app.auth

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class LocalUser(
    val id: Int,
    val nombre: String,
    val correo: String
)

class LocalAuthRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("importsv_auth", Context.MODE_PRIVATE)

    private val random = SecureRandom()

    fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): String? {
        val email = correo.trim().lowercase()
        val usuarios = obtenerUsuarios()

        if ((0 until usuarios.length()).any {
                usuarios.getJSONObject(it).getString("correo") == email
            }) {
            return "Ya existe una cuenta con ese correo."
        }

        val salt = ByteArray(16)
        random.nextBytes(salt)

        val id = (0 until usuarios.length())
            .maxOfOrNull {
                usuarios.getJSONObject(it).getInt("id")
            }?.plus(1) ?: 1

        val usuario = JSONObject().apply {
            put("id", id)
            put("nombre", nombre.trim())
            put("correo", email)
            put("salt", codificar(salt))
            put("hash", calcularHash(clave, salt))
        }

        usuarios.put(usuario)

        val guardado = prefs.edit()
            .putString("usuarios", usuarios.toString())
            .putInt("sesion", id)
            .commit()

        return if (guardado) null
        else "No fue posible guardar el usuario."
    }

    fun ingresar(correo: String, clave: String): Boolean {
        val email = correo.trim().lowercase()
        val usuarios = obtenerUsuarios()

        for (i in 0 until usuarios.length()) {
            val usuario = usuarios.getJSONObject(i)

            if (usuario.getString("correo") == email) {
                val salt = Base64.decode(
                    usuario.getString("salt"),
                    Base64.NO_WRAP
                )
                val hash = calcularHash(clave, salt)

                val coincide = MessageDigest.isEqual(
                    Base64.decode(hash, Base64.NO_WRAP),
                    Base64.decode(
                        usuario.getString("hash"),
                        Base64.NO_WRAP
                    )
                )

                if (coincide) {
                    return prefs.edit()
                        .putInt("sesion", usuario.getInt("id"))
                        .commit()
                }
                return false
            }
        }
        return false
    }

    fun usuarioActual(): LocalUser? {
        val id = prefs.getInt("sesion", -1)
        if (id == -1) return null

        val usuarios = obtenerUsuarios()

        for (i in 0 until usuarios.length()) {
            val usuario = usuarios.getJSONObject(i)

            if (usuario.getInt("id") == id) {
                return LocalUser(
                    id = id,
                    nombre = usuario.getString("nombre"),
                    correo = usuario.getString("correo")
                )
            }
        }
        return null
    }

    fun cerrarSesion() {
        prefs.edit().remove("sesion").apply()
    }

    private fun obtenerUsuarios(): JSONArray {
        return JSONArray(prefs.getString("usuarios", "[]") ?: "[]")
    }

    private fun codificar(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun calcularHash(clave: String, salt: ByteArray): String {
        val spec = PBEKeySpec(
            clave.toCharArray(),
            salt,
            120_000,
            256
        )

        return try {
            val factory = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )
            codificar(factory.generateSecret(spec).encoded)
        } finally {
            spec.clearPassword()
        }
    }
}
