package CLog.controller;

import CLog.entities.DecryptedLogEntry;
import CLog.services.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by l.henning on 16.09.2015.
 */
@RestController
@RequestMapping("/api/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DecryptedLogEntry>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                                 @RequestParam(value = "per_page", required = false) Integer limit)
            throws URISyntaxException {
        Page<DecryptedLogEntry> page = resultService.getAllResults(PaginationUtil.generatePageRequest(offset, limit, new Sort(Sort.Direction.DESC, "timestamp")));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/results", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
