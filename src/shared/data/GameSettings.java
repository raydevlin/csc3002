package shared.data;

import client.obj.Neighbour;
import shared.obj.Connection;

public class GameSettings {

    public int generations = 100;
    public int refreshRateMilliSeconds = 250;
    public int underpopulationThreshold = 1;
    public int overpopulationThreshold = 4;
    public int reproductionThreshold = 3;
    public int detectionRadius = 1;
    public int clientCellWidth = 32;
    public int clientCellHeight = 18;
    public int[][] data;

    public GameSettings(int generations, int refreshRateMilliSeconds, int underpopulationThreshold,
                        int overpopulationThreshold, int reproductionThreshold, int detectionRadius,
                        int clientCellWidth, int clientCellHeight) {
        this.generations = generations;
        this.refreshRateMilliSeconds = refreshRateMilliSeconds;
        this.underpopulationThreshold = underpopulationThreshold;
        this.overpopulationThreshold = overpopulationThreshold;
        this.reproductionThreshold = reproductionThreshold;
        this.detectionRadius = detectionRadius;
        this.clientCellWidth = clientCellWidth;
        this.clientCellHeight = clientCellHeight;
    }

    public String toSerialisedString() {
        String separator = StaticData.gameSettingsSerialisationSeparator;
        return generations + separator +
                        refreshRateMilliSeconds + separator +
                        underpopulationThreshold + separator +
                        overpopulationThreshold + separator +
                        reproductionThreshold + separator +
                        detectionRadius + separator +
                        clientCellWidth + separator +
                        clientCellHeight;
    }

    public static GameSettings fromSerialisedString(String serialisedString) {
        String[] vars = serialisedString.split(StaticData.gameSettingsSerialisationSeparator);

        return new GameSettings(
            Integer.parseInt(vars[0]),
            Integer.parseInt(vars[1]),
            Integer.parseInt(vars[2]),
            Integer.parseInt(vars[3]),
            Integer.parseInt(vars[4]),
            Integer.parseInt(vars[5]),
            Integer.parseInt(vars[6]),
            Integer.parseInt(vars[7])
        );
    }
}
