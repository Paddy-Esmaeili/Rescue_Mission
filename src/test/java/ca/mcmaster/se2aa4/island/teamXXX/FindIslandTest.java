package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FindIslandTest {

    private FindGroundMock findGround;    //Passing predefined values to FindGroundMock
    private FindIsland findIsland;

    @BeforeEach
    void setUp() {
        findGround = new FindGroundMock();
        findIsland = new FindIsland(findGround);
    }

    @Test
    void testInitialValues() {
        assertFalse(findIsland.isComplete());    //FindIsland is not initially completed
    }

    @Test
    void testGetDecision() {

        System.out.println("Test case: Ground is located 3 tiles in the North direction.");
        JSONObject lastDecision = null;
    
        while (!findIsland.isComplete()) {
            lastDecision = findIsland.getDecision();
        }
    
        assertTrue(findIsland.isComplete());

        //Call GridSearch upon completion of FindIsland.
        assertNotNull(findIsland.getGridSearch());    

    }

    //Mock class to provide testable values
    private static class FindGroundMock extends FindGround {
        @Override
        public int getGroundRange() {
            return 3; //Ground is located 3 tiles ahead
        }

        @Override
        public DirectionStrategy getLandDirection() {
            return new North(); //The direction of the first ground cell is North. 
        }
    }
}
