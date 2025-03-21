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
    private String landDirection;
    private boolean movingToLand = true;

    public FindIsland(FindGround findGround) {
        this.findGround = findGround;
        this.tilesToLand = findGround.getGroundRange();
        this.landDirection = findGround.getLandDirection();
    }

    public GridSearch gridSearch(){
        return gridSearch();
    }

    @Override
    public JSONObject getDecision() {
        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();

        if (movingToLand) {
            decision.put("heading", landDirection);
            parameters.put("direction", landDirection);
            decision.put("action", "fly");
            tilesToLand--;

            if (tilesToLand <= 0) {
                movingToLand = false;
                logger.info("Arrived at ground cell! Stopping.");
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
