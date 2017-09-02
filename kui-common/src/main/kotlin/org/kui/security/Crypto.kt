package org.kui.security

import org.kui.util.getProperty
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Crypto {
    val secureRandom = SecureRandom.getInstanceStrong()!!

    val securityTokenSalt: ByteArray

    val systemEncryptionKey: SecretKeySpec

    init {
        securityTokenSalt = ByteArray(20)
        secureRandom.nextBytes(securityTokenSalt)

        val propertyCategoryKey: String = "security"
        val systemEncryptionKeyBase64 = getProperty(propertyCategoryKey, "system.encryption.key")
        if (systemEncryptionKeyBase64.trim().length == 0) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(AES_KEY_SIZE, secureRandom)
            val key = keyGen.generateKey()
            val systemEncryptionKeyBase64 = encode(key.encoded)
            println("System encryption key not set. Set property system.encryption.key to $systemEncryptionKeyBase64 to use new encryption key.")
            throw SecurityException("System encryption key not set.")
        } else {
            systemEncryptionKey = SecretKeySpec(decode(systemEncryptionKeyBase64), "AES")
        }
    }

    fun encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decode(text: String): ByteArray {
        return Base64.getDecoder().decode(text)
    }


    fun encrypt(nonce: ByteArray, additionAuthenticatedData: ByteArray, plainText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_AES_GCM, SECURITY_PROVIDER)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, systemEncryptionKey, spec)
        cipher.updateAAD(additionAuthenticatedData)
        return cipher.doFinal(plainText)
    }

    fun decrypt(nonce: ByteArray, additionAuthenticatedData: ByteArray, cipherText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_AES_GCM, SECURITY_PROVIDER)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.DECRYPT_MODE, systemEncryptionKey, spec)
        cipher.updateAAD(additionAuthenticatedData)
        return cipher.doFinal(cipherText)
    }

    fun passwordHash(salt: String, password: String) : ByteArray {
        val pbeKeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), PASSWORD_HASH_ITERATIONS, PASSWORD_HASH_KEY_SIZE)
        val secretKeyFactory = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM_PBKDF2_HMAC_SHA512)
        return secretKeyFactory.generateSecret(pbeKeySpec).encoded
    }

    fun securityTokenHash(securityToken: String) : ByteArray {
        val pbeKeySpec = PBEKeySpec(securityToken.toCharArray(), securityTokenSalt, PASSWORD_HASH_ITERATIONS, PASSWORD_HASH_KEY_SIZE)
        val secretKeyFactory = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM_PBKDF2_HMAC_SHA512)
        return secretKeyFactory.generateSecret(pbeKeySpec).encoded
    }

    fun createSecurityToken() : String {
        var tokenBytes = ByteArray(20)
        secureRandom.nextBytes(tokenBytes)
        return Base64.getEncoder().encodeToString(tokenBytes)
    }
}