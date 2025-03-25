package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test class for FindIsland
//Provides testable values 
//Debugging 

class FindIslandTest {

    private FindIsland findIsland;
    private FindGroundMock findGround; 

    @BeforeEach
    void setUp() {
        findGround = new FindGroundMock(); 
        findIsland = findGround.createFindIsland();
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

        int groundRange; 
        boolean landFound; 
        DirectionStrategy landDirection;

       public FindGroundMock(){
            landFound = true; 
            groundRange = 3; 
            landDirection = new North();      
        }
        public FindIsland createFindIsland() {
            return new FindIsland(groundRange, landDirection);
        }

    }

}
