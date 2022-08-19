package be.uantwerpen.distributedfileapp.Controller;


import be.uantwerpen.distributedfileapp.Model.Naming;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Controller
public class NamingController {

    Map<Integer, String> nodeMap = Naming.getNodeMap();

    @RequestMapping(value = {"/file-owner"}, method = RequestMethod.GET)
    public String getFileOwner(@RequestParam(value = "fileName") String fileName){

        short hashedName = Naming.getHash(fileName);

        log.info("File hash is: " + hashedName);

        return Short.toString(hashedName);
    }

    @RequestMapping(value = {"/add-node"}, method = RequestMethod.POST)
    public String addNode2Topology(
            @RequestParam(value = "nodeName") String nodeName,
            HttpServletRequest request
    ){
        log.info("Adding node: " + nodeName
                + " with ID: " + Naming.getHash(nodeName) + " ...if absent!"
        );
        nodeMap.putIfAbsent(
                (int) Naming.getHash(nodeName),
                request.getRemoteAddr()
        );

        return "Node Added!";
    }

    @RequestMapping(value = {"/del-node"}, method = RequestMethod.DELETE)
    public String removeNode(@RequestParam(value = "nodeName") String nodeName){

        log.warn("Removing node: " + nodeName
                + " with ID: " + Naming.getHash(nodeName) + " ...if present!"
        );
        nodeMap.remove((int)Naming.getHash(nodeName));

        return "Node Removed!";
    }

    private void saveMap2LocalDisk(Map<Integer, String> map){

    }
}
