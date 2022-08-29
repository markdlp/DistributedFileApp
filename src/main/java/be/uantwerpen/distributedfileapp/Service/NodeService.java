package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Model.Node;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class NodeService {

    Map<Integer, String> nodeMap = Naming.getNodeMap();

    NamingService namingService = new NamingService();

    public Node initNewNode(
            String nodeName, String addr, Integer numOfNodes
    ) throws IOException {

        // Adding local files to node's file List
        List<File> files = Arrays.stream(
                (Objects.requireNonNull(new File("files").listFiles()))
        ).toList();

        if (numOfNodes == 1) return new Node(
                nodeName, addr, files,
                (int) Naming.getHash(nodeName),
                (int) Naming.getHash(nodeName)
        ); else {

            int previousID = 32768, nextID = 32768;

            byte[] message = new byte[255];

            DatagramPacket pkt = new DatagramPacket(
                    message, message.length
            );

            for(int i = 0; i < numOfNodes - 1; i++) try (
                    DatagramSocket rcvSckt = new DatagramSocket(1337)
            ) {
                rcvSckt.receive(pkt);

                String fromNode = new String(pkt.getData());

                int fromNodeId = Integer.parseInt(fromNode.split("@")[0]);
                int fromNodePrevOrNextId = Integer.parseInt(fromNode.split("@")[1]);

                if (fromNodePrevOrNextId == Naming.getHash(nodeName)){
                    if(fromNodeId < Naming.getHash(nodeName)
                            && Math.abs(Naming.getHash(nodeName) - fromNodeId) < previousID
                    ) previousID = fromNodeId;
                    if(fromNodeId < Naming.getHash(nodeName)
                            && Math.abs(Naming.getHash(nodeName) - fromNodeId) < nextID
                    ) nextID = fromNodeId;
                }
            }

            return new Node(nodeName, addr, files, previousID, nextID);
        }
    }

    public Integer sendMulticast2All(
            String nodeName, String addr
    ) throws RuntimeException, IOException {

        byte[] message = (
                nodeName + '@' + addr + '@'
        ).getBytes(StandardCharsets.UTF_8);

        DatagramPacket pkt = new DatagramPacket(
                message, message.length, InetAddress.getByName("224.0.0.1"), 4994
        );
        try ( MulticastSocket udpSocket = new MulticastSocket()) {udpSocket.send(pkt);}

        try ( DatagramSocket rcvSckt = new DatagramSocket(1337)) {rcvSckt.receive(pkt);}

        return Integer.parseInt(new String(pkt.getData()).split("@")[0]);
    }

    public Node updateNodeParams(String newNode, String addr, Node currentNode) throws IOException {

        Integer newNodeHash = (int) Naming.getHash(newNode);
        Integer currentID = (int) Naming.getHash(currentNode.getName());


        if((currentID < newNodeHash && currentNode.getNextID() > newNodeHash)
                || currentID.equals(currentNode.getNextID())
        ){
            currentNode.setNextID(newNodeHash);

            String message = String.valueOf(currentID) + '@' + currentNode.getNextID() + '@';

            DatagramPacket pkt = new DatagramPacket(
                    message.getBytes(StandardCharsets.UTF_8), message.length(),
                    new InetSocketAddress(InetAddress.getByName(addr), 1337)
            );

            log.info("Sending parameters to new node");

            try (DatagramSocket resp = new DatagramSocket()){resp.send(pkt);}
        }

        if((currentNode.getPreviousID() < newNodeHash && currentID > newNodeHash)
                || currentID.equals(currentNode.getPreviousID())
        ){
            currentNode.setPreviousID(newNodeHash);

            String message = String.valueOf(currentID) + '@' + currentNode.getPreviousID() + '@';

            DatagramPacket pkt = new DatagramPacket(
                    message.getBytes(StandardCharsets.UTF_8), message.length(),
                    new InetSocketAddress(InetAddress.getByName(addr), 1337)
            );

            log.info("Sending parameters to new node");

            try (DatagramSocket resp = new DatagramSocket()){resp.send(pkt);}
        }

        return currentNode;
    }

    public String shutDown(Node node) throws IOException {

        // Sending the id of the next node to the previous node
        byte[] message = (String.valueOf(node.getNextID()) + '@').getBytes(StandardCharsets.UTF_8);

        DatagramPacket pkt = new DatagramPacket(
                message, message.length,
                InetAddress.getByName(nodeMap.get(node.getPreviousID())), 4994
        );

        try (DatagramSocket rspSock = new DatagramSocket()){ rspSock.send(pkt); }

        // Send the id of the previous node to the next node
        message = (String.valueOf(node.getPreviousID()) + '@').getBytes(StandardCharsets.UTF_8);

        pkt = new DatagramPacket(
                message, message.length,
                InetAddress.getByName(nodeMap.get(node.getNextID())), 4994
        );

        try (DatagramSocket rspSock = new DatagramSocket()){ rspSock.send(pkt); }

        nodeMap.remove((int)Naming.getHash(node.getName()));

        return "Shutdown Proc Complete";
    }

    public String failure(Integer ID)throws IOException{

        // Sending the id of the next node to the previous node
        byte[] message =
                (String.valueOf(namingService.getNextId(ID)) + '@').getBytes(StandardCharsets.UTF_8);

        DatagramPacket pkt = new DatagramPacket(
                message, message.length,
                InetAddress.getByName(nodeMap.get(namingService.getPrevId(ID))), 4994
        );

        try (DatagramSocket rspSock = new DatagramSocket()){ rspSock.send(pkt); }

        // Send the id of the previous node to the next node
        message =
                (String.valueOf(namingService.getPrevId(ID)) + '@').getBytes(StandardCharsets.UTF_8);

        pkt = new DatagramPacket(
                message, message.length,
                InetAddress.getByName(nodeMap.get(namingService.getNextId(ID))), 4994
        );

        try (DatagramSocket rspSock = new DatagramSocket()){ rspSock.send(pkt); }

        return "Failure Proc Completed for host: "+ nodeMap.remove(ID);
    }
}
