package CLog.services;

import CLog.entities.Approval;
import CLog.entities.Request;
import CLog.entities.User;
import CLog.repositories.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by l.henning on 17.06.2015.
 */
@Service
public class RequestService {


    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserService userService;

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
        return requestRepository.save(request);
    }

    public Request editRequest(Request request) {
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

    public Map<String, Object> approve(String id) {
        Request request = requestRepository.findOne(id);
        Map<String, Object> map = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
        User user = userService.findUserByUsername(name);
        if (user != request.getInitiator()) {
            map.put("type", "danger");
            map.put("msg","Request could not be approved by initiator. ");
            return map;
        } else if ( !request.getApprovals().contains(user) ) {
            map.put("type", "danger");
            map.put("msg","Can not approve request. This request is already approved by "+user.getUsername());
            return map;
        } else {
            Approval approval = new Approval();
            approval.setApprover(user);
            approval.setTimestamp(new Date());
            request.getApprovals().add(approval);
            map.put("type", "success");
            map.put("msg","Request approved!");
            return map;
        }
    }

}
