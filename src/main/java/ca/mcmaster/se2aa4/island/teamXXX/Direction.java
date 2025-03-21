package ca.mcmaster.se2aa4.island.teamXXX;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Direction {
    EAST ("E"),
    WEST ("W"),
    NORTH ("N"),
    SOUTH ("S");

    private String character;
    private final Logger logger = LogManager.getLogger();

    Direction (String character){
        this.character = character;
    }

    public String toString() {
        return character;
    }

}