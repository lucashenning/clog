package CLog;

import org.springframework.data.annotation.Id;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * Created by Lucas on 11.05.2015.
 */
public class KeyPaar {

    @Id
    private long id;
    private Date timestamp;
    private PublicKey pub;
    private PrivateKey priv;

    public KeyPaar(long id, Date timestamp, PublicKey pub, PrivateKey priv) {
        this.id = id;
        this.timestamp = timestamp;
        this.pub = pub;
        this.priv = priv;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public PublicKey getPub() {
        return pub;
    }

    public void setPub(PublicKey pub) {
        this.pub = pub;
    }

    public PrivateKey getPriv() {
        return priv;
    }

    public void setPriv(PrivateKey priv) {
        this.priv = priv;
    }
}
