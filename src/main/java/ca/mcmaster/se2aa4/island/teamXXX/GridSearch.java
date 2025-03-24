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
    private boolean onLand; // Track if the drone is on land after a turn
    private boolean creekFound;
    private boolean siteFound;

    private int maxIslandHeight; //Maximum height of island.

    // Storage for CREEK IDS
    private String creekIDs;
    private String siteIDs;

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
        RIGHT_TURN("action", "heading", "RIGHT TURN"),
        LEFT_TURN("action", "heading", "LEFT TURN"),
        STOP ("action", "stop", "STOP");

        private String type; // Action key
        private String item; // Action item
        private int iterations; // Number of times this action has been made consecutively
        private String str; // Text for console logging
        private static final Logger logger = LogManager.getLogger();

        // Construct modes
        Mode(String type, String item, String str) {
            this.type = type;
            this.item = item;
            this.str = str;
            this.iterations = 0;
        }

        private String getString() {
            return str;
        }

        private void resetIterations() {
            iterations = 0;
        }

        private int getIterations() {
            return iterations;
        }

        /**
         * Build a JSON object corresponding to the action.
         */
        public JSONObject getDecision(DirectionStrategy direction) {
            JSONObject decision = new JSONObject();
            JSONObject parameters = new JSONObject();
            decision.put(type, item);

            // Add parameters to decisions that require them
            if (this == LEFT_TURN) {
                logger.info("Selected Left Turn");
                parameters.put("direction", direction.getLeftTurn().toString());
                decision.put("parameters", parameters);
            }
            else if (this == RIGHT_TURN) {
                logger.info("Selected Right Turn");
                parameters.put("direction", direction.getRightTurn().toString());
                decision.put("parameters", parameters);
            }
            
            this.iterations++;
            return decision;
        }

    }

    /**
     * Instantiate grid search.
     */
    public GridSearch(FindIsland island) {
        logger.info("===== GRID SEARCH INSTANTIATED =====");
        direction = island.getLandDirection(); // Fetch directional data from island. 
        previousTurn = Mode.RIGHT_TURN;
        mode = Mode.SCAN;
        creekFound = false;
        siteFound = false;
        turnRequested = false;
        maxIslandHeight = 2; //Maximum known island height
        onLand = false;
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
     * Switch decision modes based on the results of the previous action.
     */
    public Mode switchMode() {
        if (turnRequested) {
            if (mode.getIterations() == 2) {
                logger.info("TURN COMPLETE. Proceed to FLY.");
                mode.resetIterations();
                previousTurn = switchTurns();
                turnRequested = false;
                return Mode.FLY;
            } 
            else if (previousTurn == Mode.LEFT_TURN) {
                logger.info("PREVIOUSLY TURNED LEFT - TURN RIGHT.");
                return Mode.RIGHT_TURN;
            }
            else {
                logger.info("PREVIOUSLY TURNED RIGHT - TURN LEFT.");
                return Mode.LEFT_TURN;
            }
        }
        else if (mode == Mode.FLY) {
                logger.info("FLIGHT COMPLETE - SCAN.");
                return Mode.SCAN;
        }
        else if (mode == Mode.SCAN) {
            logger.info("Scan evaluation complete. Proceed to FLY.");
            return Mode.FLY;
        }
        else 
            return null;
    }

    /**
     * Update the stored value of the drone's heading if it is changed. 
     * Return the direction to be changed to.
     */
    public DirectionStrategy updateDirection() {
            if (mode == Mode.LEFT_TURN) {
                return direction.getLeftTurn();
            }
            else if (mode == Mode.RIGHT_TURN) {
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
        if (previousTurn == Mode.LEFT_TURN) {
            return Mode.RIGHT_TURN;
        }
        else {
            return Mode.LEFT_TURN;
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
        mode = switchMode();
    }

    /**
     * Evaluate the results of a scan.
     */
    public void checkScan(JSONObject data) {
        logger.info("Evaluating SCAN data");

        creekIDs = searchFor(data, "creeks", creekFound);
        siteIDs = searchFor(data, "sites", creekFound);
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
                logger.info("SURROUNDED BY WATER.");
                int tilesMoved = mode.getIterations(); // How many tiles have we travelled in a straight line?

                // Decide if a turn is required. 
                if (onLand) {
                    // We were on land previously and just encountered water! Turn around.
                    turnRequested = true;
                    onLand = false;

                    // If the tiles moved is greater than the previously known island height
                    if (tilesMoved > maxIslandHeight) {
                        maxIslandHeight = tilesMoved;
                    }
                    mode.resetIterations();
                }
                //If we have surpassed the island height by going straight we must make a u-turn
                else if (tilesMoved > maxIslandHeight) {
                    turnRequested = true;
                    previousTurn = switchTurns(); // Swap turns. Turn in opposite direction.
                }
            }
        }
        else {
            // We are not on water. Thus, we are on land. 
            onLand = true;
        }
    }

    /**
     * Search for an item in a JSON array
     */
    public String searchFor(JSONObject data, String area, boolean statusFlag) {
        if (data.has(area)) {
            logger.info("Check for " + area);
            JSONArray areaReport = data.getJSONArray(area);

            if (areaReport.length() != 0) {
                logger.info("============ DISCOVERY: " + area + " HAS BEEN FOUND ============");
                statusFlag = true;
                return (String)areaReport.get(0);
            }
            logger.info("NO " + area + " HAVE BEEN FOUND");

        }
        return null;
    }
}
