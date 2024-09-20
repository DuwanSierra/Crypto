package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ChaCha20Main {

    private static final int[] CONSTANTS = {0x61707865, 0x3320646E, 0x79622D32, 0x6B206574};

    public static void main(String[] args) throws Exception {
        byte[] key = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);  // 32 bytes key
        byte[] nonce = "000000000000".getBytes(StandardCharsets.UTF_8);               // 12 bytes nonce
        System.out.println(key.length);
        System.out.println(nonce.length);
        int counter = 1;
        byte[] plaintext = ("Este es un mensaje de prueba para los algoritmos SALSA20 y CHACHA21" +
                "").getBytes(StandardCharsets.UTF_8);

        String ciphertext = encrypt(plaintext, key, nonce, counter);
        System.out.println("Texto cifrado (Base64): " + ciphertext);

        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
        decrypt(encryptedBytes, key, nonce, counter);
    }

    public static String encrypt(byte[] pText, byte[] key, byte[] nonce, int counter) throws Exception {
        int[] state = initializeState(key, nonce, counter);
        printState(state, 0);  // Estado inicial

        // Ejecutar 20 rondas de ChaCha20 (10 pares de columnas y diagonales)
        for (int i = 1; i <= 10; i++) {
            chachaRound(state);
            printState(state, i);  // Estado después de cada ronda
        }

        // Generar la palabra cifrante (keystream)
        byte[] keystream = generateKeystream(state);
        System.out.println("Palabra cifrante: " + bytesToHex(keystream));

        // Cifrar el texto
        byte[] encryptedText = xor(pText, keystream);

        // Mostrar el estado final del nonce
        System.out.println("Estado final del nonce: " + bytesToHex(nonce));

        // Codificar el texto cifrado en Base64 para que pueda ser representado como UTF-8
        return Base64.getEncoder().encodeToString(encryptedText);
    }

    private static int[] initializeState(byte[] key, byte[] nonce, int counter) {

        int[] state = new int[16];

        // Constantes de ChaCha
        System.arraycopy(CONSTANTS, 0, state, 0, CONSTANTS.length);

        // Copiar la clave
        ByteBuffer keyBuffer = ByteBuffer.wrap(key);
        for (int i = 0; i < 8; i++) {
            state[i + 4] = keyBuffer.getInt();
        }

        // Copiar el contador y nonce
        state[12] = counter;
        ByteBuffer nonceBuffer = ByteBuffer.wrap(nonce);
        for (int i = 0; i < 3; i++) {
            state[i + 13] = nonceBuffer.getInt();
        }

        return state;
    }

    private static void chachaRound(int[] state) {
        // 10 rounds of column and diagonal rounds
        for (int i = 0; i < 10; i++) {
            // Column rounds
            quarterRound(state, 0, 4, 8, 12);
            quarterRound(state, 1, 5, 9, 13);
            quarterRound(state, 2, 6, 10, 14);
            quarterRound(state, 3, 7, 11, 15);
            // Diagonal rounds
            quarterRound(state, 0, 5, 10, 15);
            quarterRound(state, 1, 6, 11, 12);
            quarterRound(state, 2, 7, 8, 13);
            quarterRound(state, 3, 4, 9, 14);
        }
    }

    private static void quarterRound(int[] state, int a, int b, int c, int d) {
        state[a] += state[b]; state[d] ^= state[a]; state[d] = Integer.rotateLeft(state[d], 16);
        state[c] += state[d]; state[b] ^= state[c]; state[b] = Integer.rotateLeft(state[b], 12);
        state[a] += state[b]; state[d] ^= state[a]; state[d] = Integer.rotateLeft(state[d], 8);
        state[c] += state[d]; state[b] ^= state[c]; state[b] = Integer.rotateLeft(state[b], 7);
    }

    private static byte[] generateKeystream(int[] state) {
        ByteBuffer buffer = ByteBuffer.allocate(67);
        for (int value : state) {
            buffer.putInt(value);
        }
        return buffer.array();
    }

    private static byte[] xor(byte[] pText, byte[] keystream) {
        byte[] result = new byte[pText.length];
        for (int i = 0; i < pText.length; i++) {
            result[i] = (byte) (pText[i] ^ keystream[i]);
        }
        return result;
    }

    private static void printState(int[] state, int round) {
        System.out.printf("Estado de la ronda %d:%n", round);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.printf("%08x ", state[i * 4 + j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    public static void decrypt(byte[] cText, byte[] key, byte[] nonce, int counter) throws Exception {
        // Reutilizar el mismo proceso para generar la palabra cifrante
        int[] state = initializeState(key, nonce, counter);
        printState(state, 0);  // Estado inicial

        // Ejecutar 20 rondas de ChaCha20 (10 pares de columnas y diagonales)
        for (int i = 1; i <= 10; i++) {
            chachaRound(state);
            printState(state, i);  // Estado después de cada ronda
        }

        // Generar la palabra cifrante (keystream)
        byte[] keystream = generateKeystream(state);
        System.out.println("Palabra cifrante para descifrado: " + bytesToHex(keystream));

        // Descifrar el texto
        byte[] decryptedText = xor(cText, keystream);

        // Mostrar el estado final del nonce
        System.out.println("Estado final del nonce (descifrado): " + bytesToHex(nonce));

        // Convertir el texto descifrado a String
        System.out.println( new String(decryptedText, StandardCharsets.UTF_8));
    }

}

