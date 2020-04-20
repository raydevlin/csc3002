package shared.obj;

import client.obj.Neighbour;
import client.obj.Neighbourhood;
import shared.data.Direction;

import java.util.ArrayList;

/**
 * Java treats 2D arrays as an array of arrays.
 * Eg. int[5][4] consists of an array containing 5 int[4] objects.
 *     This can be visualised like so:
 *                ___________________
 *     int[0][]  |____|____|____|____|
 *     int[1][]  |____|____|____|____|
 *     int[2][]  |____|____|____|____|
 *     int[3][]  |____|____|____|____|
 *     int[4][]  |____|____|____|____|
 *
 * As such standard (x,y) addressing can be confusing and unintuitive
 * when dealing with 2D arrays. I will hide this confusing with inverted
 * getter and setter methods so that when dealing with a ClientNet object
 * (x,y) addressing is valid.
 *
 */
public class ClientNet {

    Connection[][] net;
    int width = 2;
    int height = 2;
    int count = 0;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getActiveConnectionCount() { return count; }

    public int getMaxConnections() { return width * height; }

    public ClientNet(int width, int height) {
        this.width = width;
        this.height = height;
        net = new Connection[height][width];
    }

    public ClientNet(ClientNet net) {
        this.width = net.width;
        this.height = net.height;
        this.net = new Connection[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                set(net.get(x,y),x,y);
            }
        }
    }

    public void set(Connection conn, Coordinate coordinate) {
        set(conn, coordinate.x, coordinate.y);
    }

    public void set(Connection conn, int x, int y) {
        if((x >= width || x < 0)
                || (y >= height || y < 0)) return;
        net[y][x] = conn;
        count++;
    }

    public Connection get(Coordinate coordinate) {
        return get(coordinate.x, coordinate.y);
    }

    public Connection get(int x, int y) {
        if((x >= width || x < 0)
                || (y >= height || y < 0)) return null;
        return net[y][x];
    }

    public Connection remove(int x, int y) {
        if((x >= width || x < 0)
                || (y >= height || y < 0)) return null;
        Connection temp = net[y][x];
        net[y][x] = null;
        if(temp != null) count--;
        return temp;
    }

    public Coordinate locate(Connection connection) {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Connection comparison = get(x,y);
                if(comparison == null) continue;
                if(connection.uuid.equals(comparison.uuid)) return new Coordinate(x, y);
            }
        }
        return new Coordinate(-1,-1);
    }

    public void swap(Coordinate coordinate1, Coordinate coordinate2) {

        Connection connection1 = get(coordinate1);
        Connection connection2 = get(coordinate2);

        if(connection1 == null && connection2 == null) return;

        set(connection2, coordinate1);
        set(connection1, coordinate2);

    }

    public Neighbourhood getNeighbours(Connection connection) {
        Coordinate location = locate(connection);
        Neighbourhood neighbours = new Neighbourhood();
        if(location.getX() < 0 || location.getY() < 0) return neighbours;

        Coordinate north = new Coordinate(location.getX(),
                                        ((location.getY()-1)<0?height-1:location.getY()-1));

        Coordinate east = new Coordinate(((location.getX()+1)>=width?0:location.getX()+1),
                                           location.getY());

        Coordinate south = new Coordinate(location.getX(),
                                        ((location.getY()+1)>=height?0:location.getY()+1));

        Coordinate west = new Coordinate(((location.getX()-1)<0?width-1:location.getX()-1),
                                           location.getY());

        Coordinate northWest = new Coordinate(((location.getX()-1)<0?width-1:location.getX()-1),
                                              ((location.getY()-1)<0?height-1:location.getY()-1));

        Coordinate northEast = new Coordinate(((location.getX()+1)>=width?0:location.getX()+1),
                                              ((location.getY()-1)<0?height-1:location.getY()-1));

        Coordinate southEast = new Coordinate(((location.getX()+1)>=width?0:location.getX()+1),
                                              ((location.getY()+1)>=height?0:location.getY()+1));

        Coordinate southWest = new Coordinate(((location.getX()-1)<0?width-1:location.getX()-1),
                                              ((location.getY()+1)>=height?0:location.getY()+1));

        if(get(north) != null) neighbours.setNeighbour(new Neighbour(get(north),Direction.NORTH));
        if(get(northEast) != null) neighbours.setNeighbour(new Neighbour(get(northEast),Direction.NORTH_EAST));
        if(get(east) != null) neighbours.setNeighbour(new Neighbour(get(east),Direction.EAST));
        if(get(southEast) != null) neighbours.setNeighbour(new Neighbour(get(southEast),Direction.SOUTH_EAST));
        if(get(south) != null) neighbours.setNeighbour(new Neighbour(get(south),Direction.SOUTH));
        if(get(southWest) != null) neighbours.setNeighbour(new Neighbour(get(southWest),Direction.SOUTH_WEST));
        if(get(west) != null) neighbours.setNeighbour(new Neighbour(get(west),Direction.WEST));
        if(get(northWest) != null) neighbours.setNeighbour(new Neighbour(get(northWest),Direction.NORTH_WEST));

        return neighbours;
    }

    public void print() {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                System.out.print(net[y][x] + "\t");
            }
            System.out.print("\n");
        }
    }

    public ArrayList<ClientNetEntity> toList() {
        ArrayList<ClientNetEntity> list = new ArrayList<>();
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                list.add(new ClientNetEntity(get(x,y), new Coordinate(x,y)));
            }
        }
        return list;
    }

    public boolean isEmpty() {
        return count <= 0;
    }

}

