package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * FindGround test class
 */

class FindGroundTest {

    private FindGround findGround;
    private FindIsland findIsland;
    private DirectionStrategy landDirection;

    @BeforeEach
    void setUp() {
        findGround = new FindGround();
        findIsland = new FindIsland(findGround); 
    }

    @Test
    void testInitialValues() {
        assertFalse(findGround.isLandFound());
        assertFalse(findGround.isStopIssued());
        assertEquals(-1, findGround.getGroundRange());
        assertFalse(findGround.isFlyingEast());
        assertNull(findGround.getLandDirection());

    }

    @Test
    void testSetStopIssued() {
        findGround.setStopIssued(true);
        assertTrue(findGround.isStopIssued());
    }

    @Test
    void testProcessResponseGroundNotFound() {

        System.out.println("If ground is not detected: ");
        String responseString = "{ \"extras\": { \"range\": \"0\", \"found\": \"OUT_OF_RANGE\" } }";
    
        findGround.processResponse(responseString);

        System.out.println("isLandFound: " + findGround.isLandFound());
        System.out.println("isFlyingEast: " + findGround.isFlyingEast());
    
        assertFalse(findGround.isLandFound());

    }

    @Test
    void testProcessResponseGroundFound() {

        String responseString = "{ \"extras\": { \"range\": \"2\", \"found\": \"GROUND\" } }";

        System.out.println("If a ground cell is detected: ");
        findGround.processResponse(responseString);

        assertTrue(findGround.isLandFound());
        assertEquals(2, findGround.getGroundRange());

    }
}
