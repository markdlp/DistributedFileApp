package be.uantwerpen.distributedfileapp.Model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Naming {
    private final Map<Integer, String> nodeMap = new ConcurrentHashMap<>();

    public Map<Integer, String> getNodeMap() {
        return nodeMap;
    }

    public static Integer getHash(String message){

        long times2Max = Integer.MAX_VALUE + Math.abs(Integer.MIN_VALUE);

        return Math.toIntExact(((message.hashCode() + Integer.MAX_VALUE) * 32768L) / times2Max);
    }
}
