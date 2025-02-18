package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

interface DirectionStrategy {
    DirectionStrategy getAlternativeDirection();
    String getDirection();
}

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private final FindCoast findCoast = new FindCoast(); 

    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}", info.toString(2));
    }

    public String takeDecision() {
        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();
    
        if (findCoast.isLandFound() && !findCoast.isStopIssued()) {
            decision.put("action", "stop");
            findCoast.setStopIssued(true);
            logger.info("Ground found! Stopping and returning to base.");
        } else if (findCoast.isFlyingEast()) {
            parameters.put("direction", "E");
            decision.put("action", "fly");
            findCoast.incrementTilesFlown();
        } else {
            parameters.put("direction", findCoast.getNextDirection());
            decision.put("action", "echo");
        }
    
        decision.put("parameters", parameters);
        logger.info("** Decision: {}", decision.toString());
        return decision.toString();
    }

    public void acknowledgeResults(String s) {
        findCoast.processResponse(s); 
    }

    public String deliverFinalReport() {
        return "no creek found";
    }
}

class East implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new South(); 
    }

    public String getDirection() {
        return "E";
    }
}

class North implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new East();
    }

    public String getDirection() {
        return "N";
    }
}

class South implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new North();
    }

    public String getDirection() {
        return "S";
    }
}

class FindCoast {
    private static final Logger logger = LogManager.getLogger();
    private DirectionStrategy currentDirection = new North();
    private boolean landFound = false;
    private String landDirection = "";
    private boolean stopIssued = false; 
    
    private boolean isFlyingEast = false;
    private int tilesFlown = 0;

    private int outOfRangeCount = 0;
    private final int MAX_OUT_OF_RANGE = 3;

    public String getNextDirection() {
        if (landFound && !stopIssued) {
            stopIssued = true;
            logger.info("Ground found! Stopping and returning to base.");
            return "stop";
        }

        if (isFlyingEast) {
            return "E";
        } else {
            return currentDirection.getDirection();
        }
    }

    public boolean isFlyingEast() {
        return isFlyingEast;
    }

    public void incrementTilesFlown() {
        tilesFlown++;
        logger.info("Flying East. Tile count: {}", tilesFlown);

        if (tilesFlown >= 3) {
            tilesFlown = 0;
            outOfRangeCount = 0;
            isFlyingEast = false;
            currentDirection = new North();
            logger.info("Completed 3 tiles East. Echoing all directions again.");
        }
    }

    public boolean isLandFound() {
        return landFound;
    }

    public String getLandDirection() {
        return landDirection;
    }

    public boolean isStopIssued() {
        return stopIssued;
    }

    public void setStopIssued(boolean stopIssued) {
        this.stopIssued = stopIssued;
    }

    public void processResponse(String responseString) {
        JSONObject response = new JSONObject(responseString);

        if (!response.has("extras")) {
            logger.warn("Response missing 'extras' field.");
            return;
        }

        JSONObject extraInfo = response.getJSONObject("extras");

        if (extraInfo.has("found")) {
            String found = extraInfo.getString("found");
            logger.info("Found: {}", found);

            if ("OUT_OF_RANGE".equals(found)) {
                outOfRangeCount++;
                logger.warn("Object is out of range.");
                currentDirection = currentDirection.getAlternativeDirection();

                if (outOfRangeCount >= MAX_OUT_OF_RANGE && !isFlyingEast) {
                    isFlyingEast = true;
                    tilesFlown = 0;
                    logger.info("All directions are out of range. Starting to fly East.");
                }
            } else if ("GROUND".equals(found) && !landFound) {
                landFound = true;
                landDirection = currentDirection.getDirection();
                logger.info("Ground found in direction {}. Issuing stop command.", landDirection);
                isFlyingEast = false;
                outOfRangeCount = 0;
                tilesFlown = 0;
            }
        }
    }
}
