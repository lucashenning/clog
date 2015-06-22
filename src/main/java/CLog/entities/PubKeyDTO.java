package CLog.entities;

import java.security.PublicKey;

/**
 * Created by Lucas on 01.06.2015.
 */
public class PubKeyDTO {
    private String id;
    private String pubKey;

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
