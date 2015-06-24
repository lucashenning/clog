package CLog.services;

import CLog.entities.KeyPaar;
import CLog.repositories.KeyPaarRepository;
import CLog.repositories.LogRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Created by l.henning on 24.06.2015.
 */
@Service
public class DecryptService {

    private static Log log = LogFactory.getLog(DecryptService.class);

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    public String decrypt(String id) {
        Map<String,Object> map = logRepository.findOne(id);
        log.warn("Object received from remote MongoDB: "+map);

        // Base64 Decode
        byte[] ciphertext = Base64.getDecoder().decode((String) map.get("ciphertext"));
        byte[] iv = Base64.getDecoder().decode((String) map.get("iv"));
        log.warn("Encrypted Session Key is (Base64): "+(String) map.get("encrypted_session_key"));
        byte[] encrypted_session_key = Base64.getDecoder().decode((String) map.get("encrypted_session_key"));


        try {
            // RSA Decrypt to get Session Key
            KeyPaar keyPaar = keyPaarRepository.findOne(id);
            byte[] privKeyInBytes = Base64.getDecoder().decode(keyPaar.getPriv());
            Cipher rsa = Cipher.getInstance("RSA");
            PrivateKey privateKey =  KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyInBytes));
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] session_key = rsa.doFinal(encrypted_session_key);
            log.warn("RSA Result in Base 64: "+Base64.getEncoder().encodeToString(session_key));

            // AES Decrypt Event
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(session_key, "AES"), new IvParameterSpec(iv, 0, aes.getBlockSize()));
            String plaintext = new String(aes.doFinal(ciphertext));
            log.warn("AES Result: "+plaintext);
            return plaintext;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return "Fehler";
    }


}
