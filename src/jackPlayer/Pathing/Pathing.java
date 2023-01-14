package jackPlayer.Pathing;

import battlecode.common.*;

public abstract class Pathing {
    private Direction alongObstacleDir = null;
    private MapLocation currentTarget = null;
    public RobotController rc;
    public Tracker tracker;

    public Pathing (RobotController rc) {
        this.rc = rc;
        this.tracker = new Tracker();
    }

    public void move(MapLocation target) throws GameActionException {
        BFSMove(target);
    }

    // Reset tracker if new target has been specified else continue pathing with current tracker
    void update(MapLocation target) {
        if (currentTarget == null || !target.equals(currentTarget)) {
            tracker.reset();
        }
        currentTarget = target;
        tracker.add(rc.getLocation());
    }

    public void bugMove(MapLocation target) throws GameActionException {
        // Cool down active, can't move
        if (!rc.isMovementReady()) {
            return;
        }

        // Verify it's not at target location
        if (rc.getLocation().equals(target)) {
            return;
        }

        // Get direction towards target
        Direction dir = rc.getLocation().directionTo(target);

        // Move if no wall is present in the direction && bytecode is available to check
        if (rc.canMove(dir)) {
            rc.move(dir);
            alongObstacleDir = null;
        } else {
            // Set the along the obstacle direction to current direction
            if (alongObstacleDir == null) alongObstacleDir = dir;

            for (int i = 0; i < 8; i++) {

                if (rc.canMove(alongObstacleDir)) {
                    rc.move(alongObstacleDir);
                    // Turn back towards obstacle
                    alongObstacleDir = alongObstacleDir.rotateLeft();
                    return;
                }

                // Keep rotating direction until it finds an empty space to move in
                alongObstacleDir = alongObstacleDir.rotateRight();
            }
        }
    }

    public void BFSMove(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (rc.getLocation().equals(target)) return;

        update(target);

        // Target is adjacent to my location & is unoccupied & is passable
        if (rc.getLocation().distanceSquaredTo(target) <= 2) {
            if (rc.isLocationOccupied(target) && rc.sensePassability(target)) {
                Direction dir = rc.getLocation().directionTo(target);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
            return;
        }

        // Get the best direction to move in computed by bellman ford
        Direction dir = getBestDirection(target);

        // Attempt to move if best direction was found, hasn't been seen in the tracker
        // (assuming our destination hasn't changed), and we can move else we will attempt a
        // possible not optimal bug move
        if (dir != null && !tracker.check(rc.getLocation().add(dir)) && rc.canMove(dir)) {
            rc.move(dir);
            tracker.add(rc.getLocation());
        } else {
            bugMove(target);
        }
    }

    public abstract Direction getBestDirection(MapLocation target) throws GameActionException;
}
