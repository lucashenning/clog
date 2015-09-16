package CLog.repositories;

import CLog.entities.DecryptedLogEntry;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Created by l.henning on 03.07.2015.
 */

public interface DecryptedLogRepository extends ElasticsearchRepository<DecryptedLogEntry, String> { }
