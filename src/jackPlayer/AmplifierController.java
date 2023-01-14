package jackPlayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import jackPlayer.Pathing.AmplifierPathing;

public class AmplifierController extends Controller {

    public AmplifierController(RobotController rc) {
        super(rc);
        pathing = new AmplifierPathing(rc);
    }

    @Override
    public void run(RobotController rc) throws GameActionException {
        super.run(rc);

    }
}
