package CLog.Helper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * Created by Lucas on 12.05.2015.
 */
public class CryptHelper {

    Cipher cipher = null;
    KeyPair keyPair = null;
    int keyLength = 1024;

    public CryptHelper(KeyPair keyPair, int keyLength) {
        try {
            this.cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        this.keyPair = keyPair;
        this.keyLength = keyLength;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public String encrypt(String plaintext) throws Exception{
        this.cipher.init(Cipher.ENCRYPT_MODE, this.keyPair.getPublic());
        byte[] bytes = plaintext.getBytes("UTF-8");

        byte[] encrypted = blockCipher(bytes,Cipher.ENCRYPT_MODE);

        char[] encryptedTranspherable = Hex.encodeHex(encrypted);
        return new String(encryptedTranspherable);
    }

    public String decrypt(String encrypted) throws Exception{
        this.cipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
        byte[] bts = Hex.decodeHex(encrypted.toCharArray());

        byte[] decrypted = blockCipher(bts, Cipher.DECRYPT_MODE);

        return new String(decrypted,"UTF-8");
    }

    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)

        int length = (mode == Cipher.ENCRYPT_MODE) ? (keyLength / 8 ) - 11 : (keyLength / 8 );
        // OLD int length = (mode == Cipher.ENCRYPT_MODE)? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i=0; i< bytes.length; i++){

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)){
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn,scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i%length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn,scrambled);

        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix){
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i=0; i< prefix.length; i++){
            toReturn[i] = prefix[i];
        }
        for (int i=0; i< suffix.length; i++){
            toReturn[i+prefix.length] = suffix[i];
        }
        return toReturn;
    }

}
