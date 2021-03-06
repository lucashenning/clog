package CLog.services;

import CLog.entities.Approval;
import CLog.entities.KeyPaar;
import CLog.entities.Request;
import CLog.entities.User;
import CLog.repositories.RequestRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by l.henning on 17.06.2015.
 */
@Service
public class RequestService {

    private static Log log = LogFactory.getLog(RequestService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private KeyService keyService;

    @Autowired
    private RequestExecutionService requestExecutionService;

    public Request newRequest(Request preRequest) {
        Request request = new Request();
        request.setTimestamp(preRequest.getTimestamp());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
        User user = userService.findUserByUsername(name);
        request.setInitiator(user);
        request.setStartDate(preRequest.getStartDate());
        request.setEndDate(preRequest.getEndDate());
        request.setTimestamp(new Date());
        request.setComment(preRequest.getComment());
        request.setStatus(1); // 1 = new request, not approved
        return requestRepository.save(request);
    }

    public Request editRequest(Request request) {
        request.setApprovals(new ArrayList<>());
        return requestRepository.save(request);
    }

    public Request getOne(String id) {
        return requestRepository.findOne(id);
    }

    public List<Request> getAll() {
        return requestRepository.findAll();
    }

    public void deleteRequest(String id) {
        Request c = requestRepository.findOne(id);
        requestRepository.delete(c);
    }

    public long count() {
        return requestRepository.count();
    }

    public Map<String, Object> approve(String id) throws ExecutionException, InterruptedException {
        Request request = requestRepository.findOne(id);
        Map<String, Object> map = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
        User user = userService.findUserByUsername(name);
        if (request.getStatus() == 1) {
            if (user == request.getInitiator()) {
                map.put("type", "danger");
                map.put("msg", "Request could not be approved by initiator. ");
                return map;
            } else if (request.hasApprover(user)) {
                map.put("type", "danger");
                map.put("msg", "Can not approve request. This request is already approved by " + user.getUsername());
                return map;
            } else {
                map.put("type", "success");
                map.put("msg", "Request approved!");
                Approval approval = new Approval();
                approval.setApprover(user);
                approval.setTimestamp(new Date());
                request.getApprovals().add(approval);
                if (request.getApprovals().size() == 2) {
                    request.setStatus(2); // Status = approved
                    map.put("msg", "Request approved! Beginning with key recovery and decryption as soon as possible...");
                    requestExecutionService.execute(request);
                } else {
                    requestRepository.save(request);
                }
                return map;
            }
        } else {
            map.put("type", "danger");
            map.put("msg", "Request is already approved or closed.");
            return map;
        }
    }

    public List<KeyPaar> getEventsOfRequest (Request request) {
        try {
            return keyService.findByTimestampBetween(request.getStartDate(), request.getEndDate());
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
    }

    public Map countEventsOfRequest (String id) {
        Request request = requestRepository.findOne(id);
        List <KeyPaar> list = getEventsOfRequest(request);
        Map<String, Object> map = new HashMap<>();
        map.put("count",list.size());
        map.put("variants",keyService.countVariants(list));
        return map;
    }

    public Map getProgress(String id) {
        Request request = requestRepository.findOne(id);
        Map<String, Object> map = new HashMap<>();
        try {
            if (request.getStatus() == 3) {
                map = keyService.getKeyRecoveryStatus();
                map.put("status",3);
            } else if (request.getStatus() == 4) {
                map = requestExecutionService.getRequestDecryptionStatus();
                map.put("status",4);
            } else if (request.getStatus() == 5) {
                map.put("status",5);
            }
        } catch (NullPointerException e) {
            map.put("msg", "Fehler");
        }
        return map;
    }
}
