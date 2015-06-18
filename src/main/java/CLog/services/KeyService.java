package CLog.services;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Lucas on 11.05.2015.
 */
@Service
public class KeyService {

    private static Log log = LogFactory.getLog(KeyService.class);

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    public KeyPaar generateKeyPaar() {
        KeyPair keyPair = null;
        String pub = null;
        String priv = null;
        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstanceStrong();
            keyPairGenerator.initialize(2048, random);

            keyPair = keyPairGenerator.generateKeyPair();
            pub = convertToString(keyPair.getPublic().getEncoded());
            priv = convertToString(keyPair.getPrivate().getEncoded());

        } catch (NoSuchAlgorithmException e) {
            log.warn("Hier ist aber mächtig was schief gegangen", e);
        }


        // KeyPaar für die DB erzeugen und speichern
        Date timestamp = new Date();
        KeyPaar keyPaar = new KeyPaar(timestamp, pub, priv);
        // KeyPair in MongoDB abspeichern:
        return keyPaarRepository.save(keyPaar);
    }

    public static PubKeyDTO getPubKey(KeyPaar keyPaar) {
        PubKeyDTO pubKeyDTO = new PubKeyDTO();
        pubKeyDTO.setId(keyPaar.getId());
        pubKeyDTO.setPubKey(keyPaar.getPub());
        return pubKeyDTO;
    }

    public ArrayList<EventDTO> getAllKeyEvents() {
        List<KeyPaar> keyPaars = keyPaarRepository.findAll();
        ArrayList<EventDTO> events = new ArrayList<EventDTO>();
        for (KeyPaar keyPaar : keyPaars) {
            EventDTO current = new EventDTO();
            current.setId(keyPaar.getId());
            current.setPubKey(keyPaar.getPub());
            current.setTimestamp(keyPaar.getTimestamp());
            events.add(current);
        }
        return events;
    }

    public long count() {
        return keyPaarRepository.count();
    }

    private static String convertToString(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += String.valueOf(b);
        }
        return result;
    }

}
