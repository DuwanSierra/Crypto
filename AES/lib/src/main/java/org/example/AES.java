package org.example;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AES {

    // Nuevo polinomio irreducible: m'(x) = x^8 + x^5 + x^3 + x + 1 (0x1A5)
    private static final List<String> codes = List.of("20231078001", "20231078002", "20231078003");

    // Tamaño de bloque y clave en bits
    private static final int BLOCK_SIZE = 16; // 128 bits
    private static final int KEY_SIZE = 16;   // 128 bits
    private static final int NUM_ROUNDS = 10;

    // S-box y S-box inversa (se generarán dinámicamente)
    private static int[] sBox = new int[256];
    private static int[] invSBox = new int[256];

    // Matrices para MixColumns y su inversa
    private static final int[] MIX_COLUMNS_MATRIX = {
        0x02, 0x03, 0x01, 0x01,
        0x01, 0x02, 0x03, 0x01,
        0x01, 0x01, 0x02, 0x03,
        0x03, 0x01, 0x01, 0x02
    };

    private static final int[] INV_MIX_COLUMNS_MATRIX = {
        0x0E, 0x0B, 0x0D, 0x09,
        0x09, 0x0E, 0x0B, 0x0D,
        0x0D, 0x09, 0x0E, 0x0B,
        0x0B, 0x0D, 0x09, 0x0E
    };

    // Claves de ronda
    private static int[] roundKeys;

    public static void main(String[] args) {
        // Texto de ejemplo
        String text = "Hola que haces";
        // Clave de ejemplo
        String keyText = "ClaveSuperSecret";

        Integer polynomial = 0x11B;//Polynomial.getIrreduciblePolynomial(Polynomial.calculateAESModulo(codes));

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

    /** Función para ajustar el texto a 16 bytes */
    public static byte[] prepareData(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8); // Usar UTF-8 para consistencia
        byte[] result = new byte[16]; // AES usa bloques de 16 bytes (128 bits)

        if (data.length >= 16) {
            // Si el texto es mayor o igual a 16 bytes, truncar
            System.arraycopy(data, 0, result, 0, 16);
        } else {
            // Si el texto es menor a 16 bytes, copiar y rellenar con ceros
            System.arraycopy(data, 0, result, 0, data.length);
            // Los bytes restantes ya están inicializados a 0
        }
        return result;
    }

    /** Función para cifrar el bloque de texto */
    public static byte[] encrypt(byte[] input, Integer polynomial) {
        int[] state = new int[BLOCK_SIZE];
        // Convertir input a matriz de estado
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = input[i] & 0xFF;
        }

        // Añadir clave de ronda inicial
        addRoundKey(state, 0);

        // Rondas principales
        for (int round = 1; round < NUM_ROUNDS; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state, polynomial);
            addRoundKey(state, round);
        }

        // Ronda final
        subBytes(state);
        shiftRows(state);
        addRoundKey(state, NUM_ROUNDS);

        // Convertir matriz de estado a byte array
        byte[] output = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            output[i] = (byte) state[i];
        }
        return output;
    }

    /** Función para descifrar el bloque de texto */
    public static byte[] decrypt(byte[] input, Integer polynomial) {
        int[] state = new int[BLOCK_SIZE];
        // Convertir input a matriz de estado
        for (int i = 0; i < BLOCK_SIZE; i++) {
            state[i] = input[i] & 0xFF;
        }

        // Añadir clave de ronda final
        addRoundKey(state, NUM_ROUNDS);

        // Rondas principales
        for (int round = NUM_ROUNDS - 1; round > 0; round--) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, round);
            invMixColumns(state, polynomial);
        }

        // Ronda inicial
        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, 0);

        // Convertir matriz de estado a byte array
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
            int q = r0 / r1;
            int temp = r0 % r1;
            r0 = r1;
            r1 = temp;

            temp = t0 ^ multiply(q, t1, polynomial);
            t0 = t1;
            t1 = temp;
        }

        if (r0 > 1) {
            return 0;
        }
        return t0;
    }

    /** Expansión de clave para generar claves de ronda */
    private static void keyExpansion(byte[] key, Integer polynomial) {
        int Nk = KEY_SIZE / 4; // Número de palabras en la clave (4 para AES-128)
        int Nr = NUM_ROUNDS;   // Número de rondas (10 para AES-128)
        int Nb = 4;            // Número de palabras en un bloque (siempre 4 para AES)

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
    private static int rcon(int n, Integer polynomial) {
        int c = 1;
        if (n == 0) {
            return 0;
        }
        while (n > 1) {
            c = multiply(c, 0x02, polynomial);
            n--;
        }
        return c;
    }

    /** Añadir clave de ronda al estado */
    private static void addRoundKey(int[] state, int round) {
        for (int i = 0; i < 4; i++) {
            int roundKeyWord = roundKeys[round * 4 + i];
            state[4 * i] ^= (roundKeyWord >>> 24) & 0xFF;
            state[4 * i + 1] ^= (roundKeyWord >>> 16) & 0xFF;
            state[4 * i + 2] ^= (roundKeyWord >>> 8) & 0xFF;
            state[4 * i + 3] ^= roundKeyWord & 0xFF;
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

        // Fila 0 (sin desplazamiento)
        temp[0] = state[0];
        temp[4] = state[4];
        temp[8] = state[8];
        temp[12] = state[12];

        // Fila 1 (desplazamiento de 1 byte)
        temp[1] = state[5];
        temp[5] = state[9];
        temp[9] = state[13];
        temp[13] = state[1];

        // Fila 2 (desplazamiento de 2 bytes)
        temp[2] = state[10];
        temp[6] = state[14];
        temp[10] = state[2];
        temp[14] = state[6];

        // Fila 3 (desplazamiento de 3 bytes)
        temp[3] = state[15];
        temp[7] = state[3];
        temp[11] = state[7];
        temp[15] = state[11];

        // Copiar temporal al estado
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Desplazamiento inverso de filas (InvShiftRows) */
    private static void invShiftRows(int[] state) {
        int[] temp = new int[BLOCK_SIZE];

        // Fila 0 (sin desplazamiento)
        temp[0] = state[0];
        temp[4] = state[4];
        temp[8] = state[8];
        temp[12] = state[12];

        // Fila 1 (desplazamiento de 1 byte hacia la derecha)
        temp[1] = state[13];
        temp[5] = state[1];
        temp[9] = state[5];
        temp[13] = state[9];

        // Fila 2 (desplazamiento de 2 bytes hacia la derecha)
        temp[2] = state[10];
        temp[6] = state[14];
        temp[10] = state[2];
        temp[14] = state[6];

        // Fila 3 (desplazamiento de 3 bytes hacia la derecha)
        temp[3] = state[7];
        temp[7] = state[11];
        temp[11] = state[15];
        temp[15] = state[3];

        // Copiar temporal al estado
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Mezcla de columnas (MixColumns) */
    private static void mixColumns(int[] state, Integer polynomial) {
        int[] temp = new int[BLOCK_SIZE];
        for (int col = 0; col < 4; col++) {
            int[] column = new int[4];
            for (int row = 0; row < 4; row++) {
                column[row] = state[row * 4 + col];
            }

            for (int row = 0; row < 4; row++) {
                temp[row * 4 + col] = multiply(column[0], MIX_COLUMNS_MATRIX[row * 4], polynomial)
                        ^ multiply(column[1], MIX_COLUMNS_MATRIX[row * 4 + 1], polynomial)
                        ^ multiply(column[2], MIX_COLUMNS_MATRIX[row * 4 + 2], polynomial)
                        ^ multiply(column[3], MIX_COLUMNS_MATRIX[row * 4 + 3], polynomial);
            }
        }
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Mezcla inversa de columnas (InvMixColumns) */
    private static void invMixColumns(int[] state, Integer polynomial) {
        int[] temp = new int[BLOCK_SIZE];
        for (int col = 0; col < 4; col++) {
            int[] column = new int[4];
            for (int row = 0; row < 4; row++) {
                column[row] = state[row * 4 + col];
            }

            for (int row = 0; row < 4; row++) {
                temp[row * 4 + col] = multiply(column[0], INV_MIX_COLUMNS_MATRIX[row * 4], polynomial)
                        ^ multiply(column[1], INV_MIX_COLUMNS_MATRIX[row * 4 + 1], polynomial)
                        ^ multiply(column[2], INV_MIX_COLUMNS_MATRIX[row * 4 + 2], polynomial)
                        ^ multiply(column[3], INV_MIX_COLUMNS_MATRIX[row * 4 + 3], polynomial);
            }
        }
        System.arraycopy(temp, 0, state, 0, BLOCK_SIZE);
    }

    /** Multiplicación en GF(2^8) utilizando el nuevo polinomio irreducible */
    private static int multiply(int a, int b, Integer polynomial) {
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
}
