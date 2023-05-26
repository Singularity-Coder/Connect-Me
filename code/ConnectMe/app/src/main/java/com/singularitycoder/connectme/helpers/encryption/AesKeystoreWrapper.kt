package com.singularitycoder.connectme.helpers.encryption

import android.annotation.SuppressLint
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// https://medium.com/@charanolati/symmetric-encryption-using-aes-algorithm-android-a5be7574d4d
// AES Encryption will be available after API 23+ (ANDROID M)
class AesKeystoreWrapper {

    companion object {
        const val AES_NOPAD_TRANS = "AES/GCM/NoPadding" //Format - ”Algorithm/Mode/Padding”
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "Keyalaisasf"
    }

    private fun createKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }

    @RequiresApi(23)
    fun createSymmetricKey(): SecretKey? = try {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            /* keystoreAlias = */ KEY_ALIAS,
            /* purposes = */ KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            //.setUserAuthenticationRequired(true) //  requires lock screen, invalidated if lock screen is disabled
            //.setUserAuthenticationValidityDurationSeconds(120) // only available x seconds from password authentication. -1 requires finger print - every time
            //.setKeySize(256) // Set key size
            // To Set Certificate Values instead of maual initialization of certificate
            //.setCertificateNotBefore(startDate) // By default, this date is Jan 1 1970.
            //.setCertificateNotAfter(endDate) // By default, this date is Jan 1 2048.
            //.setCertificateSerialNumber(number) // By default, the serial number is 1.
            //.setCertificateSubject(x500Principal) // By default, the subject is CN=fake.
            .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
            .build()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    } catch (_: Exception) {
        null
    }

    fun decryptData(hashMap: HashMap<String, ByteArray>): String {
        val encryptedBytes = Base64.decode(hashMap[CipherUtils.ENC_VALUE], Base64.NO_WRAP)
        val ivBytes = Base64.decode(hashMap[CipherUtils.IV_VALUE], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.DECRYPT_MODE, getSymmetricKey(), GCMParameterSpec(128, ivBytes))
        }
        return cipher.doFinal(encryptedBytes).toString(Charsets.UTF_8)
    }

    fun encryptData(data: ByteArray): HashMap<String, ByteArray> {
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.ENCRYPT_MODE, getSymmetricKey())
        }
        val eiv = (Base64.encodeToString(cipher.iv, Base64.NO_WRAP)).toByteArray()
        val edata = (Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)).toByteArray()
        return hashMapOf(Pair(CipherUtils.IV_VALUE, eiv), Pair(CipherUtils.ENC_VALUE, edata))
    }

    fun decryptNoBase(ivBytes: ByteArray, encryptedBytes: ByteArray): String {
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.DECRYPT_MODE, getSymmetricKey(), GCMParameterSpec(128, ivBytes))
        }
        return cipher.doFinal(encryptedBytes).toString(Charsets.UTF_8)
    }

    @SuppressLint("NewApi")
    fun getSymmetricKey(): SecretKey {
        /*val keysore = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return keysore.secretKey*/
        val keyStore = createKeyStore()
        if (!isKeyExists(keyStore)) {
            createSymmetricKey()
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun removeKeyStoreKey() {
        val keyStore = createKeyStore()
        if (isKeyExists(keyStore)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    fun isKeyExists(keyStore: KeyStore): Boolean {
        val aliases = keyStore.aliases()
        while (aliases.hasMoreElements()) {
            return (KEY_ALIAS == aliases.nextElement())
        }
        return false
    }

    fun getCipher(): Cipher {
        val key = getSymmetricKey()
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        return cipher
    }
}