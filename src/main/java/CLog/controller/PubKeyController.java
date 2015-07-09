package CLog.controller;

import CLog.entities.EventDTO;
import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.services.DecryptService;
import CLog.services.KeyRecoveryService;
import CLog.services.KeyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucas on 11.05.2015.
 */
@RestController
@RequestMapping("/api/key")
public class PubKeyController {

    private static Log log = LogFactory.getLog(PubKeyController.class);

    @Autowired
    private KeyService keyService;

    @Autowired
    private DecryptService decryptService;

    @RequestMapping(method = RequestMethod.GET)
    public PubKeyDTO getKey() {
        KeyPaar keyPaar = keyService.generateKeyPaar();
        log.info("New KeyPaar generated. Pub Key: "+keyPaar.getPub());
        return KeyService.getPubKey(keyPaar);
    }

    @RequestMapping(value="/all", method = RequestMethod.GET)
    public ArrayList<EventDTO> getAllKeys() {
        return keyService.getAllKeyEvents();
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