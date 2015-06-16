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
    private ArrayList<String> approvers;
    private Date timestamp;
    private Date startDate;
    private Date endDate;



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
