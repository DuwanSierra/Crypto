package org.example;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;

public class Main {
    public static void main(String[] args) {
        String input = "Java & ChaCha20 encryption example.";
        ChaCha20 chaCha20 = new ChaCha20();
        SecretKey key = null;
        try {
            key = SecretKeyManager.getKey();
            byte[] nonce = SecretKeyManager.getNonce();
            int counter = 1;

            ChaCha20 cipher = new ChaCha20();

            String cText=cipher.encrypt(input.getBytes(StandardCharsets.UTF_8),key,nonce,counter);
            System.out.println("Mensaje encriptado: " + new String(cText));
            String ptext=cipher.decrypt(cText,key,nonce,counter);
            System.out.println("Mensaje desencriptado: " + ptext);


        } catch (NoSuchAlgorithmException e) {
            System.out.println("Hubo un error al encriptar o descriptar el mensaje");
            throw new RuntimeException(e);
        } catch (Exception ex) {
            System.out.println("Hubo un error al encriptar o descriptar el mensaje");
            throw new RuntimeException(ex);
        }

    }
}