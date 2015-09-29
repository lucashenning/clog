package CLog;

import CLog.services.KeyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.util.BitSet;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CLogApplication.class)
@WebAppConfiguration
public class SecretServerApplicationTests {

	@Autowired
	private KeyService keyService;

	@Autowired
	private BruteForceTestHelper bruteForceTestHelper;

	@Test
	public void testEncryptionDecryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
		BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
		BitSet priv = BitSet.valueOf(keyPair.getPrivate().getEncoded());
		System.out.println("Priv: "+keyService.byteToBinary(privateExponent.toByteArray()));
		BitSet newPriv = new BitSet(2048);
		newPriv.set(newPriv.size() - 1);
		BigInteger newD = new BigInteger(newPriv.toByteArray());

		RSAPrivateKeySpec newRSAPrivateSpec = new RSAPrivateKeySpec(rsaPrivateKey.getModulus(), newD);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPrivateKey newPrivateKey = (RSAPrivateKey) kf.generatePrivate(newRSAPrivateSpec);
		System.out.println("New Priv: "+keyService.byteToBinary(newPrivateKey.getPrivateExponent().toByteArray()));
	}

	@Test
	public void testBruteForce() throws IOException {
		writeFile("=== Brute Force Test Started ===\n");
		writeFile("AnzahlKeyPaars;AnzahlDecayedBits;Varianten;Seconds\n");
		int anzahlKeyPaars = 1;
		int anzahlDecayedBits = 1;
		while (anzahlDecayedBits < 20) {
			double seconds = bruteForceTestHelper.testBruteForceTime( bruteForceTestHelper.createTestKeyPaars(anzahlKeyPaars, anzahlDecayedBits) );
			writeFile(anzahlKeyPaars + ";" + anzahlDecayedBits + ";"+ keyService.countVariants(anzahlDecayedBits) +";"+ seconds+"\n");
			anzahlDecayedBits++;
		}
	}

	public void writeFile(String line) throws IOException {
		FileWriter writer = new FileWriter("bruteforcetest.txt", true);
		writer.write(line);
		writer.close();
	}

}
