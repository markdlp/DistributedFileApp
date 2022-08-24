package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import be.uantwerpen.distributedfileapp.Model.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NodeService {

    public Node initNewNode(String nodeName, String addr){

        List<File> files = new ArrayList<>();

        File file = new File("files/lab3.pdf");
        files.add(file);

        return new Node(
                nodeName, addr, files,
                (int) Naming.getHash(nodeName), // todo: update previous & next Ids
                (int) Naming.getHash(nodeName)
        );
    }
}
