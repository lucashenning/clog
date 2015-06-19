package CLog.services;

import CLog.entities.Request;
import CLog.entities.User;
import CLog.repositories.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by l.henning on 17.06.2015.
 */
@Service
public class RequestService {


    @Autowired
    private RequestRepository requestRepository;

    public Request newRequest(Request preRequest) {
        if ( preRequest.getId() != null && requestRepository.exists(preRequest.getId()) ) {
            return requestRepository.save(preRequest);
        } else {
            Request request = new Request();
            request.setTimestamp(preRequest.getTimestamp());
            //String user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails(); // Aktuell eingeloggten User holen
            request.setInitiator(SecurityContextHolder.getContext().getAuthentication().getName());
            request.setStartDate(preRequest.getStartDate());
            request.setEndDate(preRequest.getEndDate());
            request.setTimestamp(new Date());
            return requestRepository.save(request);
        }
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

}
