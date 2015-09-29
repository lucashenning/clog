package CLog.services;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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
import java.util.concurrent.ExecutionException;
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
            validator = encryptRSA(configurationService.validationString.getBytes(), pub.toByteArray());
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
        log.info("Validator created: " + byteToBinary(validator));
        KeyPaar keyPaar = new KeyPaar(timestamp, pub, priv, decayVector, validator);
        // KeyPair in MongoDB abspeichern:
        return keyPaar;
    }

    public KeyPaar saveKeyPaar(KeyPaar keyPaar) {
        return keyPaarRepository.save(keyPaar);
    }

    public static PubKeyDTO getPubKeyDTO(KeyPaar keyPaar) {
        PubKeyDTO pubKeyDTO = new PubKeyDTO();
        pubKeyDTO.setId(keyPaar.getId());
        pubKeyDTO.setPubKey(bitSetToBase64(keyPaar.getPub()));
        log.warn("PubKeyDTO wird ausgegeben. PubKey: "+pubKeyDTO.getPubKey());
        return pubKeyDTO;
    }

    public Page<EventDTO> getAllKeyEvents(Pageable pageable) {
        Page<KeyPaar> keyPaars = keyPaarRepository.findAll(pageable);
        List<EventDTO> events = new ArrayList<>();
        for (KeyPaar keyPaar : keyPaars) {
            EventDTO current = new EventDTO();
            current.setId(keyPaar.getId());
            current.setPubKey(bitSetToBase64(keyPaar.getPub()));
            current.setTimestamp(keyPaar.getTimestamp());
            current.setNumberOfDecayedBits(keyPaar.getDecayVector().cardinality());
            events.add(current);
        }
        return new PageImpl<>(events);
    }

    public long count() {
        return keyPaarRepository.count();
    }

    public KeyPaar findOne(String id) {
        return keyPaarRepository.findOne(id);
    }

    public List<KeyPaar> findByTimestampBetween(Date startDate, Date endDate) {
        List<KeyPaar> list = keyPaarRepository.findByTimestampBetween(startDate, endDate);
        //log.info("Looked for KeyPairs between " + startDate + " and " + endDate + " ... found: " + list);
        return list;
    }

    public Map recoverSingleKey(String id) {
        KeyPaar keyPaar = keyPaarRepository.findOne(id);
        recoverSingleKey(keyPaar);
        HashMap map = new HashMap<>();
        map.put("type", "success");
        map.put("msg", "Key Recovery started...");
        return map;
    }

    public ListenableFuture<KeyPaar> recoverSingleKey(KeyPaar keyPaar) {
        ListenableFuture<KeyPaar> keyPaarListenableFuture = keyRecoveryService.recoverKeyPaar(keyPaar);
        keyPaarListenableFuture.addCallback(new ListenableFutureCallback<KeyPaar>() {
            @Override
            public void onSuccess(KeyPaar newKeyPaar){
                keyPaarRepository.save(keyPaar);
            }
            @Override
            public void onFailure(Throwable t){
                log.error("Error executing callback.", t);
            }
        });
        return keyPaarListenableFuture;
    }

    public ListenableFuture<List<KeyPaar>> recoverKeyList(List<KeyPaar> oldKeyPaarList) throws ExecutionException, InterruptedException {
        keyRecoveryService.setProgress(new AtomicInteger(0));
        keyRecoveryService.setMax(new AtomicInteger(countVariants(oldKeyPaarList)));
        List<KeyPaar> newKeyPaarList = new ArrayList<>();
        for (KeyPaar keyPaar : oldKeyPaarList) {
            newKeyPaarList.add(recoverSingleKey(keyPaar).get());
        }
        return new AsyncResult<>(newKeyPaarList);
    }

    public int countVariants(List<KeyPaar> list) {
        int result = 0;
        for (KeyPaar k : list) {
            result = result + countVariants(k);
        }
        return result;
    }

    public int countVariants(KeyPaar k) {
        return countVariants(k.getDecayVector().cardinality());
    }

    public int countVariants(int cardinality) {
        return (int) Math.pow( 2, cardinality);
    }

    public Map getKeyRecoveryStatus() {
        Map<String, Object> map = new HashMap<>();
        if (keyRecoveryService.isBusy()) {
            map.put("progress",keyRecoveryService.getProgress());
            map.put("max",keyRecoveryService.getMax());
        } else {
            map.put("msg", "Recovery Service is not busy...");
        }
        return map;
    }

    public Map decayKey(String keyPaarId) {
        HashMap map = new HashMap<>();
        KeyPaar keyPaar = keyPaarRepository.findOne(keyPaarId);

        keyPaar = decayKey(keyPaar);

        keyPaarRepository.save(keyPaar);
        map.put("type", "success");
        map.put("msg", "New decayVector: "+keyPaar.getDecayVector());
        log.info("Key decayed! New decayVector: "+keyPaar.getDecayVector());
        return map;
    }

    public KeyPaar decayKey(KeyPaar keyPaar) {
        // Gesamt Bit-Länge des privaten Schlüssels: 9737
        // Random int inside the RSA private exponent. (between 2424 and 4471)
        // BitSet indiziert Bits von hinten.
        // Das letzte Bit des privaten Exponenten ist 4471 (von hinten 9737 - 4471 = 5266).
        // Das erste Bit des privaten Exponenten ist 2424 (von hinten 9737 - 2424 = 7313).
        int rnd = randInt(5266, 7313); // TODO: Exakte Länge des RSA-Keys bestimmen und nur relevante Bits löschen.
        //int rnd = randInt(0, keyPaar.getPriv().size() - 1);

        log.debug("Card:"+keyPaar.getPriv().cardinality()+"Key before decay: "+byteToBinary(keyPaar.getPriv().toByteArray()));

        // Clear Random-Bit in privateKey
        keyPaar.getPriv().clear(rnd);

        log.debug("Card:"+keyPaar.getPriv().cardinality()+" Key after decay: "+byteToBinary(keyPaar.getPriv().toByteArray()));

        // Set Random-Bit in decayVector
        keyPaar.getDecayVector().set(rnd);
        return keyPaar;
    }

    public boolean validateRSAprivKey(byte[] privKey, byte[] validator) {
        try {
            byte[] byteResult = decryptRSA(validator, privKey);
            String stringResult = new String(byteResult);
            if ( stringResult.equals( configurationService.validationString ) ) {
                log.info("Found the right String result (valid key): "+stringResult);
                return true;
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
            //e.printStackTrace();
            log.debug("ValidateRSAKey: RSA Decryption Error --> Not a valid Key");
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

    @Scheduled( cron = "0 0 1 * * ?" ) // "0 * * * * *" --> every minute / "0 0 1 * * ?" --> Every Night at 1 am
    public void decayContinuously() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1); // Aelter als 1 Monat --> cal.add(Calendar.MONTH, -1);
        Date endDate = cal.getTime();
        List<KeyPaar> list = keyPaarRepository.findByTimestampBefore(endDate);
        int i = 0;
        for (KeyPaar kp : list) {
            decayKey(kp.getId());
            i++;
        }
        log.info("Decay cronjob executed. " + i + " keys decayed.");
    }


}
