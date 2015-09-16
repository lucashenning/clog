package CLog.services;

import CLog.entities.DecryptedLogEntry;
import CLog.repositories.DecryptedLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by l.henning on 16.09.2015.
 */
@Service
public class ResultService {

    @Autowired
    private DecryptedLogRepository decryptedLogRepository;

    public Page<DecryptedLogEntry> getAllResults(Pageable pageable) {
        return decryptedLogRepository.findAll(pageable);
    }

}
