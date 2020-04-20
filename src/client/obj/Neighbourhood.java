package client.obj;

import shared.data.Direction;
import shared.data.StaticData;
import shared.obj.Connection;

import java.util.*;

public class Neighbourhood {

    HashMap<Integer, Neighbour> neighbours;

    public Neighbourhood() { neighbours = new HashMap<>(); }

    public Neighbour getNeighbour(int direction) {
        if(!neighbours.containsKey(direction)) return null;
        return neighbours.get(direction);
    }

    public void setNeighbour(Neighbour neighbour) {
        setNeighbour(neighbour, neighbour.direction);
    }

    private void setNeighbour(Neighbour neighbour, int direction) {
        if(neighbours.containsKey(direction))
            neighbours.replace(direction, neighbour);
        else neighbours.put(direction, neighbour);
    }

    public void removeNeighbour(int direction) {
        neighbours.remove(direction);
    }

    public Set<Integer> keySet() { return neighbours.keySet(); }

    public String toSerialisedString() {
        String serialisation = "";
        String separator = StaticData.neighbourhoodSerialisationSeparator;

        int count = 0;
        for(Integer key : neighbours.keySet()) {
            serialisation += key + separator
                            + neighbours.get(key).getConnection().getIp() + separator
                            + neighbours.get(key).getConnection().getPort() + separator
                            + neighbours.get(key).getConnection().getUuid();
            if(count < neighbours.keySet().size()-1) serialisation += separator;
            count++;
        }

        return serialisation;
    }

    public static Neighbourhood fromSerialisedString(String serialisedString) {
        Neighbourhood neighbourhood = new Neighbourhood();
        String[] vars = serialisedString.split(StaticData.neighbourhoodSerialisationSeparator);

        for(int i = 0; i < vars.length; i+= 4) {
            if(i+4 > vars.length) break;
            int direction = Integer.parseInt(vars[i]);
            String ip = vars[i+1];
            int port = Integer.parseInt(vars[i+2]);
            String uuid = vars[i+3];
            Connection connection = new Connection(ip, port, uuid);
            Neighbour neighbour = new Neighbour(connection, direction);
            neighbourhood.setNeighbour(neighbour);
        }

        return neighbourhood;
    }

    public void print() {
        for(Integer key : neighbours.keySet()) {
            System.out.println("neighbour[" + Direction.toVariableString(key) + "]: " + neighbours.get(key).getConnection());
        }
    }

    public ArrayList<Neighbour> toList() {
        ArrayList<Neighbour> neighbourList = new ArrayList<>();
        for(Integer key : neighbours.keySet()) {
            neighbourList.add(neighbours.get(key));
        }
        return neighbourList;
    }

}
