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

//Locates land and then activates creek searching. 
public class FindCoast implements Searcher {

    private static final Logger logger = LogManager.getLogger();
    private FindCreek findCreek;
    private DirectionStrategy currentDirection = new North();
    private boolean landFound = false;
    private String landDirection = "";
    private boolean stopIssued = false; 
    
    private boolean isFlyingEast = false;
    private int tilesFlown = 0;

    private int outOfRangeCount = 0;
    private final int MAX_OUT_OF_RANGE = 3;
    private int groundRange = -1;     //When a ground cell is found, this variable stores how many tiles ahead of the drone is the ground cell.
    
    //Getter methods
    public int getGroundRange() {
        return groundRange;
    }
    public FindCreek getFindCreek() {
        return findCreek;
    }
    public boolean isComplete() {
        return landFound && groundRange != -1;
    }

    @Override
    public JSONObject getDecision() {

        if (findCreek != null){
            return findCreek.getDecision();
        }

        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();
    
        if (isFlyingEast()) {
            parameters.put("direction", Direction.EAST.getChar());
            decision.put("action", "fly");
            incrementTilesFlown();
        } else {
            parameters.put("direction", getNextDirection());
            decision.put("action", "echo");
        }

        decision.put("parameters", parameters);
        logger.info("** Decision: {}", decision.toString());

        return decision;
    }

    public String getNextDirection() {
        if (landFound && !stopIssued) {
            //stopIssued = true;
            logger.info("Ground found! Searching for creek now...");
            return "scan";
        }

        if (isFlyingEast) {
            return Direction.EAST.getChar();
        } else {
            return currentDirection.getDirection().getChar();
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

    @Override
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
                if (extraInfo.has("range")){
                   groundRange = extraInfo.getInt("range");
                   logger.info("The first ground cell is {} tiles ahead", groundRange);
                }
                landFound = true;
                landDirection = currentDirection.getDirection().getChar();
                logger.info("Ground found in direction {}.", landDirection);
                isFlyingEast = false;
                outOfRangeCount = 0;
                tilesFlown = 0;
                findCreek = new FindCreek(this);
            }
        }
    }
}
