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

/**
 * Application cryptographic services including:
 * - GCM encrypt with system secret key
 * - Password hashing
 * - Security token generation and hashing
 */
object Crypto {
    /**
     * Secure random used by Crypto implementation to generate cryptographically strong random values.
     */
    private val secureRandom = SecureRandom.getInstanceStrong()!!
    /**
     * Security token salt. Generated on boot as tokens live as long as the instance lives.
     */
    private val securityTokenSalt: ByteArray = ByteArray(20)
    /**
     * The system encryption key. Loaded from property file.
     */
    private val systemEncryptionKey: SecretKeySpec

    /**
     * Static initialization of securityTokenSalt and systemEncryptionKey.
     */
    init {
        secureRandom.nextBytes(securityTokenSalt)

        val propertyCategoryKey = "security"
        val systemEncryptionKeyBase64 = getProperty(propertyCategoryKey, "system.encryption.key")
        if (systemEncryptionKeyBase64.trim().isEmpty()) {
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

    /**
     * Encrypts [plainText] with GSM using given [nonce] and [additionAuthenticatedData]
     * @return cipher text
     */
    fun encrypt(nonce: ByteArray, additionAuthenticatedData: ByteArray, plainText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_AES_GCM, SECURITY_PROVIDER)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, systemEncryptionKey, spec)
        cipher.updateAAD(additionAuthenticatedData)
        return cipher.doFinal(plainText)
    }

    /**
     * Dencrypts [cipherText] with GSM using given [nonce] and [additionAuthenticatedData]
     * @return plain text
     */
    fun decrypt(nonce: ByteArray, additionAuthenticatedData: ByteArray, cipherText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_AES_GCM, SECURITY_PROVIDER)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.DECRYPT_MODE, systemEncryptionKey, spec)
        cipher.updateAAD(additionAuthenticatedData)
        return cipher.doFinal(cipherText)
    }

    /**
     * Calculates [password] hash with given [salt].
     * @return hash bytes
     */
    fun passwordHash(salt: String, password: String) : ByteArray {
        val pbeKeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), PASSWORD_HASH_ITERATIONS, PASSWORD_HASH_KEY_SIZE)
        val secretKeyFactory = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM_PBKDF2_HMAC_SHA512)
        return secretKeyFactory.generateSecret(pbeKeySpec).encoded
    }

    /**
     * Calculates [securityToken] hash.
     * @return hash bytes
     */
    fun securityTokenHash(securityToken: String) : ByteArray {
        val pbeKeySpec = PBEKeySpec(securityToken.toCharArray(), securityTokenSalt, PASSWORD_HASH_ITERATIONS, PASSWORD_HASH_KEY_SIZE)
        val secretKeyFactory = SecretKeyFactory.getInstance(PASSWORD_HASH_ALGORITHM_PBKDF2_HMAC_SHA512)
        return secretKeyFactory.generateSecret(pbeKeySpec).encoded
    }

    /**
     * Generates security token.
     */
    fun createSecurityToken() : String {
        var tokenBytes = ByteArray(20)
        secureRandom.nextBytes(tokenBytes)
        return Base64.getEncoder().encodeToString(tokenBytes)
    }

    /**
     * Encodes [bytes] with as Base64.
     * @return base 64 encoded string
     */
    private fun encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Decodes [text] from base 64 to bytes.
     * @return bytes
     */
    private fun decode(text: String): ByteArray {
        return Base64.getDecoder().decode(text)
    }
}