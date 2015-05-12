package CLog;

import org.junit.Assert;
import org.junit.Test;
import CLog.Helper.CryptHelper;

import java.security.*;

/**
 * Created by Lucas on 12.05.2015.
 */
public class EncryptionTest {

    @Test
    public void testEncryption() {
        try {
            int keyLenght = 2048;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstanceStrong();
            keyPairGenerator.initialize(keyLenght, random);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            CryptHelper cryptHelper = new CryptHelper(keyPair, keyLenght);

            String plainText = "Hello World";
            String cipherText = null;
            cipherText = cryptHelper.encrypt(plainText);

            String decryptedText = cryptHelper.decrypt(cipherText);

            Assert.assertEquals(plainText, decryptedText);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
