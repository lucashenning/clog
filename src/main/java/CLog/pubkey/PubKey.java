package CLog.pubkey;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by Lucas on 11.05.2015.
 */
public class PubKey {

    @Id
    private String id;
    private Date timestamp;
    private String key;


    public PubKey(String id, String key, Date timestamp) {
        this.id = id;
        this.key = key;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
