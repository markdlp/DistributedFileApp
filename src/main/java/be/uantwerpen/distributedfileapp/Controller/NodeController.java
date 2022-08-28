package be.uantwerpen.distributedfileapp.Controller;

import be.uantwerpen.distributedfileapp.Model.Naming;
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
import java.net.UnknownHostException;

@Slf4j
@RestController
public class NodeController {

    NodeService nodeService = new NodeService();

    Node node;

    @RequestMapping(value = {"/discovery/cast-new-node"}, method = RequestMethod.POST)
    public String multicastNewNode(
            @RequestParam(value = "nodeName") String nodeName,
            HttpServletRequest request
    ) throws IOException {

        Integer newNumOfNodes = nodeService.sendMulticast2All(nodeName, request.getRemoteAddr());

        log.info("New number of nodes in topology " + newNumOfNodes);

        Node newNode = nodeService.initNewNode(nodeName, request.getRemoteAddr(), newNumOfNodes);

        return "New node created with ID: " + Naming.getHash(newNode.getName());
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

        String newNodeName = new String(pkt.getData()).split("@")[0];
        String newNodeAddr = new String(pkt.getData()).split("@")[1];

        node = nodeService.updateNodeParams(newNodeName, newNodeAddr, node);
        return "[Node Parameters updated]: nextID: " + node.getNextID()
                + " prevID: " + node.getPreviousID();
    }

    @RequestMapping(value = {"/shutdown"}, method = RequestMethod.DELETE)
    public String shutDown(
            //@RequestParam(value = "nodeId") Integer nodeId
    ) throws IOException {return nodeService.shutDown(node);}

    @RequestMapping(value = {"/checkIfAlive"}, method = RequestMethod.GET)
    public String isReachable(
            @RequestParam(value = "nodeName") String nodeName
    ) throws UnknownHostException {

        InetAddress inetAddress = InetAddress.getByName(
                Naming.getNodeMap().get((int)Naming.getHash(nodeName))
        );

        try {inetAddress.isReachable(50);}
        catch (IOException e) {return nodeService.failure((int)Naming.getHash(nodeName));}

        return "Host: [" + inetAddress.getHostName() + "] is reachable";
    }
}
