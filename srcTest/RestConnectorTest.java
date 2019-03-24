import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.richfaces.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(OrderedRunner.class)
public class RestConnectorTest {
//    private static RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
    private static RestConnector restConnector;

    private static       String USER_KEY       = "atester420".toUpperCase(),
                                USER_EMAIL     = "atester420@ventum.com",
                                USER_NAME      = "Adam",
                                USER_LAST_NAME = "Tester",
                                USER_PASSWORD  = "Ventum2018";

    
    private static       String USER_UPDATE_EMAIL     = "mwazowski999@ventum.com",
                                USER_UPDATE_NAME      = "Marek",
                                USER_UPDATE_LAST_NAME = "Wazowski",
                                USER_UPDATE_PASSWORD  = "Ventum2999";

    static {
        restConnector = new RestConnector(
                "IDMSFP010",
                "",
                "http://localhost:8090/")
                {
                    @Override
                    protected String getIdentityAttribute() {
                        return "id";
                    }
                };
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // TEST FOR SUCCESS
    //
    ////////////////////////////////////////////////////////////////////////////


    @Test
    @Order(10)
    public void getJsonUserDataAsJSONArray() throws IOException, JSONException {
        digiReRestConnector.getJsonUserDataAsJSONArray();
    }
ProxyClient
    @Test
    @Order(20)
    public void getObjectsMap() {
        digiReRestConnector.getObjectsMap().size();
    }

    @Test
    @Order(30)
    public void createUser() throws JSONException, IOException, ClassNotFoundException {
        Map<String, Object> userCreateMap = new HashMap<>();
        userCreateMap.put(DigiReRestConnector.getAttrKey(),      USER_KEY);
        userCreateMap.put(DigiReRestConnector.getAttrEmail(),    USER_EMAIL);
        userCreateMap.put(DigiReRestConnector.getAttrName(),     USER_NAME);
        userCreateMap.put(DigiReRestConnector.getAttrLastName(), USER_LAST_NAME);
        userCreateMap.put(digiReRestConnector.getAttrPassword(), USER_PASSWORD);


        digiReRestConnector.createUser(userCreateMap);

        Map<String, Object> createdUser = digiReRestConnector.getObjectsMap().get(USER_KEY);

        if (mapDoesntContainElements(
                userCreateMap,
                createdUser,
                DigiReRestConnector.getAttrKey(),
                DigiReRestConnector.getAttrEmail(),
                DigiReRestConnector.getAttrName(),
                DigiReRestConnector.getAttrLastName()))
            throw new AssertionError("user != createdUser");
    }

    @Test
    @Order(35)
    public void getUser() throws IOException, JSONException {
        Logger logger = digiReRestConnector.getLogger();

        String userName = USER_KEY;

        Map user = digiReRestConnector.getUser(userName);

        logger.info(user);

        if (user == null)
            throw new AssertionError("user == null");
    }

    @Test
    @Order(40)
    public void updateUser() throws IOException, ClassNotFoundException {
        Map<String, Object> userUpdateMap = new HashMap<>();

        userUpdateMap.put(DigiReRestConnector.getAttrKey(),      USER_KEY);
        userUpdateMap.put(DigiReRestConnector.getAttrEmail(),    USER_UPDATE_EMAIL);
        userUpdateMap.put(DigiReRestConnector.getAttrName(),     USER_UPDATE_NAME);
        userUpdateMap.put(DigiReRestConnector.getAttrLastName(), USER_UPDATE_LAST_NAME);
        
        digiReRestConnector.updateUser(USER_KEY, userUpdateMap);

        Map<String, Object> updatedUser = digiReRestConnector.getObjectsMap().get(USER_KEY);

        if (mapDoesntContainElements(
                userUpdateMap,
                updatedUser,
                DigiReRestConnector.getAttrEmail(),
                DigiReRestConnector.getAttrName(),
                DigiReRestConnector.getAttrLastName()))
            throw new AssertionError("user == updatedUser");
    }

    @Test
    @Order(50)
    public void updateSetPassword() throws IOException, ClassNotFoundException {
        Map<String, Object> updatePasswordMap = new HashMap<>();

        updatePasswordMap.put(DigiReRestConnector.getAttrKey(),      USER_KEY);
        updatePasswordMap.put(digiReRestConnector.getAttrPassword(), USER_UPDATE_PASSWORD);

        digiReRestConnector.updateSetPassword(USER_KEY, updatePasswordMap);
    }

    @Test
    @Order(60)
    public void deleteUser() throws IOException, ClassNotFoundException {
        digiReRestConnector.deleteUser(USER_KEY);

        Map<String, Object> deletedUser = digiReRestConnector.getObjectsMap().get(USER_KEY);

        if (deletedUser != null)
            throw new AssertionError("Deleted user still exists!");
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // TEST FOR FAILURE
    //
    ////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(70)
    public void updateNonexistentUser() {
        Map<String, Object> userUpdateMap = new HashMap<>();

        final String userKey = randomStringGenerator.getUserName();

        userUpdateMap.put(DigiReRestConnector.getAttrKey(),      userKey);
        userUpdateMap.put(DigiReRestConnector.getAttrEmail(),    randomStringGenerator.getEmail());
        userUpdateMap.put(DigiReRestConnector.getAttrName(),     randomStringGenerator.getString());
        userUpdateMap.put(DigiReRestConnector.getAttrLastName(), randomStringGenerator.getString());

        try {
            digiReRestConnector.updateUser(userKey, userUpdateMap);
        } catch (Exception e) {
            return;
        }

        // if we are here than no exceptions were thrown == FAIL
        throw new AssertionError("Test for failure succeeded!");
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // HELPER METHODS
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param original the map that contains all of the fields
     * @param toCheck map to check
     * @param elementsToCheck names of the keys as strings
     * @return true if all of <i>elementsToCheck</i> are present in the <i>original</i> and <i>toCheck</i> AND they represent the same mappings
     */
    private static boolean mapDoesntContainElements(Map<String, Object> original, Map<String, Object> toCheck, String... elementsToCheck) {
        for (String s : elementsToCheck)
            if (!original.get(s).equals(toCheck.get(s)))
                return true;

        return false;
    }
}