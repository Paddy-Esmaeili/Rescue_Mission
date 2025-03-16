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
    private Searcher searchMethod = new FindCoast();
    private boolean coastFound = false;

    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}", info.toString(2));
    }

    public String takeDecision() {
        if (!coastFound && searchMethod instanceof FindCoast) {
            JSONObject decision = searchMethod.getDecision();

            if ("stop".equals(decision.optString("action"))) {
                logger.info("FindCoast completed. Switching to FindCreek.");
                searchMethod = new FindCreek((FindCoast) searchMethod);
                coastFound = true;
                return new JSONObject().put("action", "wait").toString(); 
            }
            
            return decision.toString();
        } 

        return searchMethod.getDecision().toString();
    }

    public boolean isComplete() {
        if (searchMethod instanceof FindCoast) {
            FindCoast findCoast = (FindCoast) searchMethod;
            return findCoast.isLandFound() && findCoast.getGroundRange() != -1;
        }
        return false;
    }

    public void acknowledgeResults(String s) {
        searchMethod.processResponse(s);
    
        if (searchMethod instanceof FindCoast findCoast && findCoast.isComplete()) {
            logger.info("Switching to FindCreek...");
            searchMethod = ((FindCoast) searchMethod).getFindCreek();
        }
    }

    public String deliverFinalReport() {
        return "no creek found";
    }
}
