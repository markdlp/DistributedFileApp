package be.uantwerpen.distributedfileapp.Controller;


import be.uantwerpen.distributedfileapp.Model.Naming;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class NamingController {

    Map<Integer, String> nodeMap = Naming.getNodeMap();
}
