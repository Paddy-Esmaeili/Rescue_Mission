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

<<<<<<< HEAD
//Locates land and calls FindIsland to fly towards it. 
//Flies East 3 tiles at the time and echoes all directions until a ground cell is detected. 

=======
//Locates land and then activates creek searching. 
>>>>>>> 8c133591dfce9fb947e8c4443945577dae13c8b0
public class FindGround implements Searcher, ResponseProcessor {

    private static final Logger logger = LogManager.getLogger();
    private FindIsland findIsland;
    private boolean landFound = false;
    private boolean stopIssued = false; 
    
    private boolean isFlyingEast = false;
    private int tilesFlown = 0;

    // DIRECTIONAL DATA
    private DirectionStrategy scanDirection = new North();
    private DirectionStrategy landDirection = null;

    private int outOfRangeCount = 0;
    private final int MAX_OUT_OF_RANGE = 3;
    private int groundRange = -1;  // Stores how many tiles ahead the ground cell is when found.

    public FindIsland createFindIsland() {
        if (!landFound) {
            throw new IllegalStateException("Error: No island detected.");
        }
        return new FindIsland(groundRange, landDirection);
    }

    //getter method
    public FindIsland getFindIsland() {
        return findIsland;
    }

    public boolean isComplete() {
        return landFound && groundRange != -1;
    }

    @Override
    public JSONObject getDecision() {
        if (findIsland != null) {
            return findIsland.getDecision();
        }

        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();
    
        if (isFlyingEast()) {
            parameters.put("direction", Direction.EAST.toString());
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
            logger.info("Ground found! Searching for island now...");
            return "scan";
        }

        return isFlyingEast ? Direction.EAST.toString() : scanDirection.getDirection().toString();
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
            scanDirection = new North();
            logger.info("Completed 3 tiles East. Echoing all directions again.");
        }
    }

    public boolean isLandFound() {
        return landFound;
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
                scanDirection = scanDirection.getAlternativeDirection();

                if (outOfRangeCount >= MAX_OUT_OF_RANGE && !isFlyingEast) {
                    isFlyingEast = true;
                    tilesFlown = 0;
                    logger.info("All directions are out of range. Starting to fly East.");
                }
            } else if ("GROUND".equals(found) && !landFound) {
                if (extraInfo.has("range")) {
                   groundRange = extraInfo.getInt("range");
                   logger.info("The first ground cell is {} tiles ahead", groundRange);
                }
                landFound = true;
                logger.info("Ground found in direction {}.", scanDirection.toString());
                landDirection = scanDirection;
                isFlyingEast = false;
                outOfRangeCount = 0;
                tilesFlown = 0;
                findIsland = new FindIsland(groundRange, landDirection);
            }
        }
    }
}


