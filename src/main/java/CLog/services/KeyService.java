package CLog.services;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
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
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Lucas on 11.05.2015.
 */
@Service
public class KeyService {

    private static Log log = LogFactory.getLog(KeyService.class);

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private KeyRecoveryService keyRecoveryService;

    public KeyPaar generateKeyPaar() {
        BitSet pub = null;
        BitSet priv = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            pub = BitSet.valueOf(keyPair.getPublic().getEncoded());
            priv = BitSet.valueOf(keyPair.getPrivate().getEncoded());
            log.info("KeyPaar erfolgreich erzeugt. Länge des Private Key in Bits: "+priv.size());

            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            log.info("Whole Private Key as Binary: "+byteToBinary(rsaPrivateKey.getEncoded()));
            log.info("Private Exponent as Binary: "+byteToBinary(rsaPrivateKey.getPrivateExponent().toByteArray()));
            log.info("Private Exponent Substring: "+byteToBinary(rsaPrivateKey.getEncoded()).substring(2424,4472));

        } catch (NoSuchAlgorithmException e) {
            log.warn("Fehler bei der Schlüssel Erzeugung: ", e);
        }

        // KeyPaar für die DB erzeugen und speichern
        Date timestamp = new Date();
        BitSet decayVector = new BitSet(priv.size()); // Create decayVector which size is equivalent to the size of the private key
        log.info("Decay Vector created: "+decayVector);
        byte[] validator = {};
        try {
            validator = encryptRSA(configurationService.getValidationString().getBytes(), pub.toByteArray());
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
        log.info("Validator created: "+byteToBinary(validator));
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

    public KeyPaar findOne(String id) {
        return keyPaarRepository.findOne(id);
    }

    public List<KeyPaar> findByTimestampBetween(Date startDate, Date endDate) {
        List<KeyPaar> list = keyPaarRepository.findByTimestampBetween(startDate, endDate);
        log.info("Looked for KeyPairs between "+startDate+" and "+endDate+" ... found: "+list);
        return list;
    }

    public boolean recoverKeys(List<KeyPaar> list) {
        keyRecoveryService.setProgress(new AtomicInteger(0));
        keyRecoveryService.setMax(new AtomicInteger(countVariants(list)));
        for (KeyPaar k : list) {
            keyRecoveryService.recoverKey(k);
        }
        return true;
    }

    public int countVariants(List<KeyPaar> list) {
        int result = 0;
        for (KeyPaar k : list) {
            result = result + countVariants(k);
        }
        return result;
    }

    public int countVariants(KeyPaar k) {
        return (int) Math.pow( 2, k.getDecayVector().cardinality());
    }

    public Map getKeyRecoveryStatus() {
        Map<String, Object> map= new HashMap<>();
        if (keyRecoveryService.isBusy()) {
            map.put("progress",keyRecoveryService.getProgress());
            map.put("max",keyRecoveryService.getMax());
        } else {
            map.put("msg", "Recovery Service is not busy...");
        }
        return map;
    }

    public Map recoverOneKey(String id) {
        KeyPaar keyPaar = keyPaarRepository.findOne(id);
        List<KeyPaar> list = new ArrayList<KeyPaar>();
        list.add(keyPaar);
        HashMap map = new HashMap<>();
        if ( recoverKeys(list) ) {
            map.put("type", "success");
            map.put("msg", "Key Recovery started...");
        } else {
            map.put("type", "error");
            map.put("msg", "Error in KeyRecovery");
        }
        return map;
    }

    public Map recoverMultipleKeys(Date startDate, Date endDate) {
        List<KeyPaar> list = findByTimestampBetween(startDate, endDate);
        HashMap map = new HashMap<>();
        if ( recoverKeys(list) ) {
            map.put("type", "success");
            map.put("msg", "Key Recovery started for "+list.size()+" keys...");
        } else {
            map.put("type", "error");
            map.put("msg", "Error in KeyRecovery");
        }
        return map;
    }

    public Map decayKey(String keyPaarId) {
        HashMap map = new HashMap<>();
        KeyPaar keyPaar = keyPaarRepository.findOne(keyPaarId);
        // Random int inside the RSA private exponent. (between 2424 and 4471)
        //int rnd = randInt(2424, 4471); // TODO: Exakte Länge des RSA-Keys bestimmen und nur relevante Bits löschen.
        int rnd = randInt(0, keyPaar.getPriv().size() - 1);

        log.info("Card:"+keyPaar.getPriv().cardinality()+"Key before decay: "+byteToBinary(keyPaar.getPriv().toByteArray()));

        // Clear Random-Bit in privateKey
        keyPaar.getPriv().clear(rnd);

        log.info("Card:"+keyPaar.getPriv().cardinality()+" Key after decay: "+byteToBinary(keyPaar.getPriv().toByteArray()));

        // Set Random-Bit in decayVector
        keyPaar.getDecayVector().set(rnd);

        keyPaarRepository.save(keyPaar);
        map.put("type", "success");
        map.put("msg", "rnd: "+rnd+" new decayVector: "+keyPaar.getDecayVector());
        log.info("Key decayed! rnd: "+rnd+" New decayVector: "+keyPaar.getDecayVector());
        return map;
    }

    public boolean validateRSAprivKey(byte[] privKey, byte[] validator) {
        try {
            byte[] byteResult = decryptRSA(validator, privKey);
            String stringResult = new String(byteResult);
            if ( stringResult.equals( configurationService.getValidationString() ) ) {
                log.warn("Found the right String result (valid key): "+stringResult);
                return true;
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
            //e.printStackTrace();
            log.info("ValidateRSAKey: RSA Decryption Error --> Not a valid Key");
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

    public String byteToBinary( byte[] bytes )  {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

}
