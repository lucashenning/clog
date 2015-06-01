package CLog.services;

import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Date;
import java.util.Random;


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
        PublicKey pub = null;
        PrivateKey priv = null;
        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstanceStrong();
            keyPairGenerator.initialize(2048, random);

            keyPair = keyPairGenerator.generateKeyPair();
            pub = keyPair.getPublic();
            priv = keyPair.getPrivate();

        } catch (NoSuchAlgorithmException e) {
            log.warn("Hier ist aber mächtig was schief gegangen", e);
        }


        // KeyPaar für die DB erzeugen und speichern
        Random randomlong = new Random();
        long id = randomlong.nextLong();
        Date timestamp = new Date();
        KeyPaar keyPaar = new KeyPaar(timestamp, pub, priv);
        // KeyPair in MongoDB abspeichern:
        return keyPaarRepository.save(keyPaar);
    }

    public static PubKeyDTO getPubKey(KeyPaar keyPaar) {
        PubKeyDTO pubKeyDTO = new PubKeyDTO();
        pubKeyDTO.setId(keyPaar.getId());
        pubKeyDTO.setPubKey(convertToString(keyPaar.getPub().getEncoded()));
        return pubKeyDTO;
    }

    private static String convertToString(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += String.valueOf(b);
        }
        return result;
    }

}
