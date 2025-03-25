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

public class Battery {
    public static final int BATTERY_CAPACITY = 20000;
    public static final int LOW_BATTERY = 2500;
    public int batteryLevel;
    private static final Logger logger = LogManager.getLogger();

    public Battery() {
        this.batteryLevel = BATTERY_CAPACITY;
    }

    /**
     * Examine Battery Capacity
     * Deplete battery level if the command can be excecuted.
     * Returns true if the response can be handled.
     */
    public boolean hasCapacity() {
        if (batteryLevel <= LOW_BATTERY) {
            logger.info("LOW BATTERY. ABORTING.");
            return false;
        }
        else {
            logger.info("BATTERY STATUS: " + batteryLevel);
            return true;
        }
    }

    public void depleteBattery(String s) {
        JSONObject decision = new JSONObject(s);
        if (decision.has("cost")) {
            int cost = (int)decision.get("cost");
            batteryLevel = batteryLevel - cost; 
        }
        else{
            logger.warn("ERROR. DECISION MISSING COST FIELD");
        }

    }
}