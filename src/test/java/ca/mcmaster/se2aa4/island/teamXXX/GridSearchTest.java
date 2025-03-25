package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GridSearchTest{

    private GridSearch gridSearch;
    private FindIsland findIsland; 
    private FindGround findGround; 

    @BeforeEach
    void setUp(){
        findGround = new FindGroundMock(); 
        findIsland = findGround.createFindIsland();
        gridSearch = new GridSearch(findIsland); 
    }

    //Initial values
    @Test
    void testInitialValues(){
        assertNull(gridSearch.getCreekId());
        assertNull(gridSearch.getSiteId());
        assertFalse(gridSearch.isComplete());
    }

    //Grid search finds a creek
    @Test
    void testProcessResponseCreekFound(){

        //Test case for creek id
        String response = "{ \"extras\": { \"creeks\": [\"MOCK-CREEK-ID\"] }}";
        gridSearch.processResponse(response);

        assertEquals("MOCK-CREEK-ID", gridSearch.getCreekId());

    }

    //Grid Search finds an emergency site
    @Test
    void testProcessResponseSiteFound(){

        //Test case for site id
        String response = "{ \"extras\": { \"sites\": [\"MOCK-SITE-ID\"] }}";

        gridSearch.processResponse(response);

        assertEquals("MOCK-SITE-ID", gridSearch.getSiteId());
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
