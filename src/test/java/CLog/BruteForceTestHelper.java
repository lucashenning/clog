package CLog;

import CLog.entities.KeyPaar;
import CLog.services.KeyRecoveryService;
import CLog.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l.henning on 29.09.2015.
 */
@Service
public class BruteForceTestHelper {

    @Autowired
    private KeyService keyService;

    @Autowired
    private KeyRecoveryService keyRecoveryService;

    public List<KeyPaar> createTestKeyPaars(int schluesselanzahl, int decayedBits) {
        // Key Erzeugung
        List<KeyPaar> keyPaarList = new ArrayList<>();
        for (int i = 0; i < schluesselanzahl; i++) {
            KeyPaar keyPaar = keyService.generateKeyPaar();
            for (int j = 0; j < decayedBits; j++) {
                keyService.decayKey(keyPaar);
            }
            keyPaarList.add(keyPaar);
        }
        return keyPaarList;
    }

    public double testBruteForceTime(List<KeyPaar> keyPaarList) {
        long startTime = System.nanoTime();
        // Key Brute Force
        List<ListenableFuture<KeyPaar>> keyPaarFutureList = new ArrayList<>();
        for (KeyPaar keyPaar : keyPaarList) {
            keyPaarFutureList.add(keyRecoveryService.recoverKeyPaar(keyPaar));
        }

        int remainingFutues = keyPaarFutureList.size();
        while ( remainingFutues > 0 ) {
            remainingFutues = keyPaarFutureList.size();
            for (ListenableFuture<KeyPaar> keyPaarFuture : keyPaarFutureList) {
                if ( keyPaarFuture.isDone() ) {
                    remainingFutues--;
                }
            }
        }
        return (System.nanoTime() - startTime) / 1000000000.00;
    }

}
