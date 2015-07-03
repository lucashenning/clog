package CLog.repositories;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;


/**
 * Created by l.henning on 03.07.2015.
 */
@Repository
public class DecryptedLogRepository {

    private Node node = nodeBuilder().client(true).clusterName("clog").node();
    private Client client = node.client();

    public IndexResponse add(String data) {
        IndexResponse response = client.prepareIndex("log", "entry") // TODO: Clustername dynamisch aus Config-Datei auslesen
                .setSource(data)
                .execute()
                .actionGet();
        return response;
    }

}
