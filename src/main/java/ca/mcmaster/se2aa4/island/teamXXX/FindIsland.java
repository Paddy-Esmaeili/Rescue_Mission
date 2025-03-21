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

        if (movingToLand) {
            decision.put("heading", direction.toString());
            parameters.put("direction", direction.toString());
            decision.put("action", "fly");
            tilesToLand--;

            if (tilesToLand <= 0) {
                movingToLand = false;
                logger.info("Arrived at ground cell! Stopping and preparing for Grid Search!.");
                gridSearch = new GridSearch(this);
            }
        }

        decision.put("parameters", parameters);
        logger.info("Decision: {}", decision.toString());
        return decision;
    }

    @Override
    public void processResponse(String responseString) {
        //We don't need to use this method
    }
}
