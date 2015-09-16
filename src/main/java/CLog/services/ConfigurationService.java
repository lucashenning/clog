package CLog.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by l.henning on 07.07.2015.
 */
@Service
public class ConfigurationService {

    @Value("${logmongodb.ip}")
    public String logMongoDbIp;

    @Value("${logmongodb.collection}")
    public String logMongoDbCollection;

    @Value("${logmongodb.db}")
    public String logMongoDbDb;

    @Value("${rsa.validation.string}")
    public String validationString;

}
