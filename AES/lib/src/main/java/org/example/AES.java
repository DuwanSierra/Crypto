package org.example;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class AES {

    private static final List<String> codes = List.of("20231078001", "20231078002", "20231078003");

    // Tamaño de bloque y clave en bits
    private static final int BLOCK_SIZE = 16; // 128 bits
    private static final int KEY_SIZE = 16; // 128 bits
    private static final int NUM_ROUNDS = 10;

    // S-box y S-box inversa (se generarán dinámicamente)
    private static int[] sBox = new int[256];
    private static int[] invSBox = new int[256];
    // Claves de ronda
    private static int[] roundKeys;

    public static void main(String[] args) {
        // Texto de ejemplo
        String text = "1";
        // Clave de ejemplo
        String keyText = "ClaveSuperSecret";

        Integer polynomial = 0x11B;// Polynomial.getIrreduciblePolynomial(Polynomial.calculateAESModulo(codes));

        // Preparar texto y clave ajustados a 16 bytes
        byte[] plaintext = prepareData(text);
        byte[] key = prepareData(keyText);

        // Generar S-box y S-box inversa
        generateSBoxes(polynomial);

        // Generar claves de ronda
        keyExpansion(key, polynomial);

        // Cifrar
        byte[] ciphertext = encrypt(plaintext, polynomial);
        System.out.println("Texto cifrado: " + Base64.getEncoder().encodeToString(ciphertext));

        // Descifrar
        byte[] decryptedText = decrypt(ciphertext, polynomial);
        System.out.println("Texto descifrado: " + new String(decryptedText, StandardCharsets.UTF_8));
    }

    public static byte[] prepareData(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[BLOCK_SIZE];

        if (data.length >= BLOCK_SIZE) {
            System.arraycopy(data, 0, result, 0, BLOCK_SIZE);
        } else {
            System.arraycopy(data, 0, result, 0, data.length);
        }
        return result;
    }

    public static byte[] encrypt(byte[] input, Integer polynomial) {
        int[] state = new int[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = input[i] & 0xFF;
        }

        addRoundKey(state, 0);

        for (int round = 1; round < NUM_ROUNDS; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state, polynomial);
            addRoundKey(state, round);
        }

        subBytes(state);
        shiftRows(state);
        addRoundKey(state, NUM_ROUNDS);

        byte[] output = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            output[i] = (byte) state[i];
        }
        return output;
    }

    public static byte[] decrypt(byte[] input, Integer polynomial) {
        int[] state = new int[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = input[i] & 0xFF;
        }

        addRoundKey(state, NUM_ROUNDS);

        for (int round = NUM_ROUNDS - 1; round > 0; round--) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, round);
            invMixColumns(state, polynomial);
        }

        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, 0);

        byte[] output = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            output[i] = (byte) state[i];
        }
        return output;
    }

    /** Generar S-box y S-box inversa utilizando el nuevo polinomio irreducible */
    private static void generateSBoxes(int polynomial) {
        for (int i = 0; i < 256; i++) {
            int inv = (i == 0) ? 0 : multiplicativeInverse(i, polynomial);
            sBox[i] = affineTransformation(inv);
            invSBox[sBox[i]] = i;
        }
    }

    /** Transformación afín usada para generar S-box */
    private static int affineTransformation(int b) {
        int result = 0;
        int constant = 0x63; // Constante utilizada en AES estándar
        for (int i = 0; i < 8; i++) {
            int bit = ((b >> i) & 1) ^ ((b >> ((i + 4) % 8)) & 1) ^ ((b >> ((i + 5) % 8)) & 1)
                    ^ ((b >> ((i + 6) % 8)) & 1) ^ ((b >> ((i + 7) % 8)) & 1) ^ ((constant >> i) & 1);
            result |= bit << i;
        }
        return result & 0xFF;
    }

    /** Inverso multiplicativo en GF(2^8) utilizando el nuevo polinomio */
    private static int multiplicativeInverse(int a, int polynomial) {
        int t0 = 0;
        int t1 = 1;
        int r0 = polynomial;
        int r1 = a;

        while (r1 != 0) {
            int q = gfDivide(r0, r1);
            int temp = r0;
            r0 = r1;
            r1 = gfMod(temp, r1);

            temp = t0;
            t0 = t1;
            t1 = gfAdd(temp, gfMultiply(q, t1, polynomial));
        }

        if (r0 != 1) {
            return 0; // No existe inverso multiplicativo
        }
        return t0;
    }

    private static int gfDivide(int a, int b) {
        int shift = gfDegree(a) - gfDegree(b);
        if (shift < 0) {
            return 0;
        }
        return 1 << shift;
    }

    /** Módulo polinomial en GF(2^8) */
    private static int gfMod(int a, int b) {
        while (gfDegree(a) >= gfDegree(b)) {
            int shift = gfDegree(a) - gfDegree(b);
            a ^= b << shift;
        }
        return a;
    }

    /** Calcula el grado de un polinomio */
    private static int gfDegree(int a) {
        int degree = -1;
        while (a != 0) {
            a >>= 1;
            degree++;
        }
        return degree;
    }

    /** Suma en GF(2^8) (XOR) */
    private static int gfAdd(int a, int b) {
        return a ^ b;
    }

    /** Multiplicación en GF(2^8) utilizando el polinomio irreducible */
    private static int gfMultiply(int a, int b, int polynomial) {
        int result = 0;
        while (b != 0) {
            if ((b & 1) != 0) {
                result ^= a;
            }
            a <<= 1;
            if ((a & 0x100) != 0) {
                a ^= polynomial;
            }
            b >>= 1;
        }
        return result & 0xFF;
    }

    /** Expansión de clave para generar claves de ronda */
    private static void keyExpansion(byte[] key, Integer polynomial) {
        int Nk = KEY_SIZE / 4; // Número de palabras en la clave (4 para AES-128)
        int Nr = NUM_ROUNDS; // Número de rondas (10 para AES-128)
        int Nb = 4; // Número de palabras en un bloque (siempre 4 para AES)

        int totalWords = Nb * (Nr + 1); // Total de palabras en la clave expandida

        // Inicializar arreglo de palabras
        roundKeys = new int[totalWords];

        // Copiar la clave original en las primeras Nk palabras
        for (int i = 0; i < Nk; i++) {
            roundKeys[i] = ((key[4 * i] & 0xFF) << 24) |
                    ((key[4 * i + 1] & 0xFF) << 16) |
                    ((key[4 * i + 2] & 0xFF) << 8) |
                    (key[4 * i + 3] & 0xFF);
        }

        // Generar el resto de palabras en la clave expandida
        for (int i = Nk; i < totalWords; i++) {
            int temp = roundKeys[i - 1];
            if (i % Nk == 0) {
                temp = subWord(rotWord(temp)) ^ (rcon(i / Nk, polynomial) << 24);
            }
            roundKeys[i] = roundKeys[i - Nk] ^ temp;
        }
    }

    /** Rotar palabra (RotWord) */
    private static int rotWord(int word) {
        return ((word << 8) | (word >>> 24)) & 0xFFFFFFFF;
    }

    /** Sustituir bytes de la palabra utilizando la S-box (SubWord) */
    private static int subWord(int word) {
        return ((sBox[(word >>> 24) & 0xFF] << 24) |
                (sBox[(word >>> 16) & 0xFF] << 16) |
                (sBox[(word >>> 8) & 0xFF] << 8) |
                sBox[word & 0xFF]) & 0xFFFFFFFF;
    }

    /** Obtener constante de ronda (Rcon) */
    private static int rcon(int n, int polynomial) {
        int c = 1;
        for (int i = 1; i < n; i++) {
            c = gfMultiply(c, 0x02, polynomial);
        }
        return c;
    }

    /** Añadir clave de ronda al estado */
    private static void addRoundKey(int[] state, int round) {
        for (int i = 0; i < 4; i++) { // i es la columna
            int roundKeyWord = roundKeys[round * 4 + i];
            state[i * 4 + 0] ^= (roundKeyWord >>> 24) & 0xFF; // fila 0
            state[i * 4 + 1] ^= (roundKeyWord >>> 16) & 0xFF; // fila 1
            state[i * 4 + 2] ^= (roundKeyWord >>> 8) & 0xFF; // fila 2
            state[i * 4 + 3] ^= roundKeyWord & 0xFF; // fila 3
        }
    }

    /** Sustitución de bytes utilizando la S-box */
    private static void subBytes(int[] state) {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = sBox[state[i]];
        }
    }

    /** Sustitución inversa de bytes utilizando la S-box inversa */
    private static void invSubBytes(int[] state) {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = invSBox[state[i]];
        }
    }

    /** Desplazamiento de filas (ShiftRows) */
    private static void shiftRows(int[] state) {
        int[] temp = new int[BLOCK_SIZE];

        // Copiar la primera fila (fila 0)
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 0] = state[i * 4 + 0];
        }

        // Desplazar fila 1
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 1] = state[((i + 1) % 4) * 4 + 1];
        }

        // Desplazar fila 2
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 2] = state[((i + 2) % 4) * 4 + 2];
        }

        // Desplazar fila 3
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 3] = state[((i + 3) % 4) * 4 + 3];
        }

        // Copiar temporal al estado
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Desplazamiento inverso de filas (InvShiftRows) */
    private static void invShiftRows(int[] state) {
        int[] temp = new int[BLOCK_SIZE];

        // Copiar la primera fila (fila 0)
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 0] = state[i * 4 + 0];
        }

        // Desplazar fila 1 inversamente
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 1] = state[((i + 3) % 4) * 4 + 1];
        }

        // Desplazar fila 2 inversamente
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 2] = state[((i + 2) % 4) * 4 + 2];
        }

        // Desplazar fila 3 inversamente
        for (int i = 0; i < 4; i++) {
            temp[i * 4 + 3] = state[((i + 1) % 4) * 4 + 3];
        }

        // Copiar temporal al estado
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Mezcla de columnas (MixColumns) */
    private static void mixColumns(int[] state, int polynomial) {
        for (int c = 0; c < 4; c++) { // c es la columna
            int[] column = new int[4];
            for (int r = 0; r < 4; r++) {
                column[r] = state[c * 4 + r];
            }

            state[c * 4 + 0] = gfMultiply(column[0], 2, polynomial) ^ gfMultiply(column[1], 3, polynomial) ^ column[2]
                    ^ column[3];
            state[c * 4 + 1] = column[0] ^ gfMultiply(column[1], 2, polynomial) ^ gfMultiply(column[2], 3, polynomial)
                    ^ column[3];
            state[c * 4 + 2] = column[0] ^ column[1] ^ gfMultiply(column[2], 2, polynomial)
                    ^ gfMultiply(column[3], 3, polynomial);
            state[c * 4 + 3] = gfMultiply(column[0], 3, polynomial) ^ column[1] ^ column[2]
                    ^ gfMultiply(column[3], 2, polynomial);
        }
    }

    /** Mezcla inversa de columnas (InvMixColumns) */
    private static void invMixColumns(int[] state, int polynomial) {
        for (int c = 0; c < 4; c++) {
            int[] column = new int[4];
            for (int r = 0; r < 4; r++) {
                column[r] = state[c * 4 + r];
            }

            state[c * 4 + 0] = gfMultiply(column[0], 14, polynomial) ^ gfMultiply(column[1], 11, polynomial)
                    ^ gfMultiply(column[2], 13, polynomial) ^ gfMultiply(column[3], 9, polynomial);
            state[c * 4 + 1] = gfMultiply(column[0], 9, polynomial) ^ gfMultiply(column[1], 14, polynomial)
                    ^ gfMultiply(column[2], 11, polynomial) ^ gfMultiply(column[3], 13, polynomial);
            state[c * 4 + 2] = gfMultiply(column[0], 13, polynomial) ^ gfMultiply(column[1], 9, polynomial)
                    ^ gfMultiply(column[2], 14, polynomial) ^ gfMultiply(column[3], 11, polynomial);
            state[c * 4 + 3] = gfMultiply(column[0], 11, polynomial) ^ gfMultiply(column[1], 13, polynomial)
                    ^ gfMultiply(column[2], 9, polynomial) ^ gfMultiply(column[3], 14, polynomial);
        }
    }

}
