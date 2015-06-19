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

    private String initiator;
    private int status;
    // 1 = requested, but not approved
    // 2 = approved
    // 3 = copying encrypted data
    // 4 = calculating keys
    // 5 = decrypting
    // 6 = done
    private ArrayList<String> approvers;
    private Date timestamp;
    private Date startDate;
    private Date endDate;

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

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public ArrayList<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(ArrayList<String> approvers) {
        this.approvers = approvers;
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


}
