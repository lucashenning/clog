package CLog.repositories;

import com.mongodb.*;
import org.springframework.context.annotation.Bean;
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
    private String ip = "172.31.1.50";
    private String db = "ls_db";
    private String collection = "logstash";

    public LogRepository() {
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
