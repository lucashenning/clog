package CLog.services;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.*;


/**
 * Created by Lucas on 11.05.2015.
 */
@Service
public class KeyService {

    private static Log log = LogFactory.getLog(KeyService.class);

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    public KeyPaar generateKeyPaar() {
        BitSet pub = null;
        BitSet priv = null;
        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            // Don't need this because keyPairGenerator.initialize calls it by default:
            // SecureRandom random = SecureRandom.getInstanceStrong();
            keyPairGenerator.initialize(2048);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            pub = BitSet.valueOf(keyPair.getPublic().getEncoded());
            log.info("KeyPaar erfolgreich erzeugt. Länge des Bitstreams: "+pub.size());
            priv = BitSet.valueOf(keyPair.getPrivate().getEncoded());

        } catch (NoSuchAlgorithmException e) {
            log.warn("Fehler bei der Schlüssel Erzeugung: ", e);
        }

        // KeyPaar für die DB erzeugen und speichern
        Date timestamp = new Date();
        BitSet decayVector = new BitSet(pub.size()); // Create decayVector which size is equivalent to the size of the public key
        log.info("Decay Vector created: "+decayVector);
        KeyPaar keyPaar = new KeyPaar(timestamp, pub, priv, decayVector);
        // KeyPair in MongoDB abspeichern:
        return keyPaarRepository.save(keyPaar);
    }

    public static PubKeyDTO getPubKey(KeyPaar keyPaar) {
        PubKeyDTO pubKeyDTO = new PubKeyDTO();
        pubKeyDTO.setId(keyPaar.getId());
        pubKeyDTO.setPubKey(bitSetToBase64(keyPaar.getPub()));
        log.warn("PubKeyDTO wird ausgegeben. PubKey: "+pubKeyDTO.getPubKey());
        return pubKeyDTO;
    }

    public ArrayList<EventDTO> getAllKeyEvents() {
        List<KeyPaar> keyPaars = keyPaarRepository.findAll();
        ArrayList<EventDTO> events = new ArrayList<EventDTO>();
        for (KeyPaar keyPaar : keyPaars) {
            EventDTO current = new EventDTO();
            current.setId(keyPaar.getId());
            current.setPubKey(bitSetToBase64(keyPaar.getPub()));
            current.setTimestamp(keyPaar.getTimestamp());
            current.setNumberOfDecayedBits(keyPaar.getDecayVector().cardinality());
            events.add(current);
        }
        return events;
    }

    public long count() {
        return keyPaarRepository.count();
    }

    public byte[] getPrivKeyInBytes(String id) {
        KeyPaar keyPaar = keyPaarRepository.findOne(id);
        return keyPaar.getPriv().toByteArray();
    }

    public Map decayKey(String keyPaarId) {
        HashMap map = new HashMap<>();
        KeyPaar keyPaar = keyPaarRepository.findOne(keyPaarId);
        // First 288 Bits and last 32 of 2368 bit RSA key bitstream are meta data.
        // Therefore we have to choose a random int inside the signature part of the RSA key.
        int rnd = randInt(288, 288 + 2048);

        // Clear Random-Bit in privateKey
        BitSet priv = keyPaar.getPriv();
        priv.clear(rnd);
        keyPaar.setPriv(priv);

        // Set Random-Bit in decayVector
        BitSet decayVector = keyPaar.getDecayVector();
        decayVector.set(rnd);
        keyPaar.setDecayVector(decayVector);

        keyPaarRepository.save(keyPaar);
        map.put("type", "success");
        map.put("msg", "rnd: "+rnd+" new decayVector: "+keyPaar.getDecayVector());
        log.info("Key decayed! rnd: "+rnd+" New decayVector: "+keyPaar.getDecayVector());
        return map;
    }

    public static String bitSetToBase64(BitSet bitSet) {
        return Base64.getEncoder().encodeToString(bitSet.toByteArray());
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

}
