package CLog.entities;

import org.springframework.data.annotation.Id;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.BitSet;
import java.util.Date;

/**
 * Created by Lucas on 11.05.2015.
 */
public class KeyPaar {

    @Id
    private String id;

    private Date timestamp;
    private BitSet pub;
    private BitSet priv;
    private BitSet decayVector;

    public KeyPaar(Date timestamp, BitSet pub, BitSet priv, BitSet decayVector) {
        this.timestamp = timestamp;
        this.pub = pub;
        this.priv = priv;
        this.decayVector = decayVector;
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

    public BitSet getPub() {
        return pub;
    }

    public void setPub(BitSet pub) {
        this.pub = pub;
    }

    public BitSet getPriv() {
        return priv;
    }

    public void setPriv(BitSet priv) {
        this.priv = priv;
    }

    public BitSet getDecayVector() {
        return decayVector;
    }

    public void setDecayVector(BitSet decayVector) {
        this.decayVector = decayVector;
    }
}
