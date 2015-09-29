package CLog.services;

import CLog.entities.KeyPaar;
import CLog.entities.Request;
import CLog.repositories.RequestRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by l.henning on 10.07.2015.
 */
@Service
public class RequestExecutionService {

    private static Log log = LogFactory.getLog(RequestExecutionService.class);

    @Autowired
    private RequestService requestService;

    @Autowired
    private KeyService keyService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DecryptService decryptService;

    @Async
    public void execute(Request request) throws ExecutionException, InterruptedException {
        // Set Status to "Calculating Keys"
        request.setStatus(3);
        requestRepository.save(request);
        List<KeyPaar> list = requestService.getEventsOfRequest(request);
        ListenableFuture<List<KeyPaar>> listenableFutureKeyPaarList = keyService.recoverKeyList(list);
        list = listenableFutureKeyPaarList.get(); // Wait for Key Recovery

        log.info("keyRecovery Service ist fertig!");
        // Set Status to "copying and decrypting"
        request.setStatus(4);
        requestRepository.save(request);

        // Copy Data and decrypt events
        decryptService.setProgress(new AtomicInteger(0));
        decryptService.setMax(new AtomicInteger(list.size()));
        for (KeyPaar k : list) {
            decryptService.decrypt(k);
        }

        // Key Decryption fertig
        log.info("Key Decryption ist fertig!");
        request.setStatus(5);
        requestRepository.save(request);
    }

    public Map getRequestDecryptionStatus() {
        Map<String, Object> map= new HashMap<>();
        map.put("progress",decryptService.getProgress());
        map.put("max",decryptService.getMax());
        return map;
    }

}
