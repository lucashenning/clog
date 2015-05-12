package CLog.pubkey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import CLog.KeyPaar;
import CLog.KeyPaarRepository;

import java.security.*;
import java.util.Date;
import java.util.Random;


/**
 * Created by Lucas on 11.05.2015.
 */
@Service
public class PubKeyService {

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    public PubKey generateKey() {
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
            e.printStackTrace();
        }


        // KeyPaar für die DB erzeugen und speichern
        Random randomlong = new Random();
        long id = randomlong.nextLong();
        Date timestamp = new Date();
        KeyPaar keyPaar = new KeyPaar(id, timestamp, pub, priv);
        // KeyPair in MongoDB abspeichern:
        keyPaarRepository.save(keyPaar);

        // PubKey für die REST Schnittstelle erzeugen und zurückgeben
        PubKey pubKey = new PubKey(convertToString(priv.getEncoded()), convertToString(pub.getEncoded()), timestamp);
        return pubKey;
    }

    private static String convertToString(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += String.valueOf(b);
        }
        return result;
    }

}
