package be.uantwerpen.distributedfileapp.Controller;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Service.NamingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
public class NamingController {

    NamingService namingService = new NamingService();
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
            /*@RequestParam(value = "nodeName") String nodeName,*/
    ) throws IOException {

        DatagramPacket pkt = new DatagramPacket(new byte[255], 255);

        try(MulticastSocket udpSocket = new MulticastSocket(4994)){

            udpSocket.joinGroup(InetAddress.getByName(/*request.getRemoteAddr()*/ "224.0.0.1"));

            udpSocket.receive(pkt);

            udpSocket.leaveGroup(InetAddress.getByName(/*request.getRemoteAddr()*/ "224.0.0.1"));
        }

        log.info("[Multicast] Received: "
                + new String(pkt.getData())
                + " Offset: " + pkt.getOffset()
                + " Length: " + pkt.getLength()
        );

        String numOfNodes = namingService.addNode(new String(pkt.getData())) + '@';

        pkt = new DatagramPacket(
                numOfNodes.getBytes(StandardCharsets.UTF_8), numOfNodes.length(),
                new InetSocketAddress(InetAddress.getLocalHost(), 1337)
        );

        try (DatagramSocket resp = new DatagramSocket()){resp.send(pkt);}

        saveMap2LocalDisk(nodeMap);

        // ====== Init/ing new Node =======
        //nodeService.initNewNode(nodeName, request.getRemoteAddr());
        return "Node Added!";
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
