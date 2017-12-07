package org.kui.model

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.security.Crypto
import java.security.SecureRandom
import java.util.*
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec



/**
 * Created by tlaukkan on 7/8/2017.
 */
class CryptoTest {

    // AES-GCM parameters
    val AES_KEY_SIZE = 128 // in bits
    val GCM_NONCE_LENGTH = 12 // in bytes
    val GCM_TAG_LENGTH = 16 // in bytes

    @Test
    @Ignore
    fun testEncryption() {
        DOMConfigurator.configure("log4j.xml")

        val nonce = "test-nonce".toByteArray()
        val aad = "test-aad".toByteArray()

        val plainTextString = "test-data"

        val plainTextBytes = "test-data".toByteArray()

        val cipherTextBytes = Crypto.encrypt(nonce, aad, plainTextBytes)

        val plainTextBytes2 = Crypto.decrypt(nonce, aad, cipherTextBytes)

        val plainTextString2 = String(plainTextBytes2)

        Assert.assertEquals(plainTextString, plainTextString2)

    }

    @Test
    @Ignore
    fun testCipher() {
        var testNum = 0 // pass

        val input = "Hello AES-GCM World!".toByteArray()

        // Initialise random and generate key
        val random = SecureRandom.getInstanceStrong()
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, random)
        val key = keyGen.generateKey()

        // Encrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE")
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        random.nextBytes(nonce)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val aad = "Whatever I like".toByteArray()
        cipher.updateAAD(aad)

        val cipherText = cipher.doFinal(input)

        // Decrypt; nonce is shared implicitly
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        // EXPECTED: Uncommenting this will cause an AEADBadTagException when decrypting
        // because AAD value is altered
        if (testNum == 1) aad[1]++

        cipher.updateAAD(aad)

        // EXPECTED: Uncommenting this will cause an AEADBadTagException when decrypting
        // because the encrypted data has been altered
        if (testNum == 2) cipherText[10]++

        // EXPECTED: Uncommenting this will cause an AEADBadTagException when decrypting
        // because the tag has been altered
        if (testNum == 3) cipherText[cipherText.size - 2]++

        try {
            val plainText = cipher.doFinal(cipherText)
            if (testNum != 0) {
                println("Test Failed: expected AEADBadTagException not thrown")
            } else {
                // check if the decryption result matches
                if (Arrays.equals(input, plainText)) {
                    println("Test Passed: match!")
                } else {
                    println("Test Failed: result mismatch!")
                    println(String(plainText))
                }
            }
        } catch (ex: AEADBadTagException) {
            if (testNum == 0) {
                println("Test Failed: unexpected ex " + ex)
                ex.printStackTrace()
            } else {
                println("Test Passed: expected ex " + ex)
            }
        }
    }

}