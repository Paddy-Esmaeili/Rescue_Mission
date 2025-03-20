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
import java.util.Queue;
import java.util.LinkedList;

import ca.mcmaster.se2aa4.island.teamXXX.Direction;

public class GridSearch implements Searcher {

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
        FLY("action", "fly", "flying forward"),
        SCAN("action", "scan", "scanning surroundings"),
        CONTINUE("action", "fly", "continuing"),
        RIGHT_TURN("action", "heading", "turning right"),
        LEFT_TURN("action", "heading", "turning left");

        private String type;
        private String item;
        private String str;
        private static final Logger logger = LogManager.getLogger();

        Mode(String type, String item, String str) {
            this.type = type;
            this.item = item;
            this.str = str;
        }

        /**
         * Get the mode in string form for log messages
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
                parameters.put("direction", direction.getLeftTurn().toString());
                decision.put("parameters", parameters);
            }
            else if (this == RIGHT_TURN) {
                parameters.put("direction", direction.getRightTurn().toString());
                decision.put("parameters", parameters);
            }
            else if (this == FLY || this == CONTINUE) {
                parameters.put("direction", direction.toString());
                decision.put("parameters", parameters);
            }

            return decision;
        }

    }

    private static final Logger logger = LogManager.getLogger();
    private DirectionStrategy direction;
    private Mode mode;
    private boolean creekFound;
    private boolean siteFound;

    public GridSearch(FindIsland island) {
        logger.info("Instantiating Grid Search. MODE: FLY");
        mode = Mode.FLY;
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
        return mode.getDecision(direction);
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
            logger.info("Scan complete. Proceeding in CONTINUE mode");
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

        if (mode == Mode.SCAN) {
            logger.info("Evaluating SCAN data");
            checkScan(responseString);
        }

        mode = switchMode();
    }

    public void checkScan(String response) {

    }

}