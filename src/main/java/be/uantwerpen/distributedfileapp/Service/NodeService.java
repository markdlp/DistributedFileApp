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

            ServerSocket serverSocket = new ServerSocket(4995);

            for(int i = 0; i < numOfNodes; i++) try (
                    Socket s = serverSocket.accept()
            ) {
                InputStreamReader in = new InputStreamReader(s.getInputStream());
                BufferedReader bf = new BufferedReader(in);

                String fromNode = bf.readLine();

                s.close();

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

            serverSocket.close();

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

        if(currentID < newNodeHash && currentNode.getNextID() > newNodeHash){
            currentNode.setNextID(newNodeHash);

            Socket s = new Socket(addr, 4995);

            PrintWriter pr = new PrintWriter(s.getOutputStream());
            InputStreamReader in = new InputStreamReader(s.getInputStream());

            pr.println(String.valueOf(currentID) + '@' + currentNode.getNextID() + '@');
            pr.flush(); s.close();
        }

        if(currentNode.getPreviousID() < newNodeHash && currentID > newNodeHash){
            currentNode.setPreviousID(newNodeHash);

            Socket s = new Socket(addr, 4995);

            PrintWriter pr = new PrintWriter(s.getOutputStream());
            InputStreamReader in = new InputStreamReader(s.getInputStream());

            pr.println(String.valueOf(currentID) + '@' + currentNode.getPreviousID() + '@');
            pr.flush(); s.close();
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

    public String failure(Integer ID){




        return "Failure Proc Completed for host: "+ nodeMap.remove(ID);
    }
}