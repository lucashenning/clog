package CLog.services;

import CLog.entities.DecryptedLogEntry;
import CLog.entities.KeyPaar;
import CLog.repositories.DecryptedLogRepository;
import CLog.repositories.LogRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by l.henning on 24.06.2015.
 * Schnittstelle der drei Datenbanken
 * Lokale MongoDB mit Keys, entfernte MongoDB mit Log-Einträgen und ElasticSearch-Instanz mit Ergebnissen.
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

    private AtomicInteger progress = new AtomicInteger(0);
    public AtomicInteger getProgress() {
        return progress;
    }
    public void setProgress(AtomicInteger progress) {
        this.progress = progress;
    }

    private AtomicInteger max = new AtomicInteger(0);
    public AtomicInteger getMax() {
        return max;
    }
    public void setMax(AtomicInteger max) {
        this.max = max;
    }

    public String decrypt(KeyPaar keyPaar) {
        progress.incrementAndGet();
        // Read from Remote MongoDB
        Map<String,Object> map = logRepository.findOne(keyPaar.getId());
        log.info("Object received from remote MongoDB: "+map);

        // Base64 Decode
        byte[] ciphertext = Base64.getDecoder().decode((String) map.get("ciphertext"));
        byte[] iv = Base64.getDecoder().decode((String) map.get("iv"));
        log.info("Encrypted Session Key is (Base64): "+(String) map.get("encrypted_session_key"));
        byte[] encrypted_session_key = Base64.getDecoder().decode((String) map.get("encrypted_session_key"));

        try {
            // RSA Decrypt to get Session Key
            byte[] privKeyInBytes = keyService.findOne(keyPaar.getId()).getPriv().toByteArray();
            byte[] session_key = keyService.decryptRSA(encrypted_session_key, privKeyInBytes);
            log.info("RSA Result in Base 64: "+Base64.getEncoder().encodeToString(session_key));

            // AES Decrypt Event
            byte [] result = keyService.decryptAES(ciphertext, iv, session_key);
            String plaintext = new String(result);
            log.info("AES Result: "+plaintext);

            // Write to Elastic
            DecryptedLogEntry decryptedLogEntry = new DecryptedLogEntry();
            decryptedLogEntry.setId(keyPaar.getId());
            decryptedLogEntry.setPlaintext(plaintext);
            decryptedLogEntry.setTimestamp(keyPaar.getTimestamp());
            decryptedLogRepository.save(decryptedLogEntry);

            return "Decrypted Message: "+plaintext;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return "RSA Padding Exception: Seems like you try to decrypt directly with an incomplete key...";
        }
        return "Fehler";
    }



}
