package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Model.Node;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NodeService {

    public Node initNewNode(String nodeName, String addr){

        // Adding local files to node's file List
        List<File> files = Arrays.stream(
                (Objects.requireNonNull(new File("files").listFiles()))
        ).toList();

        return new Node(
                nodeName, addr, files,
                (int) Naming.getHash(nodeName), // todo: update previous & next Ids
                (int) Naming.getHash(nodeName)
        );
    }
}
