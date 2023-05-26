package com.singularitycoder.connectme.helpers.encryption

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.singularitycoder.connectme.BuildConfig
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

// https://www.baeldung.com/java-aes-encryption-decryption
// https://www.devglan.com/corejava/java-aes-encypt-decrypt
// https://medium.com/@charanolati/symmetric-encryption-using-aes-algorithm-android-a5be7574d4d
// https://github.com/charanolati/Android-encryption-sample
// https://github.com/d0nutptr/Android-Security-Examples/blob/master/Cryptography/app/src/main/java/com/iismathwizard/cryptonote/Crypto.java

object CipherUtils {
    private const val key = "aesEncryptionKey" // This is not safe. Either access from JNI or use Google's keystore
    private const val initVector = "encryptionIntVec"
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val INIT_VECTOR = "11111111"

    private val iv = IvParameterSpec(initVector.toByteArray(Charsets.UTF_8))
    private val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")

    const val SALT_VALUE = "salt_value"
    const val IV_VALUE = "iv_value"
    const val ENC_VALUE = "encrypt_value"

    // Padding issue
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

    fun encrypt2(
        input: String,
        algorithm: String? = ALGORITHM,
        key: SecretKey? = generateKey(128),
        iv: IvParameterSpec? = generateIv()
    ): String? = try {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val cipherText = cipher.doFinal(input.toByteArray())
        android.util.Base64.encodeToString(
            /* input = */ cipherText,
            /* flags = */ android.util.Base64.DEFAULT
        )
    } catch (_: Exception) {
        null
    }

    fun decrypt2(
        cipherText: String?,
        algorithm: String? = ALGORITHM,
        key: SecretKey? = generateKey(128),
        iv: IvParameterSpec? = generateIv()
    ): String? = try {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val plainText = cipher.doFinal(
            android.util.Base64.decode(
                /* str = */ cipherText,
                /* flags = */ android.util.Base64.DEFAULT
            )
        )
        String(plainText)
    } catch (_: Exception) {
        null
    }

    fun encrypt3(originalText: String): String {
        val ini = INIT_VECTOR // create a string to add in the initial binary code for extra security
        var cu = 0
        val arr = IntArray(11111111) // create an array

        // iterate through the string
        for (i in 0 until originalText.length) {
            arr[i] = originalText[i].code // put the ascii value of each character in the array
            cu++
        }
        var encryptedText = ""
        val bin = IntArray(111) // create another array
        var idx = 0

        // run a loop of the size of string
        for (i1 in 0 until cu) {
            var temp = arr[i1] // get the ascii value at position i1 from the first array

            // run the second nested loop of same size and set 0 value in the second array
            for (j in 0 until cu) bin[j] = 0
            idx = 0

            // run a while for temp > 0
            while (temp > 0) {
                bin[idx++] = temp % 2 // store the temp module of 2 in the 2nd array
                temp /= 2
            }
            var dig = ""
            var temps: String

            // run a loop of size 7
            for (j in 0..6) {
                temps = Integer.toString(bin[j]) // convert the integer to string
                dig += temps // add the string using concatenation function
            }
            var revs = ""

            // reverse the string
            for (j in dig.length - 1 downTo 0) {
                val ca = dig[j]
                revs += ca.toString()
            }
            encryptedText += revs
        }
        encryptedText = ini + encryptedText // add the extra string to the binary code

        return encryptedText // return the encrypted code
    }

    fun decrypt3(encryptedText: String): String {
        val ini = INIT_VECTOR // create the same initial string as in encode class
        var flag = true

        // run a loop of size 8
        for (i in 0..7) {
            if (ini[i] != encryptedText[i]) { // check if the initial value is same
                flag = false
                break
            }
        }
        var textValue = ""

        // reverse the encrypted code
        for (i in 8 until encryptedText.length) {
            val ch = encryptedText[i]
            textValue += ch.toString()
        }

        val arr = Array(11101) { IntArray(8) } // create a 2 dimensional array
        var ind1 = -1
        var ind2 = 0

        // run a loop of size of the encrypted code
        for (i in 0 until textValue.length) {
            // check if the position of the string if divisible by 7
            if (i % 7 == 0) {
                ind1++ // start the value in other column of the 2D array
                ind2 = 0
                val ch = textValue[i]
                arr[ind1][ind2] = ch - '0'
                ind2++
            } else {
                val ch = textValue[i] // otherwise store the value in the same column
                arr[ind1][ind2] = ch - '0'
                ind2++
            }
        }

        val num = IntArray(11111) // create an array
        var nind = 0
        var tem = 0
        var cu = 0

        // run a loop of size of the column
        for (i in 0..ind1) {
            cu = 0
            tem = 0
            // convert binary to decimal and add them from each column and store in the array
            for (j in 6 downTo 0) {
                val tem1 = Math.pow(2.0, cu.toDouble()).toInt()
                tem += arr[i][j] * tem1
                cu++
            }
            num[nind++] = tem
        }
        var originalText = ""
        var ch: Char
        // convert the decimal ascii number to its char value and add them to form a decrypted string using conception function
        for (i in 0 until nind) {
            ch = num[i].toChar()
            originalText += ch.toString()
        }
        println("Desc: text 11 - $originalText")

        // check if the encrypted code was generated for this algorithm
        return if (textValue.length % 7 == 0 && flag) {
            originalText // return the decrypted code
        } else {
            "Invalid Code" // otherwise return an invalid message
        }
    }

    fun generateKey(n: Int): SecretKey? = try {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(n)
        keyGenerator.generateKey()
    } catch (_: Exception) {
        null
    }

    fun getKeyFromPassword(password: String, salt: String): SecretKey? = try {
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 256)
        SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    } catch (_: Exception) {
        null
    }

    fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }
}

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

fun Context.openLockScreenSettings() {
    val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
    startActivity(intent)
}

fun String.toByteArray() = this.toByteArray(Charsets.UTF_8)

fun ByteArray.fromByteToString() = String(this, Charsets.UTF_8)

