package CLog.controller;

import CLog.entities.KeyPaar;
import CLog.entities.PubKeyDTO;
import CLog.services.KeyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Lucas on 11.05.2015.
 */
@RestController
@RequestMapping("/key")
public class PubKeyController {

    private static Log log = LogFactory.getLog(PubKeyController.class);

    @Autowired
    private KeyService keyService;

    @RequestMapping(method = RequestMethod.GET)
    public PubKeyDTO getKey() {
        KeyPaar keyPaar = keyService.generateKeyPaar();
        log.info("New KeyPaar generated.");
        return KeyService.getPubKey(keyPaar);
    }

}