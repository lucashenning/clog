package CLog.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by l.henning on 07.07.2015.
 */
@Service
public class ConfigurationService {


    @Value("${elasticsearch.clustername}")
    public String elasticSearchClusterName;

    @Value("${logmongodb.ip}")
    public String logMongoDbIp;

    @Value("${logmongodb.collection}")
    public String logMongoDbCollection;

    @Value("${logmongodb.db}")
    public String logMongoDbDb;

    @Value("${rsa.validation.string}")
    public String validationString;

    public String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    public void setElasticSearchClusterName(String elasticSearchClusterName) {
        this.elasticSearchClusterName = elasticSearchClusterName;
    }

    public String getLogMongoDbIp() {
        return logMongoDbIp;
    }

    public void setLogMongoDbIp(String logMongoDbIp) {
        this.logMongoDbIp = logMongoDbIp;
    }

    public String getLogMongoDbCollection() {
        return logMongoDbCollection;
    }

    public void setLogMongoDbCollection(String logMongoDbCollection) {
        this.logMongoDbCollection = logMongoDbCollection;
    }

    public String getLogMongoDbDb() {
        return logMongoDbDb;
    }

    public void setLogMongoDbDb(String logMongoDbDb) {
        this.logMongoDbDb = logMongoDbDb;
    }

    public String getValidationString() {
        return validationString;
    }

    public void setValidationString(String validationString) {
        this.validationString = validationString;
    }
}
