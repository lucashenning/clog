package CLog.repositories;

import CLog.services.ConfigurationService;
import com.mongodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by l.henning on 24.06.2015.
 */

@Repository
public class LogRepository {

    private MongoClient mongoClient;

    private String ip;
    private String collection;
    private String db;

    @Autowired
    public LogRepository(ConfigurationService config) {
        this.ip = config.logMongoDbIp;
        this.collection = config.getLogMongoDbCollection();
        this.db = config.getLogMongoDbDb();
        try {
            mongoClient = new MongoClient(this.ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Map<String,Object> findOne(String id) {
        DB db = mongoClient.getDB(this.db);
        DBCollection collection = db.getCollection(this.collection);
        BasicDBObject query = new BasicDBObject("_id", id);
        DBObject result = collection.findOne(query);
        Map<String,Object> map = result.toMap();
        return map;
    }

}
