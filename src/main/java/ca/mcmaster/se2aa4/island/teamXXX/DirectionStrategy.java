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

interface DirectionStrategy {
    DirectionStrategy getAlternativeDirection();
    Direction getDirection();
}

class East implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new South(); 
    }

    public Direction getDirection() {
        return Direction.EAST;
    }
}

class North implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new East();
    }

    public Direction getDirection() {
        return Direction.NORTH;
    }
}

class South implements DirectionStrategy {

    public DirectionStrategy getAlternativeDirection() {
        return new North();
    }

    public Direction getDirection() {
        return Direction.SOUTH;
    }
}