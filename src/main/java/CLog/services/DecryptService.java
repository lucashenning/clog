package CLog.services;

import CLog.entities.KeyPaar;
import CLog.repositories.DecryptedLogRepository;
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
    private KeyService keyService;

    @Autowired
    private DecryptedLogRepository decryptedLogRepository;

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
            byte[] privKeyInBytes = keyService.getPrivKeyInBytes(id);
            byte[] session_key = keyService.decryptRSA(encrypted_session_key, privKeyInBytes);
            log.warn("RSA Result in Base 64: "+Base64.getEncoder().encodeToString(session_key));

            // AES Decrypt Event
            byte [] result = keyService.decryptAES(ciphertext, iv, session_key);
            String plaintext = new String(result);
            log.warn("AES Result: "+plaintext);

            // Write to Elastic
            log.warn(decryptedLogRepository.add(plaintext).isCreated());

            return plaintext;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return "RSA Padding Exception: Seems like you try to decrypt directly with an incomplete key...";
        }
        return "Fehler";
    }


}
