package ca.mcmaster.se2aa4.island.teamXXX;

public enum Direction {
    EAST ("E"),
    WEST ("W"),
    NORTH ("N"),
    SOUTH ("S");

    private String character;

    Direction (String character){
        this.character = character;
    }

    public String toString() {
        return character;
    }

}