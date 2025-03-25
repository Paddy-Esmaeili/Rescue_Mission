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
import org.json.JSONArray;
import org.json.JSONTokener;
import java.util.Queue;
import java.util.LinkedList;

import ca.mcmaster.se2aa4.island.teamXXX.Direction;

/**
 * Grid Search
 * Execute a grid searching scan method.
 * Send a stop signal when a creek and emergency site are found.
 * 
 * Rosemarie Collier
 */
public class GridSearch implements Searcher, ResponseProcessor{

    private static final Logger logger = LogManager.getLogger(); 
    private DirectionStrategy direction; // The direction the drone is CURRENTLY FACING
    private Mode mode; // Action modes.
    private Mode previousTurn; // The Turn Direction the Drone Previously Made

    // Search Status Flags
    private boolean turnRequested;
    private boolean echoRequested;
    private boolean sweepDirectionChangeRequested;
    private boolean justFinishedTurning; // Have we just finished a turn? 
    private boolean creekFound;
    private boolean siteFound;

    // Storage for CREEK IDS
    private String creekId;
    private String siteId;

    /**
     * Mode
     * An enumeration of all potential action types.
     * FLY - Move forward one space
     * SCAN - Scan surroundings
     * RIGHT TURN - Turn right relative to the current direction
     * LEFT TURN - Turn left relative to the current direction
     * STOP - Send a signal to stop the game.
     */
    private enum Mode {
        FLY("action", "fly", "FLY"),
        SCAN("action", "scan", "SCAN"),
        RIGHT_TURN_A("action", "heading", "RIGHT TURN A"),
        RIGHT_TURN_B("action", "heading", "RIGHT TURN B"),
        LEFT_TURN_A("action", "heading", "LEFT TURN A"),
        LEFT_TURN_B("action", "heading", "LEFT TURN B"),
        ECHO ("action", "echo", "ECHO");

        private String type; // Action key
        private String item; // Action item
        private String str; // Text for console logging
        private static final Logger logger = LogManager.getLogger();

        // Construct modes
        Mode(String type, String item, String str) {
            this.type = type;
            this.item = item;
            this.str = str;
        }

        private String getString() {
            return str;
        }

        /**
         * Build a JSON object corresponding to the action.
         */
        public JSONObject getDecision(DirectionStrategy direction) {
            JSONObject decision = new JSONObject();
            JSONObject parameters = new JSONObject();
            decision.put(type, item);

            // Add parameters to decisions that require them
            if (this == LEFT_TURN_A || this == LEFT_TURN_B) {
                parameters.put("direction", direction.getLeftTurn().toString());
                decision.put("parameters", parameters);
            }
            else if (this == RIGHT_TURN_A || this == RIGHT_TURN_B) {
                parameters.put("direction", direction.getRightTurn().toString());
                decision.put("parameters", parameters);
            }
            else if (this == ECHO) {
                parameters.put("direction", direction.toString());
                decision.put("parameters", parameters);
            }
            
            return decision;
        }

    }

    /**
     * Instantiate grid search.
     */
    public GridSearch(FindIsland island) {
        logger.info("===== GRID SEARCH INSTANTIATED =====");
        direction = island.getLandDirection(); // Fetch directional data from island. 
        previousTurn = Mode.RIGHT_TURN_A;
        mode = Mode.SCAN;
        creekFound = false;
        siteFound = false;
        turnRequested = false;
        echoRequested = false;
        justFinishedTurning = false;
        sweepDirectionChangeRequested = false;
        creekId = null;
        siteId = null;
    }

    /**
     * Exists for console logging. 
     * Prints the status of the grid search.
     */
    private void printStatus(JSONObject decision) {
        logger.info("===== GRID SEARCH ====="
                    + "\n\t\tDIRECTION - " + direction.toString() 
                    + "\n\t\tPREVIOUS TURN - " + previousTurn.getString()
                    + "\n\t\tACTION MODE - " + mode.getString()
                    + "\n\t\tDecision: {}", decision.toString());
    }

    /**
     * Return true if the grid search is complete. 
     */
    @Override
    public boolean isComplete() {
        return creekFound && siteFound; 
    }

    /**
     * Return a JSON object containing next move data.
     */
    public JSONObject getDecision() {
        JSONObject decision = mode.getDecision(direction);
        direction = updateDirection();
        printStatus(decision);
        return decision;
    }

    /**
     * Return creek ID
     */
    public String getCreekId() {
        return creekId;
    }

    /**
     * Return site ID
     */
    public String getSiteId() {
        return siteId;
    }

    /**
     * Switch decision modes based on the results of the previous action.
     */
    public Mode switchMode() {
        if (sweepDirectionChangeRequested) {
            return getChangeSweep();
        }
        else if (turnRequested) {
            return getTurn();
        }
        else if (echoRequested) {
            logger.info("RECIEVED ECHO REQUEST - ECHO");
            return Mode.ECHO;
        }
        else if (mode == Mode.FLY) {
            logger.info("FLIGHT COMPLETE - SCAN.");
            return Mode.SCAN;
        }
        else if (mode == Mode.SCAN || mode == Mode.ECHO) {
            logger.info("Scan or echo evaluation complete. Proceed to FLY.");
            return Mode.FLY;
        }
        else {
            return null;
        }
    }

    /**
     * Change sweep direction process
     */
    private Mode getChangeSweep() {
        if (mode == Mode.RIGHT_TURN_B || mode == Mode.LEFT_TURN_B) {
            logger.info("Sweep direction change ended. SWITCH TO FLY.");
            sweepDirectionChangeRequested = false;
            return Mode.FLY;
        }
        else if (mode == Mode.FLY) {
            if (previousTurn == Mode.LEFT_TURN_A) {
                return Mode.LEFT_TURN_B;
            }
            else {
                return Mode.RIGHT_TURN_B;
            }
        }
        else if (mode == Mode.RIGHT_TURN_A || mode == Mode.LEFT_TURN_A) {
            return Mode.FLY;
        }
        else {
            return previousTurn; // Begin the CIRCLE BACK process
        }
    }

    /**
     * Return the next move when turning
     */
    private Mode getTurn() {
        if (mode == Mode.LEFT_TURN_A) {
            return Mode.LEFT_TURN_B;
        }
        else if (mode == Mode.RIGHT_TURN_A) {
            return Mode.RIGHT_TURN_B;
        }
        else if (mode == Mode.LEFT_TURN_B || mode == Mode.RIGHT_TURN_B) {
            logger.info("TURN COMPLETE. Proceed to FLY.");
            previousTurn = switchTurns();
            turnRequested = false;
            justFinishedTurning = true;
            return Mode.FLY;
        }
        else if (previousTurn == Mode.LEFT_TURN_A) {
            logger.info("PREVIOUSLY TURNED LEFT - TURN RIGHT.");
            return Mode.RIGHT_TURN_A;
        }
        else {
            logger.info("PREVIOUSLY TURNED RIGHT - TURN LEFT.");
            return Mode.LEFT_TURN_A;
        }
    }

    /**
     * Update the stored value of the drone's heading if it is changed. 
     * Return the direction to be changed to.
     */
    public DirectionStrategy updateDirection() {
            if (mode == Mode.LEFT_TURN_A || mode == Mode.LEFT_TURN_B) {
                return direction.getLeftTurn();
            }
            else if (mode == Mode.RIGHT_TURN_A || mode == Mode.RIGHT_TURN_B) {
                return direction = direction.getRightTurn();
            }
            else {
                return direction;
            }
    }

    /**
     * Return the alternate turn value
     */
    private Mode switchTurns() {
        // Log previous turn
        if (previousTurn == Mode.LEFT_TURN_A) {
            return Mode.RIGHT_TURN_A;
        }
        else {
            return Mode.LEFT_TURN_A;
        }
    }

    /**
     * After a decision is made, process results.
     */
    public void processResponse(String responseString) {
        JSONObject response = new JSONObject(responseString);
        logger.info(response.toString());

        // Fetch extra data
        if (!response.has("extras")) {  
            logger.warn("Response missing 'extras' field.");
            return;
        }
        
        JSONObject extraInfo = response.getJSONObject("extras");

        // Evaluate scan data
        if (mode == Mode.SCAN) {
            checkScan(extraInfo);
        }
        else if (mode == Mode.ECHO) {
            checkEcho(extraInfo);
        }
        mode = switchMode();
    }

    /**
     * Evaluate the results of an echo.
     */
    private void checkEcho(JSONObject data) {
        logger.info("Evaluating ECHO data");

        if (data.has("found")) {
            String found = data.getString("found");
            logger.info("Found: {}", found);
            if ("GROUND".equals(found)) {
                logger.info("There is ground ahead. Proceed forwards");
                justFinishedTurning = false;
            }
            else {
                logger.info("There is NO GROUND AHEAD. TURN AROUND!");
                turnRequested = true;

                if (justFinishedTurning) {
                    logger.info("======== ALERT ========\n JUST REACHED THE END \n========================");
                    sweepDirectionChangeRequested = true;
                }
            }
        }
        else {
            logger.warn("ECHO does not have FOUND field");
        }
        echoRequested = false; // Request complete. Reset.
    }

    /**
     * Evaluate the results of a scan.
     */
    public void checkScan(JSONObject data) {
        logger.info("Evaluating SCAN data");
        String results;
        
        results = searchFor(data, "creeks");
        if (results != null) {
            creekId = results;
            creekFound = true;
        }

        results = searchFor(data, "sites");
        if (results != null) {
            siteId = results;
            siteFound = true;
        }

        //Check biomes and change course if the drone is surrounded by water.         
        if (data.has("biomes")) {
            logger.info("Evaluating surroundings.");
            JSONArray biomes = data.getJSONArray("biomes");
            evaluateBiomes(biomes);
        }
    }

    /**
     * Evaluate the biomes
     * This method is very long. 
     * Consider refactoring in the future. 
     * This violates open-closed. 
     */
    public void evaluateBiomes(JSONArray biomes) {
        // If WATER is the only biome, turn around.
        if (biomes.length() == 1 && !turnRequested) {
            if (biomes.get(0).equals("OCEAN")) {
                logger.info("SURROUNDED BY WATER. REQUEST AN ECHO");
                echoRequested = true;
            }
            else {
                justFinishedTurning = false; // On ground. Did not just finish turning.
            }
        }
        else {
            justFinishedTurning = false; // On ground. Did not just finish turning.
        }
    }

    /**
     * Search for an item in a JSON array
     */
    public String searchFor(JSONObject data, String area) {
        if (data.has(area)) {
            logger.info("Check for " + area);
            JSONArray areaReport = data.getJSONArray(area);

            if (areaReport.length() != 0) {
                logger.info("============ DISCOVERY: " + area + " HAS BEEN FOUND ============");
                return (String)areaReport.get(0);
            }
            logger.info("NO " + area + " HAVE BEEN FOUND");

        }
        return null;
    }
}
