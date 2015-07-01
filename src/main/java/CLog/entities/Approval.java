package CLog.entities;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by Lucas on 20.06.2015.
 */
public class Approval {

    @Id
    private String id;

    private User approver;
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
