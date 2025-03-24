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
    private Searcher searchMethod;    
    private Battery battery;
    private ResponseProcessor responseProcessor;

    public Explorer() {
        FindGround initialSearch = new FindGround();
        this.searchMethod = initialSearch;
        this.responseProcessor = initialSearch;
        this.battery = new Battery();

    }

    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}", info.toString(2));
    }

    public String takeDecision() {
        JSONObject decision = searchMethod.getDecision();
        if (battery.hasCapacity()) {
            return decision.toString();
        }
        else {
            // Stop and return home if there is no battery!
            logger.info("LOW BATTERY. STOPPING.");
            decision = new JSONObject();
            decision.put("action", "stop");
            return decision.toString();
        }

    }

    public boolean isComplete() {
        return searchMethod.isComplete();
    }

    public void acknowledgeResults(String s) {
        if (responseProcessor != null) {
            battery.depleteBattery(s);
            responseProcessor.processResponse(s);
        }

        if (searchMethod.isComplete()) {
            if (searchMethod instanceof FindGround) {
                logger.info("Switching to FindIsland...");
                searchMethod = ((FindGround) searchMethod).getFindIsland();
                responseProcessor = null; 
            } 
            else if (searchMethod instanceof FindIsland) {
                logger.info("Switching to GridSearch...");
                searchMethod = ((FindIsland) searchMethod).getGridSearch();
                responseProcessor = (ResponseProcessor) searchMethod; 
            }
        }
    }

    public String deliverFinalReport() {
        return "no creek found";
    }
}
