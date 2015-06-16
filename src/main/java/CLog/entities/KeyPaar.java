package CLog.entities;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by Lucas on 11.05.2015.
 */
public class KeyPaar {

    @Id
    private String id;

    private Date timestamp;
    private String pub;
    private String priv;

    public KeyPaar(Date timestamp, String pub, String priv) {
        this.timestamp = timestamp;
        this.pub = pub;
        this.priv = priv;
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

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    public String getPriv() {
        return priv;
    }

    public void setPriv(String priv) {
        this.priv = priv;
    }
}
