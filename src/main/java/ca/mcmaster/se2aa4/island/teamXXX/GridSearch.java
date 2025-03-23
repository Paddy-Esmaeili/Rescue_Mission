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
    private DirectionStrategy direction; 
    private Mode mode; // Action modes.
    private Direction scanDirection;

    // Search Status Flags
    private boolean turnRequested;
    private boolean creekFound;
    private boolean siteFound;

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
        FLY("action", "fly", 3),
        SCAN("action", "scan",1),
        RIGHT_TURN("action", "heading",2),
        LEFT_TURN("action", "heading",2),
        STOP ("action", "stop",1);

        private String type; // Action key
        private String item; // Action item
        private int iterations; // Number of times this action has been made consecutively
        private int maxIterations; // Number of times this action has been made consecutively
        private static final Logger logger = LogManager.getLogger();

        // Construct modes
        Mode(String type, String item, int maxIterations) {
            this.type = type;
            this.item = item;
            this.maxIterations = maxIterations;
            this.iterations = 0;
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
        logger.info("Instantiating Grid Search. MODE: SCAN");
        direction = island.getLandDirection(); // Fetch directional data from island. 
        scanDirection = Direction.EAST;
        mode = Mode.SCAN;
        creekFound = false;
        siteFound = false;
        turnRequested = false;

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
        logger.info("Decision: {}", decision.toString());
        return decision;
    }

    /**
     * Switch decision modes based on the results of the previous action.
     */
    public Mode switchMode() {
        if (turnRequested) {
            logger.info("MAKING. TURN DIRECTION: " + scanDirection.toString());
            if (mode.getIterations() == 2) {
                mode.resetIterations();
                turnRequested = false;
                return Mode.FLY;
            } 
            else if (scanDirection == Direction.EAST) {
                return Mode.RIGHT_TURN;
            }
            else {
                return Mode.LEFT_TURN;
            }
        }
        else if (mode == Mode.FLY) {
            if (mode.getIterations() == 2){
                mode.resetIterations();
                return Mode.SCAN;
            }
            else {
                return Mode.FLY;
            }  
        }
        else if (mode == Mode.SCAN) {
            logger.info("Scan evaluation complete. Proceeding in CONTINUE mode");
            mode.resetIterations();
            return Mode.FLY;
        }
        else 
            return null;
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
            
            // If WATER is the only biome, turn around.
            if (biomes.length() == 1) {
                if (biomes.get(0).equals("OCEAN")) {
                    logger.info("SURROUNDED BY WATER. COMMENCE U-TURN");
                    turnRequested = true;
                }
            }
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
