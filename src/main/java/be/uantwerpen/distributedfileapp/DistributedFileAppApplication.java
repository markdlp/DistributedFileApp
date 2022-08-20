package be.uantwerpen.distributedfileapp;

import be.uantwerpen.distributedfileapp.Model.Node;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DistributedFileAppApplication {

    private static void loader(){
        Node node = new Node();

        List<File> list = new ArrayList<>();

        list.add(new File("files/lab3.pdf"));
        list.add(new File("files/lab4.pdf"));

        node.setFiles(list);
    }

    public static void main(String[] args) {
        loader();

        SpringApplication.run(DistributedFileAppApplication.class, args);
    }
}
