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

public class FindCreek implements Searcher {
    private static final Logger logger = LogManager.getLogger();
    private DirectionStrategy currentDirection = new North();
    private boolean landFound = false;
    private String landDirection = "";
    private boolean stopIssued = false; 
    
    private boolean isFlyingEast = false;
    private int tilesFlown = 0;

    private int outOfRangeCount = 0;
    private final int MAX_OUT_OF_RANGE = 3;

    @Override
    public JSONObject getDecision() {

        return null;
    }

    @Override
    public void processResponse(String responseString) {
        
    }

}
