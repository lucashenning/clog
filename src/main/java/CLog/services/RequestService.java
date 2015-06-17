package CLog.services;

import CLog.entities.Request;
import CLog.repositories.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        Request request = new Request();
        request.setTimestamp(preRequest.getTimestamp());
        request.setInitiator(preRequest.getInitiator());
        request.setStartDate(preRequest.getStartDate());
        request.setEndDate(preRequest.getEndDate());
        request.setTimestamp(new Date());
        requestRepository.save(request);
        return request;
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

}
