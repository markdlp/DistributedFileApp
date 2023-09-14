package be.uantwerpen.distributedfileapp.Service;

import be.uantwerpen.distributedfileapp.Model.Naming;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NamingService {

    Map<Integer, String> nodeMap = Naming.getNodeMap();

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private static final Date date = new Date();

    public Integer getFileOwner(Integer hashedName){

        int diff = 32768;// Biggest possible key difference
        int ownerID = 0; // IDof the file owner

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

    public String addNode(String message) {

        // using delimiter as '@' cause
        String nodeName = message.split("@")[0];
        String ip = message.split("@")[1];

        log.info(formatter.format(date)
                + " Request to add Node: " + nodeName
                + " Ip: " + ip + " And ID: " + Naming.getHash(nodeName)
        );

        // add node if it doesn't exist already
        nodeMap.putIfAbsent((int)Naming.getHash(nodeName), ip);

        return Integer.toString(nodeMap.size());
    }

    public Integer getNextId(Integer Id) {
        int tmp = 0;

        for (Map.Entry<Integer, String> entry : nodeMap.entrySet()) {
            if (Id > entry.getKey() && tmp < entry.getKey()) tmp = entry.getKey();
        }
        if (tmp == 0) tmp = getBiggestKey();
        return tmp;
    }

    private int getBiggestKey() {
        int tmp = 0;
        for (Map.Entry<Integer, String> entry : nodeMap.entrySet()) {
            if (entry.getKey() > tmp) tmp = entry.getKey();
        }
        return tmp;
    }

    public Integer getPrevId(Integer Id) {
        int tmp = 32768;

        for (Map.Entry<Integer, String> entry : nodeMap.entrySet()) {
            if (Id < entry.getKey() && tmp > entry.getKey()) tmp = entry.getKey();
        }
        if (tmp == 32768) tmp = getSmallestKey();
        return tmp;
    }

    private int getSmallestKey() {
        int tmp = 32768;
        for (Map.Entry<Integer, String> entry : nodeMap.entrySet()) {
            if (entry.getKey() < tmp) tmp = entry.getKey();
        }
        return tmp;
    }
}
