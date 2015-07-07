package CLog.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by l.henning on 07.07.2015.
 */
@Service
public class ConfigurationService {


    @Value("${elasticsearch.clustername}")
    public static String elasticSearchClusterName = "clog";

    @Value("${logmongodb.ip}")
    public static String logMongoDbIp = "172.31.1.50";

    @Value("${logmongodb.collection}")
    public static String logMongoDbCollection = "logstash";

    @Value("${logmongodb.db}")
    public static String logMongoDbDb = "ls_db";

    @Value("${rsa.validation.string}")
    public static String validationString = "YetAnotherRSAValidationString";





}
