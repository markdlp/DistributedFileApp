package be.uantwerpen.distributedfileapp.Controller;

import be.uantwerpen.distributedfileapp.Model.Node;
import be.uantwerpen.distributedfileapp.Service.NodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Slf4j
@RestController
public class NodeController {

    NodeService nodeService = new NodeService();

    Node node;

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

    @RequestMapping(value = {"/discovery/receive-new-node"}, method = RequestMethod.GET)
    public String recvNewNode( HttpServletRequest request) throws IOException {

        DatagramPacket pkt = new DatagramPacket(new byte[255], 255);

        try(MulticastSocket udpSocket = new MulticastSocket(4994)){

            udpSocket.joinGroup(InetAddress.getByName(/*request.getRemoteAddr()*/ "224.0.0.1"));

            udpSocket.receive(pkt);

            udpSocket.leaveGroup(InetAddress.getByName(/*request.getRemoteAddr()*/ "224.0.0.1"));
        }

        log.info("[Multicast Receiver in Nodes] Received: "
                + new String(pkt.getData())
                + " Offset: " + pkt.getOffset()
                + " Length: " + pkt.getLength()
        );

        node = nodeService.updateNodeParams(
                new String(pkt.getData()).split("@")[0], request.getRemoteAddr()
        );
        return "[Node Parameters updated]: nextID: " + node.getNextID()
                + " prevID: " + node.getPreviousID();
    }

    @RequestMapping(value = {"/shutdown"}, method = RequestMethod.DELETE)
    public String shutDown(
            //@RequestParam(value = "nodeId") Integer nodeId
    ) throws IOException {

        nodeService.shutDown(this.node);

        return "Shutdown proc complete";
    }
}
