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

public class FindCreek implements Searcher {
    private static final Logger logger = LogManager.getLogger();
    private final FindCoast findCoast;
    
    private int tilesToLand;
    private String landDirection;
    private boolean creekFound = false;
    private boolean movingToLand = true;
    private boolean searchingForCreek = false;
    private boolean shouldEcho = true;
    private DirectionStrategy currentDirectionStrategy;
    private String lastEchoResult = "";
    
    private Queue<String> pendingEchoes = new LinkedList<>();
    private boolean allDirectionsOutOfBounds = false;
    
    public FindCreek(FindCoast findCoast) {
        this.findCoast = findCoast;
        this.tilesToLand = findCoast.getGroundRange();
        this.landDirection = findCoast.getLandDirection();
    
        switch (landDirection) {
            case "N": this.currentDirectionStrategy = new North(); break;
            case "S": this.currentDirectionStrategy = new South(); break;
            case "E": this.currentDirectionStrategy = new East(); break;
            default: throw new IllegalArgumentException("Invalid land direction: " + landDirection);
        }
        
        // Initialize echo directions
        pendingEchoes.add("S");
        pendingEchoes.add("E");
    }

    @Override
    public JSONObject getDecision() {
        JSONObject decision = new JSONObject();
        JSONObject parameters = new JSONObject();
    
        if (creekFound) {
            logger.info("Creek found! Stopping.");
            decision.put("action", "stop");
            return decision;
        }
    
        if (movingToLand) {
            parameters.put("direction", landDirection);
            decision.put("action", "fly");
            tilesToLand--;
    
            if (tilesToLand <= 0) {
                movingToLand = false;
                searchingForCreek = true;
                shouldEcho = true;
                logger.info("Landed! Searching for creeks.");
            }
        } else if (searchingForCreek) {
            if (lastEchoResult.equals("GROUND")) {
                logger.info("Currently on ground. Scanning for creeks.");
                decision.put("action", "scan");
                return decision;
            }
    
            if (!pendingEchoes.isEmpty()) {
                String nextEchoDir = pendingEchoes.poll();
                parameters.put("direction", nextEchoDir);
                decision.put("action", "echo");
                decision.put("parameters", parameters);
                return decision;
            }
    
            if (allDirectionsOutOfBounds) {
                logger.info("All directions out of range. Moving forward.");
                parameters.put("direction", currentDirectionStrategy.getDirection().getChar());
                decision.put("action", "fly");
                
                pendingEchoes.add("S");
                pendingEchoes.add("E");
                allDirectionsOutOfBounds = false;
            }
        }
    
        decision.put("parameters", parameters);
        logger.info("Decision: {}", decision.toString());
        return decision;
    }

    @Override
    public void processResponse(String responseString) {
        JSONObject response = new JSONObject(responseString);
        if (!response.has("extras")) {
            logger.warn("Response missing 'extras' field.");
            return;
        }

        JSONObject extraInfo = response.getJSONObject("extras");

        if (extraInfo.has("creeks") && !extraInfo.getJSONArray("creeks").isEmpty()) {
            creekFound = true;
            String creekId = extraInfo.getJSONArray("creeks").getString(0);
            logger.info("Creek found! ID: {}", creekId);
            return;
        }

        if (extraInfo.has("found")) {
            lastEchoResult = extraInfo.getString("found");
            logger.info("Echo result: {}", lastEchoResult);
        }

        if ("GROUND".equals(lastEchoResult)) {
            logger.info("Found ground. Scanning for creeks.");
            shouldEcho = false;
        } else if ("OUT_OF_RANGE".equals(lastEchoResult)) {
            if (pendingEchoes.isEmpty()) {
                allDirectionsOutOfBounds = true;
            }
        }
    }
}
