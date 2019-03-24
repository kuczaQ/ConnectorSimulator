import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.richfaces.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
@RunWith(OrderedRunner.class)
public class RestConnectorTest {
    private static RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
    private static RestConnector restConnector;

    private static       String USER_KEY       = "atester420".toUpperCase(),
                                USER_EMAIL     = "atester420@ventum.com",
                                USER_NAME      = "Adam",
                                USER_LAST_NAME = "Tester";

    
    private static       String USER_UPDATE_EMAIL     = "mwazowski999@ventum.com",
                                USER_UPDATE_NAME      = "Marek",
                                USER_UPDATE_LAST_NAME = "Wazowski";

    static {
        restConnector = new RestConnector(
                "not used",
                "also not used",
                "http://localhost:8080/")
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
        restConnector.getJsonUserDataAsJSONArray();
    }

    @Test
    @Order(20)
    public void getObjectsMap() {
        restConnector.getObjectsMap().size();
    }

    @Test
    @Order(30)
    public void createUser() throws JSONException, IOException, ClassNotFoundException {
        Map<String, Object> userCreateMap = new HashMap<>();
        userCreateMap.put(getAttrKey(),      USER_KEY);
        userCreateMap.put(getAttrEmail(),    USER_EMAIL);
        userCreateMap.put(getAttrName(),     USER_NAME);
        userCreateMap.put(getAttrLastName(), USER_LAST_NAME);


        restConnector.createUser(userCreateMap);

        Map<String, Object> createdUser = restConnector.getUser(USER_KEY);

        if (mapDoesntContainElements(
                userCreateMap,
                createdUser,
                getAttrKey(),
                getAttrEmail(),
                getAttrName(),
                getAttrLastName()))
            throw new AssertionError("user != createdUser");
    }


    @Test
    @Order(35)
    public void getUser() throws IOException, JSONException {
        Logger logger = restConnector.getLogger();

        String userName = USER_KEY;

        Map user = restConnector.getUser(userName);

        logger.info(user);

        if (user == null || user.isEmpty())
            throw new AssertionError("user is null or empty");
    }

    @Test
    @Order(40)
    public void updateUser() throws IOException, ClassNotFoundException {
        Map<String, Object> userUpdateMap = new HashMap<>();

        userUpdateMap.put(getAttrKey(),      USER_KEY);
        userUpdateMap.put(getAttrEmail(),    USER_UPDATE_EMAIL);
        userUpdateMap.put(getAttrName(),     USER_UPDATE_NAME);
        userUpdateMap.put(getAttrLastName(), USER_UPDATE_LAST_NAME);
        
        restConnector.updateUser(USER_KEY, userUpdateMap);

        Map<String, Object> updatedUser = restConnector.getObjectsMap().get(USER_KEY);

        if (mapDoesntContainElements(
                userUpdateMap,
                updatedUser,
                getAttrEmail(),
                getAttrName(),
                getAttrLastName()))
            throw new AssertionError("user == updatedUser");
    }

    @Test
    @Order(60)
    public void deleteUser() throws IOException, ClassNotFoundException {
        restConnector.deleteUser(USER_KEY);

        Map<String, Object> deletedUser = restConnector.getObjectsMap().get(USER_KEY);

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

        userUpdateMap.put(getAttrKey(),      userKey);
        userUpdateMap.put(getAttrEmail(),    randomStringGenerator.getEmail());
        userUpdateMap.put(getAttrName(),     randomStringGenerator.getString());
        userUpdateMap.put(getAttrLastName(), randomStringGenerator.getString());

        try {
            restConnector.updateUser(userKey, userUpdateMap);
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

    private String getAttrLastName() {
        return "lastName";
    }

    private String getAttrName() {
        return "firstName";
    }

    private String getAttrEmail() {
        return "email";
    }

    private String getAttrKey() {
        return "id";
    }
}