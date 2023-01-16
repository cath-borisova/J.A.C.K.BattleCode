package jackPlayer;

import battlecode.common.*;
import jackPlayer.Communications.Communications;
import jackPlayer.Communications.Headquarter;
import jackPlayer.Communications.Well;

import java.util.*;

public class CarrierController extends Controller {

    private MapLocation headquarter;
    private MapLocation wellLocation;
    private ResourceType wellType;

    public CarrierController(RobotController rc) throws GameActionException {
        super(rc);
        assignWell(rc);
        assignHQ(rc);
    }

    private void assignWell(RobotController rc) throws GameActionException {
        List<Well> wells = Communications.getWells(rc);
        if (wells == null)
            return; // TODO: Add logic here beside just waiting

        MapLocation curLoc = rc.getLocation();
        // If this is too expensive switch to repeatedly taking the minimum
        Collections.sort(wells, Comparator.comparingInt(o -> curLoc.distanceSquaredTo(o.getMapLocation())));
        for (Well well : wells) {
            if (well.getWorkerCount() <= 15 /* || well.getPressure() < 5 */ ) {
                wellLocation = well.getMapLocation();
                wellType = well.getType();
                Communications.incrementWellWorkers(rc, well);
                break;
            }
        }
    }

    private void assignHQ(RobotController rc) throws GameActionException {
        List<Headquarter> headQuarters = Communications.getHeadQuarters(rc);
        if (headQuarters == null || headQuarters.size() == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots()) {
                if (robot.getType() == RobotType.HEADQUARTERS) {
                    MapLocation curLoc = rc.getLocation();
                    if (headquarter == null ||
                            curLoc.distanceSquaredTo(robot.getLocation()) < curLoc.distanceSquaredTo(headquarter)) {
                        headquarter = robot.getLocation();
                    }
                }
            }
        } else {
            MapLocation curLoc = rc.getLocation();
            headquarter = headQuarters.stream().min(
                    Comparator.comparingInt(o -> curLoc.distanceSquaredTo(o.getMapLocation()))
            ).get().getMapLocation();
        }
    }

    private void attemptCollect(RobotController rc) throws GameActionException {
        if (rc.canCollectResource(wellLocation, -1)) {
            rc.collectResource(wellLocation, -1);
        }
    }

    private void attemptDeposit(RobotController rc) throws GameActionException {
        if (headquarter.isAdjacentTo(rc.getLocation()) && rc.isActionReady()) {
            int exAmount = rc.getResourceAmount(ResourceType.ELIXIR);
            if (exAmount > 0) {
                rc.transferResource(headquarter, ResourceType.ELIXIR, exAmount);
                return;
            }
            int adAmount = rc.getResourceAmount(ResourceType.ADAMANTIUM);
            if (adAmount > 0) {
                rc.transferResource(headquarter, ResourceType.ADAMANTIUM, adAmount);
                return;
            }
            int mnAmount = rc.getResourceAmount(ResourceType.MANA);
            if (mnAmount > 0) {
                rc.transferResource(headquarter, ResourceType.MANA, mnAmount);
                return;
            }
        }
    }

    private int totalHeld(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                rc.getResourceAmount(ResourceType.ELIXIR) +
                rc.getResourceAmount(ResourceType.MANA);
    }

    @Override
    public void run(RobotController rc) throws GameActionException {
        super.run(rc);

        assignHQ(rc);
        if (wellLocation == null) {
            assignWell(rc);
            if (wellLocation == null)
                return;
        }
        rc.setIndicatorString("Assigned Well: " + wellLocation.x + ", " + wellLocation.y);

        attemptCollect(rc);
        attemptDeposit(rc);
        if (totalHeld(rc) < 40) {
            moveTowards(rc, wellLocation);
        } else {
            moveTowards(rc, headquarter);
        }
        attemptCollect(rc);
        attemptDeposit(rc);
    }


    public static void attack(RobotController rc) throws GameActionException {
        if (rc.isActionReady()) {
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
            int indexAttack = -1;
            int health = 100;
            for (int i = 0; i < enemies.length; i++) {
                int enemyHealth = enemies[i].getHealth();
                RobotInfo enemy = enemies[i];
                if(enemy.getType().equals(RobotType.CARRIER) || enemy.getType().equals(RobotType.DESTABILIZER) || enemy.getType().equals(RobotType.LAUNCHER)) {
                    if(enemyHealth == rc.getType().damage){
                        indexAttack = i;
                        break;
                    } else if (enemyHealth < health) {
                        indexAttack = i;
                        health = enemyHealth;
                    }
                }
            }
            if (indexAttack >= 0) {
                MapLocation enemyLoc = enemies[indexAttack].getLocation();
                if (rc.canAttack(enemyLoc)) {
                    rc.attack(enemyLoc);
                }
            } else if (enemies.length > 0) { //there exist enemies in the action range, but they are all at full health
                MapLocation enemyLoc = enemies[0].getLocation();
                if (rc.canAttack(enemyLoc)) {
                    rc.attack(enemyLoc);
                }
            }
        }
    }

}
