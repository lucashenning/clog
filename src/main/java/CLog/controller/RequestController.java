package CLog.controller;

import CLog.entities.Request;
import CLog.repositories.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by l.henning on 16.06.2015.
 */
@RestController
@RequestMapping("/api/request")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Request> getAll() {
        return requestRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void add(@RequestBody Request newrequest) {
        requestRepository.save(newrequest);
    }

}
