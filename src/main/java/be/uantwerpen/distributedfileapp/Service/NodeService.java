package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Model.Node;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NodeService {

    public void initNewNode(String nodeName, String addr){

        // Adding local files to node's file List
        List<File> files = Arrays.stream(
                (Objects.requireNonNull(new File("files").listFiles()))
        ).toList();

        new Node(
                nodeName, addr, files,
                (int) Naming.getHash(nodeName), // todo: update previous & next Ids
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
}
