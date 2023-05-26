package com.singularitycoder.connectme.helpers.encryption

import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.Key
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

// https://medium.com/@charanolati/symmetric-encryption-using-aes-algorithm-android-a5be7574d4d

@RequiresApi(23)
class AesSaltEncryption {

    companion object {
        const val ALGORTIHM_TYPE = "PBKDF2WithHmacSHA1"
        const val CPR_TRANSFORMATION = "AES/CBC/PKCS7Padding"//API 23+ //https://miro.medium.com/max/2068/1*MNcknQeCrJMhTWx9JlpnKg.png
        const val ENCRYPT_PASSWORD = "charan12345"
    }

    fun encrypt(data: ByteArray): HashMap<String, ByteArray> {
        val salt = ByteArray(256)
        SecureRandom().nextBytes(salt)

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(CPR_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getSecretKey(salt), IvParameterSpec(iv))
        }

        return hashMapOf(Pair(CipherUtils.SALT_VALUE, salt), Pair(CipherUtils.IV_VALUE, iv), Pair(CipherUtils.ENC_VALUE, cipher.doFinal(data)))
    }

    fun decrypt(map: HashMap<String, ByteArray>): String {
        val salt = map[CipherUtils.SALT_VALUE]
        val iv = map[CipherUtils.IV_VALUE]
        val encrypted = map[CipherUtils.ENC_VALUE]
        val cipher = Cipher.getInstance(CPR_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getSecretKey(salt!!), IvParameterSpec(iv))
        }
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun getSecretKey(salt: ByteArray): Key {
        val pbKeySpec = PBEKeySpec(ENCRYPT_PASSWORD.toCharArray(), salt, 1324, 256)
        val keyBytes = SecretKeyFactory.getInstance(ALGORTIHM_TYPE).generateSecret(pbKeySpec).encoded
        return SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES)
    }
}