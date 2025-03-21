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
    DirectionStrategy getLeftTurn();
    DirectionStrategy getRightTurn();
    Direction getDirection();
    String toString();
}

class East implements DirectionStrategy {

    public DirectionStrategy getLeftTurn() {
        return new North(); 
    }

    public DirectionStrategy getRightTurn() {
        return new South(); 
    }

    public DirectionStrategy getAlternativeDirection() {
        return new South(); 
    }

    public Direction getDirection() {
        return Direction.EAST;
    }

    public String toString() {
        return Direction.EAST.toString();
    }
}

class North implements DirectionStrategy {

    public DirectionStrategy getLeftTurn() {
        return new West(); 
    }

    public DirectionStrategy getRightTurn() {
        return new East(); 
    }

    public DirectionStrategy getAlternativeDirection() {
        return new East();
    }

    public Direction getDirection() {
        return Direction.NORTH;
    }

    public String toString() {
        return Direction.NORTH.toString();
    }
}

class South implements DirectionStrategy {

    public DirectionStrategy getLeftTurn() {
        return new East(); 
    }

    public DirectionStrategy getRightTurn() {
        return new West(); 
    }

    public DirectionStrategy getAlternativeDirection() {
        return new North();
    }

    public Direction getDirection() {
        return Direction.SOUTH;
    }

    public String toString() {
        return Direction.SOUTH.toString();
    }
}

class West implements DirectionStrategy {

    public DirectionStrategy getLeftTurn() {
        return new South(); 
    }

    public DirectionStrategy getRightTurn() {
        return new North(); 
    }

    public DirectionStrategy getAlternativeDirection() {
        return new North();
    }

    public Direction getDirection() {
        return Direction.WEST;
    }

    public String toString() {
        return Direction.WEST.toString();
    }
}