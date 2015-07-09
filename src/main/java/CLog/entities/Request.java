package CLog.entities;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by l.henning on 16.06.2015.
 */
public class Request {

    @Id
    private String id;

    private User initiator;
    private int status;
    // 1 = requested, but not approved
    // 2 = approved
    // 3 = calculating keys
    // 4 = copying encrypted data and decrypting
    // 5 = done
    private ArrayList<Approval> approvals = new ArrayList<>();
    private Date timestamp;
    private Date startDate;
    private Date endDate;
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public ArrayList<Approval> getApprovals() {
        return approvals;
    }

    public void setApprovals(ArrayList<Approval> approvals) {
        this.approvals = approvals;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean hasApprover (User user) {
        for (Approval app : this.approvals) {
            if (app.getApprover().getUsername().equals(user.getUsername())) {
                return true;
            }
        }
        return false;
    }

}
