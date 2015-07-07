package CLog.services;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.river.RiverIndexName;
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
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


/**
 * Created by Lucas on 11.05.2015.
 */
@Service
public class KeyService {

    private static Log log = LogFactory.getLog(KeyService.class);

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    @Autowired
    private DecryptService decryptService;


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
        byte[] validator = {};
        try {
            validator = encryptRSA(ConfigurationService.validationString.getBytes(), pub.toByteArray());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        log.info("Validator created: "+validator);
        KeyPaar keyPaar = new KeyPaar(timestamp, pub, priv, decayVector, validator);
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

    public Map recoverKey(String keyPaarId) {
        HashMap map = new HashMap<>();
        KeyPaar keyPaar = keyPaarRepository.findOne(keyPaarId);
        BitSet result = bruteForceKey(keyPaar.getPriv(), keyPaar.getDecayVector(), keyPaar.getValidator());
        map.put("type", "success");
        map.put("msg", "Successfully recovered key: "+result);
        return map;
    }

    public BitSet bruteForceKey(BitSet key, BitSet decayVector, byte[] validator) {
        if ( decayVector.isEmpty() ) { // wenn der decayVector nur Nullen enthält, ist der key vollständig generiert und muss validiert werden.
            if ( validateRSAprivKey(key.toByteArray(), validator) ) {
                log.info("BruteForceKey: Found the right key:"+key);
                return key;
            } else {
                log.info("BruteForceKey: Wrong key tested: "+key);
                return null;
            }
        } else {
            int firstSetBit = decayVector.nextSetBit(0); // Erste 1 im Vektor suchen
            decayVector.clear(firstSetBit); // Die gefundene Position im DecayVector auf 0 setzen
            BitSet firstResult = bruteForceKey(key, decayVector, validator); // Aufruf der Funktion mit der gefundenen Stelle im Key = 0
            if (firstResult == null) { // Wenn beim ersten Aufruf kein Key gefunden wurde, dann zweiter Aufruf
                key.set(firstSetBit);
                return bruteForceKey(key, decayVector, validator); // Aufruf der Funktion mit der gefundenen Stelle im Key = 1
            } else {
                return firstResult;
            }
        }
    }

    public boolean validateRSAprivKey(byte[] privKey, byte[] validator) {
        try {
            byte[] byteResult = decryptRSA(validator, privKey);
            String stringResult = new String(byteResult);
            if (stringResult.equals(ConfigurationService.validationString)) {
                return true;
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] encryptRSA(byte[] plaintext, byte[] pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        Cipher rsa = Cipher.getInstance("RSA");
        PublicKey publicKey =  KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));
        rsa.init(Cipher.ENCRYPT_MODE, publicKey);
        return rsa.doFinal(plaintext);
    }

    public byte[] decryptRSA(byte[] ciphertext, byte[] privKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher rsa = Cipher.getInstance("RSA");
        PrivateKey privateKey =  KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKey));
        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        return rsa.doFinal(ciphertext);
    }

    public byte[] decryptAES(byte[] ciphertext, byte[] iv, byte[] aesKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv, 0, aes.getBlockSize()));
        return aes.doFinal(ciphertext);
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
