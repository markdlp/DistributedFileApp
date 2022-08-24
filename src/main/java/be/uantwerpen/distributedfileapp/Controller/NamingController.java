package be.uantwerpen.distributedfileapp.Controller;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Service.NamingService;
import be.uantwerpen.distributedfileapp.Service.NodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class NamingController {

    NamingService namingService = new NamingService();

    NodeService nodeService = new NodeService();
    Map<Integer, String> nodeMap = Naming.getNodeMap();

    @RequestMapping(value = {"/file-owner"}, method = RequestMethod.GET)
    public String getFileOwner(@RequestParam(value = "fileName") String fileName){

        short hashedName = Naming.getHash(fileName);
        log.info("File hash is: " + hashedName);

        Integer fileOwner = namingService.getFileOwner((int)hashedName);

        return "IP of the file Owner: " + nodeMap.get(fileOwner);
    }

    @RequestMapping(value = {"/add-node"}, method = RequestMethod.POST)
    public String addNode2Topology(
            @RequestParam(value = "nodeName") String nodeName,
            HttpServletRequest request
    ) throws IOException {
        log.info("Adding node: " + nodeName
                + " with ID: " + Naming.getHash(nodeName) + " ...if absent!"
        );
        nodeMap.putIfAbsent(
                (int) Naming.getHash(nodeName),
                request.getRemoteAddr()
        );

        saveMap2LocalDisk(nodeMap);

        // ====== Init/ing new Node =======
        String newNodeName = nodeService.initNewNode(nodeName, request.getRemoteAddr()).getName();
        return "Node w/ name: "+ newNodeName +" Added!";
    }

    @RequestMapping(value = {"/del-node"}, method = RequestMethod.DELETE)
    public String removeNode(
            @RequestParam(value = "nodeName") String nodeName
    ) throws IOException {

        log.warn("Removing node: " + nodeName
                + " with ID: " + Naming.getHash(nodeName) + " ...if present!"
        );
        nodeMap.remove((int)Naming.getHash(nodeName));

        saveMap2LocalDisk(nodeMap);

        return "Node Removed!";
    }

    private void saveMap2LocalDisk(Map<Integer, String> map) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(new File("nodeMap.json"), map);
    }
}
