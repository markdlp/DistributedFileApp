package be.uantwerpen.distributedfileapp.Model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Naming {
    private static final Map<Integer, String> nodeMap = new ConcurrentHashMap<>();

    public static Map<Integer, String> getNodeMap() {
        return nodeMap;
    }

    public static short getHash(String message){

        // Choosing 32479 instead of 32768 because a prime number spreads the distribution of hashsn
        // values better -> [0, 32479] : 289 values left out
        int tmp = (message.hashCode() % 32479);

        return (short) tmp;
    }
}