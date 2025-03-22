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

    // Search Status Flags
    private boolean creekFound;
    private boolean siteFound;

    /**
     * Mode
     * An enumeration of all potential action types.
     * FLY - Move forward one space
     * SCAN - Scan surroundings
     * CONTINUE - After scanning, move forward one space.
     * RIGHT TURN - Turn right relative to the current direction
     * LEFT TURN - Turn left relative to the current direction
     */
    private enum Mode {
        FLY("action", "fly", "FLY"),
        SCAN("action", "scan", "SCAN"),
        CONTINUE("action", "fly", "CONTINUE"),
        RIGHT_TURN("action", "heading", "RIGHT_TURN"),
        LEFT_TURN("action", "heading", "LEFT_TURN"),
        STOP ("action", "stop", "STOP");

        private String type; // Action key
        private String item; // Action item
        private String str; // String description for logging. Dont use if not needed
        private static final Logger logger = LogManager.getLogger();

        // Construct modes
        Mode(String type, String item, String str) {
            this.type = type;
            this.item = item;
            this.str = str; // Unused as of now. Exists if logging is needed.
        }

        /**
         * Get the mode in string form for log messages
         * Remove this later if not needed.
         */
        public String toString() {
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
            else if (this == FLY || this == CONTINUE) {
                logger.info("Selected Fly Forward");
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
        logger.info("Instantiating Grid Search. MODE: SCAN");
        direction = island.getLandDirection(); // Fetch directional data from island. 
        mode = Mode.SCAN;
        creekFound = false;
        siteFound = false;
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
        if (mode == Mode.FLY) {
            logger.info("Switching to SCAN mode");
            return Mode.SCAN;
        }
        else if (mode == Mode.SCAN) {
            logger.info("Scan evaluation complete. Proceeding in CONTINUE mode");
            return Mode.CONTINUE;
        }
        else if (mode == Mode.CONTINUE) {
            logger.info("Switching to FLY mode");
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

        //Check for creeks and set flags. 
        if (data.has("creeks")) {
            logger.info("Check for creeks.");
            JSONArray creeks = data.getJSONArray("creeks");

            
        }
        //Check for emergency sites and set flags. 
        if (data.has("sites")) {
            logger.info("Check for emergency sites.");
            JSONArray sites = data.getJSONArray("sites");
        }

        //Check biomes and change course if the drone is surrounded by water. 

    }
}
