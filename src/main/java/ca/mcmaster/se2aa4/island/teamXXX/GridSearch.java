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
     * Action Types
     */
    private enum Mode {
        FLY("action", "fly"),
        SCAN("action", "scan"),
        CONTINUE("action", "fly"),
        RIGHT_TURN("action", "heading"),
        LEFT_TURN("action", "heading");

        String type;
        String item;

        Mode(String type, String item) {
            this.type = type;
            this.item = item;
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

    private DirectionStrategy direction;
    private Mode mode;
    private boolean creekFound;
    private boolean siteFound;

    public GridSearch(FindIsland island) {
        mode = Mode.FLY;
        creekFound = false;
        siteFound = false;
    }

    @Override
    public boolean isComplete() {
        return creekFound && siteFound; 
    }
    
    public JSONObject getDecision() {
        return mode.getDecision(direction);
    }

    public Mode switchMode() {
        if (mode == Mode.FLY) {
            return Mode.SCAN;
        }
        else if (mode == Mode.SCAN) {
            return Mode.CONTINUE;
        }
        else if (mode == Mode.CONTINUE) {
            return Mode.FLY;
        }
        else 
            return null;
    }

    public void processResponse(String responseString) {
        JSONObject response = new JSONObject(responseString);

        if (mode == Mode.SCAN) {
            checkScan(responseString);
        }

        mode = switchMode();
    }

    public void checkScan(String response) {

    }

}