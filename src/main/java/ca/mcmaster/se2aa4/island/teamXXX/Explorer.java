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

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private Searcher searchMethod = new FindGround();
    private boolean groundFound = false;

    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}", info.toString(2));
    }

    public String takeDecision() {
        if (!groundFound && searchMethod instanceof FindGround) {
            JSONObject decision = searchMethod.getDecision();
            
            return decision.toString();
        } 

        return searchMethod.getDecision().toString();
    }

    public boolean isComplete() {
        if (searchMethod instanceof FindGround) {
            FindGround findGround = (FindGround) searchMethod;
            return findGround.isLandFound() && findGround.getGroundRange() != -1;
        }

        return false;
    }

    public void acknowledgeResults(String s) {
        searchMethod.processResponse(s);
    
        if (searchMethod instanceof FindGround && searchMethod.isComplete()) {
            logger.info("Switching to FindIsland...");
            searchMethod = ((FindGround) searchMethod).getFindIsland();
        }
        else if (searchMethod instanceof FindIsland && searchMethod.isComplete()) {
            logger.info("Switching to GridSearch...");
            searchMethod = ((FindIsland) searchMethod).getGridSearch();
        }
    }

    public String deliverFinalReport() {
        return "no creek found";
    }
}
