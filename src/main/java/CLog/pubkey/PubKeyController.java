package CLog.pubkey;

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

    @Autowired
    private PubKeyService pubKeyService;

    @RequestMapping(method = RequestMethod.GET)
    public PubKey getKey() {
        return pubKeyService.generateKey();
    }

}
