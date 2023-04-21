package com.singularitycoder.connectme.helpers

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.singularitycoder.connectme.BuildConfig
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

// https://www.devglan.com/corejava/java-aes-encypt-decrypt
object AesEncryption {
    private const val key = "aesEncryptionKey" // This is not safe. Either access from JNI or use Google's keystore
    private const val initVector = "encryptionIntVec"

    fun encrypt(value: String?): String? = try {
        val iv = IvParameterSpec(initVector.toByteArray(Charsets.UTF_8))
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.ENCRYPT_MODE, secretKeySpec, iv)
        }
        val encrypted: ByteArray = cipher.doFinal(value?.toByteArray())
        android.util.Base64.encode(
            /* input = */ encrypted,
            /* flags = */ android.util.Base64.DEFAULT
        ).toString()
    } catch (_: java.lang.Exception) {
        null
    }

    fun decrypt(encrypted: String?): String? = try {
        val iv = IvParameterSpec(initVector.toByteArray(Charsets.UTF_8))
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.DECRYPT_MODE, secretKeySpec, iv)
        }
        val original: ByteArray = cipher.doFinal(
            android.util.Base64.decode(
                /* str = */ encrypted,
                /* flags = */ android.util.Base64.DEFAULT
            )
        )
        String(original)
    } catch (_: java.lang.Exception) {
        null
    }
}

// https://medium.com/@charanolati/symmetric-encryption-using-aes-algorithm-android-a5be7574d4d
// https://github.com/charanolati/Android-encryption-sample

fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

// To check Device has set Screen lock or not
fun isDeviceSecure(keyguardManager: KeyguardManager): Boolean = if (hasMarshmallow()) keyguardManager.isDeviceSecure else keyguardManager.isKeyguardSecure

// To SHOW the Dialog to to Open settings to set a SCREEN LOCK
fun showDeviceSecurityAlert(context: Context): AlertDialog {
    return AlertDialog.Builder(context)
        .setMessage("To Use the App\nSet a Password to Unlock the Screen")
        .setPositiveButton("Settings") { _, _ -> context.openLockScreenSettings() }
        .setNegativeButton("Exit") { _, _ ->
            run {
                exitProcess(0)
            }
        }
        .setCancelable(BuildConfig.DEBUG)
        .show()
}

fun String.toByteArray() = this.toByteArray(Charsets.UTF_8)

fun ByteArray.fromByteToString() = String(this,Charsets.UTF_8)

fun Context.openLockScreenSettings() {
    val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
    startActivity(intent)
}
