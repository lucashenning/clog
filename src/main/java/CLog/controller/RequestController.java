package CLog.controller;

import CLog.entities.Request;
import CLog.services.KeyService;
import CLog.services.RequestService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by l.henning on 16.06.2015.
 */
@RestController
@RequestMapping("/api/request")
public class RequestController {

    private static Log log = LogFactory.getLog(RequestController.class);

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
        return requestService.editRequest(request); // Speichert den Request
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) {
        requestService.deleteRequest(id);
    }

    @RequestMapping(value="/count", method = RequestMethod.GET)
    public long count() {
        return requestService.count();
    }

    @RequestMapping(value="/{id}/countevents", method = RequestMethod.GET)
    public Map countEvents(@PathVariable String id) {
        return requestService.countEventsOfRequest(id);
    }

    @RequestMapping(value="/{id}/getprogress", method = RequestMethod.GET)
    public Map getProgress(@PathVariable String id) {
        return requestService.getProgress(id);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.POST, params = "approve=true")
    public Map<String,Object> approve(@PathVariable String id) {
        return requestService.approve(id);
    }


}
