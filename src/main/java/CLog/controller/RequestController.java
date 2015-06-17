package CLog.controller;

import CLog.entities.Request;
import CLog.services.RequestService;
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
    private RequestService requestService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Request> getAll() {
        return requestService.getAll();
    }

    // get request by id (Path)
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Request getOnebyPath(@PathVariable String id) {
        return requestService.getOne(id);
    }


    @RequestMapping(method = RequestMethod.POST)
    public Request add(@RequestBody Request newrequest) {
        return requestService.newRequest(newrequest); // Wandelt den neuen (unvollständigen) Request um und schickt den vollständigen zurück.
    }
    @RequestMapping(value="/{id}", method = RequestMethod.POST)
    public Request save(@RequestBody Request request) {
        return requestService.newRequest(request); // Speichert den Request
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) {
        requestService.deleteRequest(id);
    }


}
