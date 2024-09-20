package org.example;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String DICTIONARY = "EAOLSNDRUITCPMYQ";
    private static final int DICTIONARY_LENGTH = DICTIONARY.length(); // 32 caracteres
    private byte[] privateKey;

    public Main(String key) {
        validateKey(key);
        this.privateKey = keySetup(key);
    }

    // Convierte un número en su representación binaria de 5 bits.
    public static String to5BitBinary(int num) {
        return String.format("%5s", Integer.toBinaryString(num)).replace(' ', '0');
    }

    // Valida que la clave esté en el rango adecuado y dentro del diccionario.
    public static void validateKey(String key) {
        if (key.length() < 4 || key.length() > 16) {
            throw new IllegalArgumentException("La clave debe tener entre 4 y 16 caracteres.");
        }

        for (char c : key.toCharArray()) {
            if (DICTIONARY.indexOf(c) == -1) {
                throw new IllegalArgumentException("La clave contiene un carácter inválido: " + c);
            }
        }
    }

    // Convierte un texto a un array de índices numéricos dentro del diccionario.
    public static byte[] convert(String text) {
        byte[] codes = new byte[text.length()];

        for (int i = 0; i < text.length(); i++) {
            int index = DICTIONARY.indexOf(text.charAt(i));
            if (index == -1) {
                throw new IllegalArgumentException("El carácter " + text.charAt(i) + " no está en el diccionario");
            }
            codes[i] = (byte) index;
        }

        return codes;
    }

    // Key setup para inicializar el estado de RC4
    public static byte[] keySetup(String key) {
        byte[] K = new byte[DICTIONARY_LENGTH];
        for (int i = 0; i < DICTIONARY_LENGTH; i++) {
            K[i] = (byte) i;
        }

        int j = 0;
        byte[] keyBytes = convert(key);

        for (int i = 0; i < DICTIONARY_LENGTH; i++) {
            j = (j + K[i] + keyBytes[i % keyBytes.length]) % DICTIONARY_LENGTH;
            byte temp = K[i];
            K[i] = K[j];
            K[j] = temp;
        }

        return K;
    }

    // Generador de flujo de bytes
    public static int byteStreamGenerator(byte[] K) {
        int i = 0, j = 0;
        i = (i + 1) % DICTIONARY_LENGTH;
        j = (j + K[i]) % DICTIONARY_LENGTH;

        byte temp = K[i];
        K[i] = K[j];
        K[j] = temp;

        return (K[(K[i] + K[j]) % DICTIONARY_LENGTH]);
    }

    // Método para cifrar el mensaje
    public String encrypt(String input) {
        StringBuilder outputText = new StringBuilder();
        byte[] KCopy = Arrays.copyOf(privateKey, privateKey.length); // Copia de la clave privada

        for (int i = 0; i < input.length(); i++) {
            int charIndex = DICTIONARY.indexOf(input.charAt(i));
            if (charIndex == -1) {
                throw new IllegalArgumentException("Carácter inválido en el mensaje: " + input.charAt(i));
            }

            int keyStreamByte = byteStreamGenerator(KCopy);
            int encryptedChar = (charIndex ^ keyStreamByte) % DICTIONARY_LENGTH;

            outputText.append(DICTIONARY.charAt(encryptedChar));
        }

        return outputText.toString();
    }

    // Método para descifrar el mensaje
    public String decrypt(String input) {
        StringBuilder outputText = new StringBuilder();
        byte[] KCopy = Arrays.copyOf(privateKey, privateKey.length); // Copia de la clave privada

        for (int i = 0; i < input.length(); i++) {
            int charIndex = DICTIONARY.indexOf(input.charAt(i));
            if (charIndex == -1) {
                throw new IllegalArgumentException("Carácter inválido en el mensaje cifrado: " + input.charAt(i));
            }

            int decryptedChar = (charIndex ^ byteStreamGenerator(KCopy)) % DICTIONARY_LENGTH;
            outputText.append(DICTIONARY.charAt(decryptedChar));
        }

        return outputText.toString();
    }

    public static void main(String[] args) {

        String palabraEncriptada = "IUADCAY";
        GenerateKey generateKey = new GenerateKey("ACDEILMNOPQRSTUY", 4);

        Map<String, String> posiblesSoluciones = new HashMap<>();

        generateKey.generateCombinations();

        generateKey.getCombinaciones().forEach(key -> {
            Main rc4 = new Main(key);
            String decrypted = rc4.decrypt(palabraEncriptada);
            posiblesSoluciones.put(key, decrypted);
        });

       System.out.println("Cantidad de posibles soluciones: " + posiblesSoluciones.size());
        System.out.println(posiblesSoluciones.toString());

        String key = "OLSN";
        String message = "SONIDOS";

        Main rc4 = new Main(key);

        String encrypted = rc4.encrypt(message);
        String decrypted = rc4.decrypt(encrypted);

        System.out.println("Mensaje cifrado: " + encrypted);
        System.out.println("Mensaje descifrado: " + decrypted);

        String key2 = "PEDRO";
        Main rc42 = new Main(key2);

        String encrypted2 = rc42.encrypt(message);
        String decrypted2 = rc42.decrypt(encrypted2);
        String decrypted3 = rc4.decrypt(encrypted2);

        System.out.println("Texto descifrado con clave propia: " + decrypted2);
        System.out.println("Texto descifrado con clave original: " + decrypted);
        System.out.println("Texto descifrado con clave original pero cambiando al decrypt de la clave 2: " + decrypted3);
    }

}