package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ChaCha20 {
    public String encrypt(byte[] pText, SecretKey key, byte[] nonce, int counter) throws Exception {
        Cipher cipher = Cipher.getInstance("ChaCha20");
        ChaCha20ParameterSpec param=new ChaCha20ParameterSpec(nonce, counter);
        cipher.init(Cipher.ENCRYPT_MODE,key,param);
        byte[] encryptedText = cipher.doFinal(pText);
        return Base64.getEncoder().encodeToString(encryptedText);
    }
    public String decrypt(String cText, SecretKey key, byte[] nonce, int counter ) throws Exception {

        Cipher cipher=Cipher.getInstance("ChaCha20");
        ChaCha20ParameterSpec param=new ChaCha20ParameterSpec(nonce, counter);
        cipher.init(Cipher.DECRYPT_MODE,key,param);

        byte[] decodedText = Base64.getDecoder().decode(cText);
        byte[] decryptedText = cipher.doFinal(decodedText);

        return  new String(decryptedText, StandardCharsets.UTF_8);
    }
}
