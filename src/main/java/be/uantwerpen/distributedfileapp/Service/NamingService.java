package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamingService {

    Map<Integer, String> nodeMap = Naming.getNodeMap();

    public Integer getFileOwner(Integer hashedName){

        int diff = 32768;// Biggest possible key difference
        int ownerID = 0; // ID of the file owner

        Map<Integer, String> N = new ConcurrentHashMap<>();

        for(Map.Entry<Integer, String> entry : nodeMap.entrySet())
            if(entry.getKey() < hashedName) N.put(entry.getKey(), entry.getValue());

        if (N.isEmpty()){
            for(Integer nodeKey : nodeMap.keySet()) if(nodeKey > ownerID) ownerID = nodeKey;
            // Maximum ID in the collection
            return ownerID;

        } else for(Integer nodeKey: N.keySet()){
            if (Math.abs(hashedName - nodeKey) < diff){
                diff = Math.abs(hashedName - nodeKey);
                ownerID = nodeKey;
            }
        }return ownerID;
    }
}
