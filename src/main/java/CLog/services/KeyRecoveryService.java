package CLog.services;

import CLog.entities.KeyPaar;
import CLog.repositories.KeyPaarRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by l.henning on 09.07.2015.
 */
@Service
public class KeyRecoveryService {

    private static Log log = LogFactory.getLog(KeyRecoveryService.class);

    @Autowired
    private KeyService keyService;

    @Autowired
    private KeyPaarRepository keyPaarRepository;

    private AtomicInteger progress = new AtomicInteger(0);
    public AtomicInteger getProgress() {
        return progress;
    }
    public void setProgress(AtomicInteger progress) {
        this.progress = progress;
    }

    private AtomicInteger max = new AtomicInteger(0);
    public AtomicInteger getMax() {
        return max;
    }
    public void setMax(AtomicInteger max) {
        this.max = max;
    }

    private List<String> busyList = new ArrayList<>();
    public boolean isBusy() {
        return !busyList.isEmpty();
    }

    @Async
    public Future<KeyPaar> recoverKey(KeyPaar keyPaar) {
        log.info("Starting Key Recovery for Key "+keyPaar.getId()+" with cardinality "+keyPaar.getPriv().cardinality()+" and vector "+keyPaar.getDecayVector());
        busyList.add(keyPaar.getId());
        BitSet result = bruteForceKey(keyPaar.getPriv(), keyPaar.getDecayVector(), keyPaar.getValidator(), progress);
        keyPaar.setPriv(result);
        keyPaar.getDecayVector().clear(0,keyPaar.getDecayVector().size());
        keyPaarRepository.save(keyPaar);
        busyList.remove(keyPaar.getId());
        return new AsyncResult<>(keyPaar);
    }

    public BitSet bruteForceKey(BitSet key, BitSet decayVector, byte[] validator, AtomicInteger i) {
        BitSet currentDecayVector = (BitSet) decayVector.clone();
        BitSet currentKey = (BitSet) key.clone();
        if ( currentDecayVector.isEmpty() ) { // wenn der decayVector nur Nullen enthält, ist der key vollständig generiert und muss validiert werden.
            i.incrementAndGet();
            if ( keyService.validateRSAprivKey(currentKey.toByteArray(), validator) ) {
                log.debug("BruteForceKey: Found the right key:"+currentKey);
                return currentKey;
            } else {
                log.debug("BruteForceKey: Round "+i+" Wrong key tested: "+currentKey);
                return null;
            }
        } else {
            int firstSetBit = currentDecayVector.nextSetBit(0); // Erste 1 im Vektor suchen
            currentDecayVector.clear(firstSetBit); // Die gefundene Position im DecayVector auf 0 setzen
            BitSet firstResult = bruteForceKey(currentKey, currentDecayVector, validator, i); // Aufruf der Funktion mit der gefundenen Stelle im Key = 0
            if (firstResult == null) { // Wenn beim ersten Aufruf kein Key gefunden wurde, dann zweiter Aufruf
                currentKey.set(firstSetBit);
                return bruteForceKey(currentKey, currentDecayVector, validator, i); // Aufruf der Funktion mit der gefundenen Stelle im Key = 1
            } else {
                return firstResult;
            }
        }
    }


}
