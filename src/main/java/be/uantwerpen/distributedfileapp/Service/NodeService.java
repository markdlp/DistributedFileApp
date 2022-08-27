package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Model.Node;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeService {

    Map<Integer, String> nodeMap = Naming.getNodeMap();

    public Node initNewNode(String nodeName, String addr){

        // Adding local files to node's file List
        List<File> files = Arrays.stream(
                (Objects.requireNonNull(new File("files").listFiles()))
        ).toList();

        return new Node(
                nodeName, addr, files,
                (int) Naming.getHash(nodeName),
                (int) Naming.getHash(nodeName)
        );
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

    public Node updateNodeParams(String newNode, String addr) throws IOException {

        Integer newNodeHash = (int) Naming.getHash(newNode);

        // We have to use naming here
        // Map<Integer, String> map = Naming.getInstance().getMap();

        Node node = initNewNode(newNode, addr);

        int nodeID = Naming.getHash(node.getName());

        if(nodeID < newNodeHash && node.getNextID() > newNodeHash){
            node.setNextID(newNodeHash);

            byte[] message = (
                    String.valueOf(nodeID) + '@' + node.getNextID() + '@'
            ).getBytes(StandardCharsets.UTF_8);

            DatagramPacket pkt = new DatagramPacket(
                    message, message.length, InetAddress.getByName(addr), 4994
            );

            try (DatagramSocket rspSock = new DatagramSocket()){rspSock.send(pkt);}
        }

        if(node.getPreviousID() < newNodeHash && nodeID > newNodeHash){
            node.setPreviousID(newNodeHash);

            byte[] message = (
                    String.valueOf(nodeID) + '@' + node.getPreviousID() + '@'
            ).getBytes(StandardCharsets.UTF_8);

            DatagramPacket pkt = new DatagramPacket(
                    message, message.length, InetAddress.getByName(addr), 1337
            );

            try (DatagramSocket rspSock = new DatagramSocket()){rspSock.send(pkt);}
        }

        return node;
    }

    public void shutDown(Node node) throws IOException {

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

    }
}
