package CLog.entities;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by l.henning on 16.06.2015.
 */
public class EventDTO {

    @Id
    private String id;

    private String pubKey;
    private Date timestamp;
    private int numberOfDecayedBits;

    public int getNumberOfDecayedBits() {
        return numberOfDecayedBits;
    }

    public void setNumberOfDecayedBits(int numberOfDecayedBits) {
        this.numberOfDecayedBits = numberOfDecayedBits;
    }

    public Date getTimestamp() {   return timestamp;   }

    public void setTimestamp(Date timestamp) {     this.timestamp = timestamp;   }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

}
