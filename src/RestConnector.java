import openconnector.ConnectorConfig;
import openconnector.Log;
import org.richfaces.json.JSONArray;
import org.richfaces.json.JSONException;
import org.richfaces.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RestConnector extends AbstractRestConnectorV2 {
    ////////////////////////////////////////////////////////////////////////////
    //
    // CONSTANTS
    //
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTORS
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public RestConnector() {
        super();
        setAttributesAndFunctions();
    }

    /**
     * Constructor for an account CustomConnectorTest
     *
     * @param config The ConnectorConfig to use.
     * @param log    The Log to use.
     */
    public RestConnector(ConnectorConfig config, Log log) {
        super(config, log);
        setAttributesAndFunctions();
    }


    /**
     * Hardcoded debug constructor.
     */
    public RestConnector(String configUserName, String configUserPassword, String configUrl) {
        super(configUserName, configUserPassword, configUrl);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // INIT
    //
    ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void setAttributesAndFunctions() {

    }

    @Override
    public List<Feature> getSupportedFeatures(String objectType) {
        List<Feature> res = new ArrayList<>();

        return res;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // BASIC CRUD
    //
    ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createUser(Map<String, Object> user) throws ClassNotFoundException, IOException, JSONException {

    }

    @Override
    protected void updateUser(String id, Map<String, Object> user) throws ClassNotFoundException, IOException {

    }

    @Override
    protected void updateSetPassword(String id, Map<String, Object> user) throws ClassNotFoundException, IOException {

    }

    @Override
    protected void deleteUser(String id) throws ClassNotFoundException, IOException {

    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // OVERRIDDEN
    //
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    //
    // GETTERS AND SETTERS
    //
    ////////////////////////////////////////////////////////////////////////////


}
