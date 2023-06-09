package jackPlayer.Communications;

import battlecode.common.MapLocation;
import battlecode.common.ResourceType;

public class Well {

    public Well(int wellIndex, boolean amplifierPresent, int workerCount, int pressure, ResourceType type, MapLocation mapLocation) {
        this.wellIndex = wellIndex;
        this.amplifierPresent = amplifierPresent;
        this.workerCount = workerCount;
        this.pressure = pressure;
        this.type = type;
        this.mapLocation = mapLocation;
    }

    private int wellIndex;
    private boolean amplifierPresent;
    private int workerCount;
    private int pressure;
    private ResourceType type;
    private MapLocation mapLocation;

    public ResourceType getType() {
        return type;
    }

    public MapLocation getMapLocation() {
        return mapLocation;
    }

    public boolean isAmplifierPresent() {
        return amplifierPresent;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int getPressure() {
        return pressure;
    }

    public int getWellIndex() {
        return wellIndex;
    }

}
