package CLog.repositories;

import CLog.services.ConfigurationService;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;


/**
 * Created by l.henning on 03.07.2015.
 */

@Repository
public class DecryptedLogRepository {

    private Node node;
    private Client client;

    @Autowired
    public DecryptedLogRepository (ConfigurationService configurationService){
        this.node = nodeBuilder().client(true).clusterName(configurationService.elasticSearchClusterName).node();
        this.client = node.client();
    }

    public IndexResponse add(String data) {
        IndexResponse response = client.prepareIndex("log", "entry") // TODO: Clustername dynamisch aus Config-Datei auslesen
                .setSource(data)
                .execute()
                .actionGet();
        return response;
    }



}
