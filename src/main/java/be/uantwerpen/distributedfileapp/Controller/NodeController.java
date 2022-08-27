package be.uantwerpen.distributedfileapp.Controller;

import be.uantwerpen.distributedfileapp.Service.NodeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class NodeController {

    NodeService nodeService = new NodeService();

    @RequestMapping(value = {"/discovery/cast-new-node"}, method = RequestMethod.POST)
    public String multicastNewNode(
            @RequestParam(value = "nodeName") String nodeName,
            HttpServletRequest request
    ) {
        try {
            return "New number of nodes in topology "
                    + nodeService.sendMulticast2All(nodeName, request.getRemoteAddr())
                    ;
        } catch (IOException e){
            return "Calling Failure Proc" ;
        }
    }

}
