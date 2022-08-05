package be.uantwerpen.distributedfileapp.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Getter @Setter
public class Node {

    private String name;

    private String ip;

    private List<File> files;

    private Integer previousID;
    private Integer nextID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return name.equals(node.name) && ip.equals(node.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ip);
    }

    // ================ Functions =====================================
}
