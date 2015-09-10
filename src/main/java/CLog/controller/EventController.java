package CLog.controller;

import CLog.entities.EventDTO;
import CLog.services.DecryptService;
import CLog.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lucas on 10.09.2015.
 */
@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private KeyService keyService;

    @Autowired
    private DecryptService decryptService;

    @RequestMapping(value="/all", method = RequestMethod.GET)
    public List<EventDTO> getAllEvents() {
        return keyService.getAllKeyEvents(PaginationUtil.generatePageRequest(0, 0)).getContent();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventDTO>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                                 @RequestParam(value = "per_page", required = false) Integer limit)
            throws URISyntaxException {
        Page<EventDTO> page = keyService.getAllKeyEvents(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value="/count", method = RequestMethod.GET)
    public long count() {
        return keyService.count();
    }

    @RequestMapping(value="/decrypt/{id}", method = RequestMethod.GET)
    public Map<String,Object> decrypt(@PathVariable String id) {
        String result = decryptService.decrypt(id);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type","success");
        map.put("msg",result);
        return map;
    }

    @RequestMapping(value="/decay/{id}", method = RequestMethod.GET)
    public Map<String,Object> decay(@PathVariable String id) {
        return keyService.decayKey(id);
    }

    @RequestMapping(value="/recover/{id}", method = RequestMethod.GET)
    public Map<String,Object> recover(@PathVariable String id) {
        return keyService.recoverOneKey(id);
    }

    @RequestMapping(value="/countvariants/{id}", method = RequestMethod.GET)
    public int countVariants(@PathVariable String id) {
        return keyService.countVariants(keyService.findOne(id));
    }

}
