package shared.data;

public class Direction {

    public static final int NORTH = 0;
    public static final int NORTH_EAST = 1;
    public static final int EAST = 2;
    public static final int SOUTH_EAST = 3;
    public static final int SOUTH = 4;
    public static final int SOUTH_WEST = 5;
    public static final int WEST = 6;
    public static final int NORTH_WEST = 7;
    private static final int COMPLEMENT = 8;

    public static String toVariableString(int direction) {
        switch (direction) {
            case NORTH: return "Direction.NORTH";
            case NORTH_EAST: return "Direction.NORTH_EAST";
            case EAST: return "Direction.EAST";
            case SOUTH_EAST: return "Direction.SOUTH_EAST";
            case SOUTH: return "Direction.SOUTH";
            case SOUTH_WEST: return "Direction.SOUTH_WEST";
            case WEST: return "Direction.WEST";
            case NORTH_WEST: return "Direction.NORTH_WEST";
            default: return "NULL";
        }
    }

    public static int getComplementaryDirection(int direction) {
        return (direction + COMPLEMENT/2) % COMPLEMENT;
    }

}
