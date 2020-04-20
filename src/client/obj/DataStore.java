package client.obj;

import java.util.HashMap;

public class DataStore {

    HashMap<Integer, Integer> intDatamap;
    HashMap<Integer, int[]> arrayDatamap;
    HashMap<Integer, int[][]> generations;

    public DataStore() {
        this.intDatamap = new HashMap<>();
        this.arrayDatamap = new HashMap<>();
        this.generations = new HashMap<>();
    };

    public void setData(int direction, int[] data) {
        if(arrayDatamap.containsKey(direction)) arrayDatamap.replace(direction, data);
        else arrayDatamap.put(direction, data);
    }

    public void setData(int direction, int data) {
        if(intDatamap.containsKey(direction)) intDatamap.replace(direction, data);
        else intDatamap.put(direction, data);
    }

    public int[] getData(int direction) {
        return arrayDatamap.getOrDefault(direction, null);
    }

    public int getIntData(int direction) {
        return intDatamap.getOrDefault(direction, -1);
    }

    public void saveGeneration(int generation, int[][] data) {
        if(generations.containsKey(generation)) generations.replace(generation, data);
        else generations.put(generation, data);
    }

    public int[][] getGeneration(int generation) {
        return generations.getOrDefault(generation, null);
    }


}
