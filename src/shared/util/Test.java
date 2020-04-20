package shared.util;

import client.obj.Neighbour;
import client.obj.Neighbourhood;
import shared.data.Direction;
import shared.data.GameSettings;
import shared.obj.ClientNet;
import shared.obj.Connection;
import shared.obj.Coordinate;

public class Test {

    public static void main(String[] args) {

        matrix_test();

    }

    private static void clientNetSwap_test() {
        ClientNet net = clientNet_data();

        System.out.println("clientnet before swap:");
        net.print();

        net.swap(new Coordinate(0,0), new Coordinate(2,2));
        System.out.println("\nclientnet after swap");
        net.print();
    }

    private static void clientNetToList_test() {
        ClientNet net = clientNet_data();
        System.out.println(net.toList());
    }

    private static void gameSettingsSerialisation_test() {

        GameSettings gs = new GameSettings(100,250,1,4,3,1,32,18);
        GameSettings deserialised = GameSettings.fromSerialisedString(gs.toSerialisedString());

        System.out.println("deserialised vars:");
        System.out.println(deserialised.generations);
        System.out.println(deserialised.refreshRateMilliSeconds);
        System.out.println(deserialised.underpopulationThreshold);
        System.out.println(deserialised.overpopulationThreshold);
        System.out.println(deserialised.reproductionThreshold);
        System.out.println(deserialised.detectionRadius);
    }

    private static void neighbourDataChange_test() {
        Neighbour neighbourA = new Neighbour(new Connection("a",1), Direction.NORTH);
        Neighbour neighbourB = new Neighbour(new Connection("b",1), Direction.EAST);
        Neighbour neighbourC = new Neighbour(new Connection("c",1), Direction.SOUTH);
        Neighbour neighbourD = new Neighbour(new Connection("d",1), Direction.WEST);

        Neighbourhood neighbourhood = new Neighbourhood();
        neighbourhood.setNeighbour(neighbourA);
        neighbourhood.setNeighbour(neighbourB);
        neighbourhood.setNeighbour(neighbourC);
        neighbourhood.setNeighbour(neighbourD);
        for(Neighbour neigh : neighbourhood.toList()) {
            neigh.getConnection().isDataGathered = true;
        }
        for(Integer key : neighbourhood.keySet()) {
            System.out.println(neighbourhood.getNeighbour(key).getConnection().getIp() + " neighbour.isDataGathererd " + neighbourhood.getNeighbour(key).getConnection().isDataGathered);
        }
    }

    private static void directionComplement_test() {
        for(int d = 0; d < 8; d++)
            System.out.println("Complement of " + Direction.toVariableString(d) + ": " + Direction.toVariableString(Direction.getComplementaryDirection(d)));
    }

    private static void clientNet_test() {

        ClientNet net = clientNet_data();
        Connection e = net.get(1,1);

        net.print();

        System.out.println();

        Neighbourhood e_neighbours = net.getNeighbours(e);
        e_neighbours.print();

    }

    private static ClientNet clientNet_data() {
        ClientNet net = new ClientNet(3,3);

        Connection a = new Connection("a.com", 1);
        Connection b = new Connection("b.com", 2);
        Connection c = new Connection("c.com", 3);
        Connection d = new Connection("d.com", 4);
        Connection e = new Connection("e.com", 5);
        Connection f = new Connection("f.com", 6);
        Connection g = new Connection("g.com", 7);
        Connection h = new Connection("h.com", 8);
        Connection i = new Connection("i.com", 9);

        net.set(a, 0, 0);
        net.set(b, 0, 1);
        net.set(c, 0, 2);
        net.set(d, 1, 0);
        net.set(e, 1, 1);
        net.set(f, 1, 2);
        net.set(g, 2, 0);
        net.set(h, 2, 1);
        net.set(i, 2, 2);

        return net;
    }

    private static void matrix_test() {

        int[][] test = {
                {1,2,3,4,5,6,7,7,8},
                {9,10,11,12,13,14,15,16},
                {17,18,19,20,21,22,23,24},
                {25,26,27,28,29,30,31,32},
                {33,34,35,36,37,38,39,40},
                {41,42,43,44,45,46,47,48},
                {49,50,51,52,53,54,55,56},
                {57,58,59,60,61,62,63,64},
                {65,66,67,68,69,70,71,72},
                {73,74,75,76,77,78,79,80}
        };

        int[] data = {1,0,0,0,0,1,2,2,2};
        //MatrixUtility.print2dArray(MatrixUtility.appendRowAfter(test, data, Alignment.BOTTOM));

        int[][] test2 = {
                {1,2,3,4},
                {2,0,0,0},
                {3,0,0,0},
                {4,0,0,0}
        };

        int[][] injection = {
                {1,2,3,4,5,6,7},
                {2,2,3,4,5,6,7},
                {3,2,3,4,5,6,7},
                {4,2,3,4,5,6,7},
                {5,2,3,4,5,6,7},
                {6,2,3,4,5,6,7}
        };

        MatrixUtility.inject(test2, injection, Alignment.CENTER, Alignment.CENTER);

    }

    private static void matrixSetColSetRow_test() {
        int[][] test = {
                {1,2,3,4,5,6,7,8,0},
                {9,10,11,12,13,14,15,16,0},
                {17,18,19,20,21,22,23,24,0},
                {25,26,27,28,29,30,31,32,0},
                {33,34,35,36,37,38,39,40,0},
                {41,42,43,44,45,46,47,48,0},
                {49,50,51,52,53,54,55,56,0},
                {57,58,59,60,61,62,63,64,0},
                {65,66,67,68,69,70,71,72,0},
                {73,74,75,76,77,78,79,80,0}
        };

        int[] data = {1,0,0,2,2,2,3,3,3,4,4,4};
        MatrixUtility.print2dArray(MatrixUtility.setCol(test, 2, data, Alignment.CENTER));

        System.out.println();

        int[] data2 = {1,0,0,2,2};
        MatrixUtility.print2dArray(MatrixUtility.setRow(test, 2, data2, Alignment.CENTER));

    }

}
