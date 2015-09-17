package CLog.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by l.henning on 07.07.2015.
 */
@Service
public class ConfigurationService {

    @Value("${logserver.mongodb.ip}")
    public String logServerMongoDbIp;

    @Value("${logserver.mongodb.collection}")
    public String logServerMongoDbCollection;

    @Value("${logserver.mongodb.db}")
    public String logServerMongoDbDb;

    @Value("${rsa.validation.string}")
    public String validationString;

}
