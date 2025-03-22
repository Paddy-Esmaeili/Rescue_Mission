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

public class FindIsland implements Searcher {
    private static final Logger logger = LogManager.getLogger();
    private final FindGround findGround;
    private int tilesToLand;
    private DirectionStrategy direction;
    private boolean movingToLand = true;
    private boolean headingChanged = false; //Tracks changes in the heading 
    private GridSearch gridSearch;

    public FindIsland(FindGround findGround) {
        this.findGround = findGround;
        this.tilesToLand = findGround.getGroundRange();
        this.direction = findGround.getLandDirection();
    }

    public GridSearch getGridSearch(){
        return gridSearch;
    }

    public DirectionStrategy getLandDirection() {
        return direction;
    }

    /**
     * Return true if the drone has reached the ground cell
     */
    public boolean isComplete() {
        return !movingToLand;
    }

    @Override
    public JSONObject getDecision() {
        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();

        if (!headingChanged) { //Change heading only once before flying 
            logger.info("TURNING TO ISLAND DIRECTION: " + direction.toString());
            parameters.put("direction", direction.toString());
            decision.put("action", "heading");
            decision.put("parameters", parameters);
            headingChanged = true; //The heading has been updated. 
        } 
        else if (movingToLand) {
            decision.put("action", "fly");
            tilesToLand--;    
        }

        if (tilesToLand <= 0) {
            movingToLand = false;
            logger.info("Arrived at ground cell! Stopping and preparing for GridSearch!.");
            gridSearch = new GridSearch(this);
        }

        logger.info("Decision: {}", decision.toString());
        return decision;
    }
}
