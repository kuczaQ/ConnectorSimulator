/*
 * tak, ty to napisaÅ‚eÅ›...
 */

import openconnector.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.richfaces.json.JSONArray;
import org.richfaces.json.JSONException;
import org.richfaces.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;

/**
 * <h2>REMEMBER!</h2>
 * <p>The implementing class always has to call <b><u>ALL</u></b> of the INITIALIZE METHODS
 * <ul>
 * 		<b>Methods to call:</b>
 * 
 * 		<li><code>void setLogger(String logger)</code></li>
 * 		<li><code>void setPasswordAttribute(String ATTR_PASSWORD)</code></li>
 * </ul>
 * </p>
 * 
 * <p>
 * 	<b>Additionally you specify which types you want to support and call corresponding methods.</b>
 * 	<ul>
 * 		<li>For accounts:</li>
 * 			<ul>
 *  			<li>void supportAccounts()</li>
 * 				<li>void setAttributesAccount(String[] ATTRIBUTES)</li>
 * 				<li>void setGetAllUserAddress(String getAllUserAddress)</li>
 * 			</ul>
 * 		<li>For groups:</li>
 * 			<ul>
 *  			<li>void supportGroups()</li>
 * 				<li>void setAttributesGroup(String[] ATTRIBUTES)</li>
 * 				<li>void setGetAllGroupsAddress(String getAllGroupsAddress)</li>
 * 			</ul>
 * 	</ul>
 * @author Adam Mazurkiewicz - Madej
 */
@SuppressWarnings("squid:S1659") // ignore one line declarations
public abstract class AbstractRestConnectorV2 extends AbstractConnector {
	
	////////////////////////////////////////////////////////////////////////////
	//
	// STATIC FIELDS
	//
	////////////////////////////////////////////////////////////////////////////


	
	
	protected static final String ERROR_MSG_UNKNOWN_OBJECT = "Unhandled object type: ";



	////////////////////////////////////////////////////////////////////////////
	//
	// LOCAL FIELDS
	//
	////////////////////////////////////////////////////////////////////////////

	protected Logger logger = null;
	
	@SuppressWarnings("squid:S2068") // false positive (password)
	protected String attrPassword              = null; //	Yes, this is indeed a comment.
								  	 		           //
	protected String    getAllUserAddress      = null, //	These fields should be final, but aren't, 'cause they can't be since I need
					    createUserAddress      = null, //
					    updateUserAddress      = null, //
	 				    deleteUserAddress      = null, //
	 				    passwordUserAddress    = null, //
	 				    enableUserAddress      = null, //
						getAllGroupsAddress    = null; //
											 	       //
	protected String[]  attributesAccount      = null, //	to assign values to 'em in the implementing class...
			            attributesGroup        = null; //
	
	protected String configUserName,
					 configUserPassword,
					 configUrl;
	
	protected boolean userSupported   = false,
					  groupsSupported = false;
	
	protected boolean hardcoded = false; // TODO del

	////////////////////////////////////////////////////////////////////////////
	//
	// INNER CLASSES
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * An iterator that returns copies of the maps that are returned.
	 */
	protected class CopyIterator implements Iterator<Map<String,Object>> {

		private Iterator<Map<String,Object>> it;

		public CopyIterator(Iterator<Map<String,Object>> it) {
			this.it = it;
		}

		public boolean hasNext() {
			return this.it.hasNext();
		}

		public Map<String,Object> next() {
			return copy(this.it.next());
		}

		@Override
		public void remove() {
			this.it.remove();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CONSTRUCTORS
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public AbstractRestConnectorV2() {
		super();
		setAttributesAndFunctions();
	}

	/**
	 * Hardcoded debug constructor.
	 */
	public AbstractRestConnectorV2(String configUserName, String configUserPassword, String configUrl) {
		this();

		this.configUserName = configUserName;
		this.configUserPassword = configUserPassword;
		this.configUrl = configUrl;
		
		this.hardcoded = true;


	}
	
	/**
	 * Constructor for an account CustomConnectorTest
	 * 
	 * @param  config  The ConnectorConfig to use.
	 * @param  log     The Log to use.
	 */
	public AbstractRestConnectorV2(ConnectorConfig config, Log log) {
		super(config, log);
		setAttributesAndFunctions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LIFECYCLE
	//
	////////////////////////////////////////////////////////////////////////////


	/**
	 * Initialises the connection
	 */
	@SuppressWarnings("squid:S1192") // password is not an attribute
	public void init() {
		final String funcName = "init";
		
		logEnter(funcName);

		if (!hardcoded) {
			logDebug("Initialising...");
			
			configUserName = config.getString("user");
			configUserPassword = config.getString("password");
			configUrl = config.getString("url");
		}
		
		logExit(funcName);
	}


	/**
	 * No resources to close.
	 */
	@Override
	public void close() {
		// Do nothing
	}

	@Override
	public void testConnection() {
		final String funcName = "testConnection";

		logEnter(funcName);

		HttpURLConnection conn = null;
		try {
			conn = getUsersConnection(); // if no exception -> ok
		
		} catch (Exception e) {
			String err = exceptionToString(e);

			logError(err);
			
			throw new ConnectorException(e.getMessage());
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		
		
		logExit(funcName);
	}

	/**
	 * Support all of the features for all supports object types.
	 */
	@Override
	public abstract List<Feature> getSupportedFeatures(String objectType);

	////////////////////////////////////////////////////////////////////////////
	//
	// BASIC CRUD
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Return the Map that has the objects for the currently configured object
	 * type.  This maps native identifier to the resource object with that
	 * identifier.
	 */
	public Map<String,Map<String,Object>> getObjectsMap() {
		final String funcName = "getObjectsMapAbstract";
					 
		logEnter(funcName);

		Map<String, Map<String, Object>> res;
		
		
		try {
			if (OBJECT_TYPE_ACCOUNT.equals(this.objectType)) {
				checkIsNull(attributesAccount, "ATTRIBUTES_ACCOUNT");

				JSONArray jsArray = getJsonUserDataAsJSONArray();

				res = createObjectsFromJSONArray(jsArray, attributesAccount);
				
			} else if (OBJECT_TYPE_GROUP.equals(this.objectType)) {
				checkIsNull(attributesGroup, "ATTRIBUTES_GROUP");
				
				JSONArray jsArray = getJsonGroupDataAsJSONArray();

				res = createObjectsFromJSONArray(jsArray, attributesGroup);	

			} else
				throw new ConnectorException(ERROR_MSG_UNKNOWN_OBJECT + this.objectType);
		} catch (Exception e) {
			String err = exceptionToString(e);

			logError(err);

			throw new ConnectorException(e.getMessage(), e);
		}
		
		logExit(funcName);
		return res;	
	}
	

	/* (non-Javadoc)
	 * @see openconnector.Connector#create
	 */
	@Override
	public Result create(String nativeIdentifier, List<Item> items) {
	
		final String funcName = "create";
		
		logEnter(funcName);
		logDebug("ID::" + nativeIdentifier);
		
		Result result = new Result(Result.Status.Committed);
		
		if (read(nativeIdentifier) != null)
			throw new ObjectAlreadyExistsException(nativeIdentifier);

		Map<String,Object> object = new HashMap<>();

		object.put(getIdentityAttribute(), nativeIdentifier);
		if (items != null) {
			for (Item item : items)
				object.put(item.getName(), item.getValue());
		}

		try {
			createUser(object);
		} catch (Exception e) {
			throw new ConnectorException(e);
		}
		
		result.setObject(object);
		
		
		logExit(funcName);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#read(java.lang.String)
	 */
	public Map<String,Object> read(String nativeIdentifier) {
		return read(nativeIdentifier, false);
	}

	/**
	 * Return a copy of the requested object (or the actual object if forUpdate
	 * is true).
	 */
	protected Map<String,Object> read(String nativeIdentifier, boolean forUpdate) {

		final String funcName = "read";
		
		logEnter(funcName);
		
		logDebug("Native ID: " + nativeIdentifier);

        if (null == nativeIdentifier) {
            throw new IllegalArgumentException("nativeIdentitifier is required");
        }
        
        @SuppressWarnings("squid:S2259") // obj can be null, I don't mind
        Map<String,Object> obj;
        
        try {
        	obj = getUser(nativeIdentifier);
        } catch (Exception e) {
			obj = null;
			logError(e);
		}
        // If we're not updating, create a copy so the cache won't get corrupted.
        
        logExit(funcName);
        
        return (forUpdate) ? obj : copy(obj);
	}

	/**
	 * Create a deep clone of the given map.
	 */
	protected Map<String,Object> copy(Map<String,Object> obj) {
		// Should do a deeper clone here.
		//TODO yeah, we probably should....
		//TODO should we? -> consult with Steffen/Flo
		return (null != obj) ? new HashMap<>(obj) : null;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#iterate(openconnector.Filter)
	 */
	public Iterator<Map<String,Object>> iterate(Filter filter) {
		// Return the iterator on a copy of the list to avoid concurrent modification
		// exceptions if entries are added/removed while iterating.
		Iterator<Map<String,Object>> it =
				new ArrayList<Map<String,Object>>(getObjectsMap().values()).iterator();

		// Note: FilteredIterator should not be used for most connectors.
		// Instead, the filter should be converted to something that can be
		// used to filter results natively (eg. - an LDAP search filter, etc...)
		// Wrap this in a CopyIterator so the cache won't get corrupted.
		return new CopyIterator(new FilteredIterator(it, filter));
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#update
	 */
	@Override
	@SuppressWarnings("squid:S1199")
	public Result update(String nativeIdentifier, List<Item> items) {
		final String funcName = "update";
		
		Result result;
		
		logEnter(funcName);
		logDebug("Native ID: " + nativeIdentifier);
		
		try {
			logDebug("Items: " + items.toString());
	
			result = new Result(Result.Status.Committed);
	
			Map<String,Object> existing = read(nativeIdentifier, true);
	
			if (null == existing) {
				throw new ObjectNotFoundException(nativeIdentifier);
			}
	
			logDebug("Existing object: " + mapToJsonObject(existing, getAttrPassword()));
	
			if (items != null) {
				for (Item item : items) { // Iterate over all the items and execute corresponding operations
	
	                String         name  = item.getName();
	                Object         value = item.getValue();
	                Item.Operation op    = item.getOperation();

	                if (!name.contains(getAttrPassword()))
	                	logDebug("Item name: " + name + " | Value: " + value + " | OP: " + op.toString());
	                else
						logDebug("Item name: " + name + " | Value: <password> | OP: " + op.toString());

					switch (op) {
					case Add: {
						List<Object> currentList = getAsList(existing.get(name));
						List<Object> values = getAsList(value);
						currentList.addAll(values);
						existing.put(name, currentList);
					} break;
	
					case Remove: {
						List<Object> currentList = getAsList(existing.get(name));
						List<Object> values = getAsList(value);
						currentList.removeAll(values);
						if (currentList.isEmpty())
							existing.remove(name);
						else
							existing.put(name, currentList);
					} break;
	
					case Set: {
						existing.put(name, value);
					} break;
	
					default:
						throw new IllegalArgumentException("Unknown operation: " + op);
					}
				}
				
	
	
				logDebug(mapToJsonObject(existing, getAttrPassword()));
				updateUser(nativeIdentifier, existing);
				
			}
		} catch (Exception e) {
			logError(e);
			throw new ConnectorException(e);
		}
		logExit(funcName);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#delete(java.lang.String)
	 */
	@Override
	public Result delete(String nativeIdentifier, Map<String,Object> options) {
		Result result = new Result(Result.Status.Committed);
		
		try {
			deleteUser(nativeIdentifier);
		} catch (Exception e) {
			result.setStatus(Result.Status.Failed);
			logger.error(exceptionToString(e));
		}
		
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EXTENDED OPERATIONS
	//
	////////////////////////////////////////////////////////////////////////////
	
	/* (non-Javadoc)
	 * @see openconnector.Connector#enable(java.lang.String)
	 */
	@Override
	public Result enable(String nativeIdentifier, Map<String,Object> options) {
		methodUnsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#disable(java.lang.String)
	 */
	@Override
	public Result disable(String nativeIdentifier, Map<String,Object> options) {
		methodUnsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#unlock(java.lang.String)
	 */
	@Override
	@SuppressWarnings("squid:S4144") // the functionality is the same as delete() but one method should not call another one
	public Result unlock(String nativeIdentifier, Map<String,Object> options) {
		methodUnsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#setPassword(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public Result setPassword(String nativeIdentifier, String newPassword,
                              String currentPassword, Date expiration,
                              Map<String,Object> options) {
		final String methodName = "setPassword";
		
		logEnter(methodName);
				
		checkIsNull(attrPassword, "ATTR_PASSWORD");
		
		Result result = new Result(Result.Status.Committed);

		Map<String,Object> obj = read(nativeIdentifier, true);
		if (null == obj) {
			throw new ObjectNotFoundException(nativeIdentifier);
		}
		
		if (currentPassword != null && !currentPassword.equals(newPassword)) // TODO is this right?
				throw new ConnectorException("Passwords don't match!");
		
		Map<String,Object> passMap = new HashMap<>(); 
		passMap.put(getIdentityAttribute(), nativeIdentifier);
		passMap.put(attrPassword, newPassword);

		try {
			updateSetPassword(nativeIdentifier, passMap);
		} catch (Exception e) {
			result.setStatus(Result.Status.Failed);
			throw new ConnectorException(e);
		}
		
		logExit(methodName);
		
		
		result.setObject(obj);
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADDITIONAL FEATURES
	//
	////////////////////////////////////////////////////////////////////////////
	
	/* (non-Javadoc)
	 * @see openconnector.Connector#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings("squid:S4144") // the functionality is the same as delete() but one method should not call another one
	public Map<String,Object> authenticate(String identity, String password) {
		methodUnsupported();
		return null;
	}

	/* (non-Javadoc)
	 * @see openconnector.Connector#discoverSchema()
	 */
	@Override
	public Schema discoverSchema() {
		if (attributesAccount == null)
			throw new NullPointerException("Attributes was not set!");
		
		String funcName = "discoverSchema";
		
		logEnter(funcName);
		
		Schema schema = new Schema();

		if (OBJECT_TYPE_ACCOUNT.equals(this.objectType) && userSupported) {
			checkIsNull(attributesAccount, "ATTRIBUTES_ACCOUNT");
			for (String s : attributesAccount)
				schema.addAttribute(s);
		} else if (OBJECT_TYPE_GROUP.equals(this.objectType) && groupsSupported) {
			checkIsNull(attributesGroup, "ATTRIBUTES_GROUP");
			for (String s : attributesGroup)
				schema.addAttribute(s);
		} else
			throw new ConnectorException(ERROR_MSG_UNKNOWN_OBJECT + this.objectType);
		

		logExit(funcName);
		
		return schema;
	}


	/**
	 * <h3>HTTP status codes: 2xx Success </h3>
	 * This class of status codes indicates the action requested by the client was received, understood and accepted.
	 * @param code - code value
	 * @return code >= 200 && code < 300
	 */
	protected static boolean httpStatusCodeOk(int code) {
		return code >= 200 && code < 300;
	}
	
	protected void checkConnection(HttpURLConnection conn) throws IOException {
		checkConnection(conn, true);
	}

	/**
	 * Checks the connection status and throws an exception if needed
	 * @param conn - connection to check
	 * @throws IOException
	 */
	protected void checkConnection(HttpURLConnection conn, boolean toThrow) throws IOException {
		checkConnection(conn, null, toThrow);
	}

	/**
	 * Checks the connection status and throws an exception if needed
	 * @param conn - connection to check
	 * @throws IOException
	 */
	protected void checkConnection(HttpURLConnection conn, String output, boolean toThrow) throws IOException {
		int code = conn.getResponseCode();
		logDebug("Connection code: " + code);
		
		if (!httpStatusCodeOk(code)) {
			String errorMsg = connectionToString(conn);
			try {
				errorMsg = new JSONObject(errorMsg).getString("message");
			} catch (Exception e) {
				// use raw connection output
			}
			
			errorMsg = "Action failed: HTTP error code : " + conn.getResponseCode() + "    Error msg: " + errorMsg;
			
			logError(errorMsg);
			
			if (toThrow)
				throw new ConnectorException(errorMsg);
		}
	}

	public static String strip(String s) {
		if (s == null)
			s = "";
		s = s.replace("\'", "");
		s = s.replace("[", "");
		s = s.replace("]", "");
		return s;
	}
	
	protected static String getBasicAuthenticationEncoding(String username, String password) {
		String userpass = username + ":" + password;
		return "Basic " + new String(new Base64().encode(userpass.getBytes()));
	}

	public HttpURLConnection getConnection(String path) throws IOException {
		init();
		return getConnection(new URL(configUrl + path));
	}
	
	public HttpURLConnection getConnection(URL url) throws IOException {
		final String funcName = "getConnection";


		logEnter(funcName);
		
		// Initialize
		init();
		
		// Create URL
		logDebug("Currently used url: " + url);
		
		// Create connection from url
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		
		// Setting header for authorization in base64 by user:password
		conn.setRequestProperty("Authorization", getBasicAuthenticationEncoding(configUserName, configUserPassword));
		
		
		logExit(funcName);
		
		return conn;
	}
	
	/**
	 * Use POST for creating and PUT for updating
	 * 
	 * @param requestMethod either POST or PUT in Jira's case
	 * @param path path to the resource
	 * @param payload data to transfer; pass null to not send this data
	 */
	protected void executeUpdateWithPayload(String requestMethod, String path, byte[] payload) throws IOException {
		HttpURLConnection conn = getConnection(path);
		
		conn.setRequestMethod(requestMethod);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setUseCaches(false);
		conn.setDoOutput(true);
		conn.setDoInput(true); // TODO false
		
		conn.setRequestProperty("Authorization", 
				getBasicAuthenticationEncoding(configUserName, configUserPassword)
				);
		
		conn.connect();

		if (payload != null) {
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	        wr.write(payload);
	        wr.flush();
			wr.close();
		}

		String output = connectionToString(conn);

        checkConnection(conn, output, true);

		logDebug("Returned object: " + output);
		
        conn.disconnect();
	}

	protected static String exceptionToString(Exception e) {
		StringBuilder err = new StringBuilder();
		
		err.append(e.getMessage() + "\n\n");
		
		for (StackTraceElement s : e.getStackTrace()) {
				err.append(s.toString() + '\n');
		}
		
		return err.toString();
	}
	
	
	protected HttpURLConnection getGetConnection(URL url) throws IOException {
        HttpURLConnection conn = getConnection(url);

		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		
		return conn;
	}
	
	protected HttpURLConnection getGetConnectionWithLocation(String location) throws IOException {
		init();
		return getGetConnection(new URL(configUrl + location));
	}
	
	////////////////////
	//
	// Account functions
	//
	////////////////////
	
	protected JSONArray getJsonUserDataAsJSONArray() throws IOException, JSONException {

		HttpURLConnection conn = getUsersConnection();
		
		String jsonData = connectionToString(conn);
		
		conn.disconnect();
		
		
		return new JSONArray(jsonData);
	}
	
	protected HttpURLConnection getUsersConnection() throws IOException {
		checkIsNull(getAllUserAddress, "GET_ALL_USERS_ADRESS");
		
		return getGetConnectionWithLocation(getAllUserAddress);
	}


	/**
	 * Debug function
	 * @return json data
	 * @throws IOException
	 */
	protected String getJsonDataAsString(String loc) throws IOException {
		HttpURLConnection conn = getConnection(getAllUserAddress + loc);
		
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		checkConnection(conn);
		
		String jsonData = connectionToString(conn);
		
		conn.disconnect();
		
		
		return jsonData.toString();
	}
	
	////////////////////
	//
	// Group functions
	//
	////////////////////
	
	protected HttpURLConnection getGroupsConnection() throws IOException {
		checkIsNull(getAllGroupsAddress, "GET_ALL_GROUPS_ADRESS");
		
		return getGetConnectionWithLocation(getAllGroupsAddress);
	}
	
	protected JSONArray getJsonGroupDataAsJSONArray() throws IOException, JSONException {
		HttpURLConnection conn = getGroupsConnection();

		String jsonData = connectionToString(conn);
		
		conn.disconnect();
		
		
		return new JSONArray(jsonData);
	}
	
	////////////////////
	//
	// Misc. functions
	//
	////////////////////


	protected boolean checkIfPresent(String nativeIdentifier) throws Exception {
		return null != read(nativeIdentifier);	
	}
	
	protected Map<String, Map<String, Object>> createObjectsFromJSONArray(JSONArray jsArray, String[] fields) throws JSONException, IOException {
		Map<String, Map<String, Object>> res = new HashMap<>();
		
		for (int a = 0; a < jsArray.length(); a++) {
			JSONObject jsObject = jsArray.getJSONObject(a);

			Map<String, Object> object = createAccount(jsObject, fields);
			
			String nativeIdentitifer = strip(jsObject.get(getIdentityAttribute()).toString());
			
			if (nativeIdentitifer.isEmpty())
				throw new ConnectorException("Native Identifier cannot be empty!");
				
			res.put(nativeIdentitifer, object);
		}
		
		return res;
	}
	
	protected Map<String, Object> createAccount(JSONObject jsObject, String[] fields) throws JSONException {
		Map<String, Object> account = new HashMap<>();

		for (String s : fields) {
			try {
				account.put(s, strip(jsObject.get(s).toString()));
			} catch (JSONException e) {
				logError("Field " + s + " not found! Skipping...");
			}
		}

		return account;
	}
	
	protected String connectionToString(HttpURLConnection conn) throws IOException {
		StringBuilder jsonData = new StringBuilder();
		String line;
		InputStream ioStream;

		try {
			ioStream = conn.getInputStream();
		} catch (IOException e) {
			ioStream = conn.getErrorStream();
		} catch (Exception e) {
		    ioStream = null;
        }

		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ioStream))) {
			while ((line = br.readLine()) != null) {
				jsonData.append(line + "\n");
			}
		} catch (Exception e) {
			return "Connection returned no data!";
		}
		return jsonData.toString();
	}
	
	protected Set<String> getFieldNames() throws IOException, JSONException {
		Set<String> res     = new HashSet<>();
		JSONArray   results = getJsonUserDataAsJSONArray(); // get the results
		

		if (results.length() != 0) { 
		    for (int i = 0; i < results.length(); i++) { // iterating through the results array
		        JSONObject columnsIn = results.getJSONObject(i); // get i-th object in the results array
		        
		        Iterator<?> it = columnsIn.keys();
		        
		        while (it.hasNext()) { // iterate over the objects keys
		        	String curr = it.next().toString();
		        	if (!res.contains(curr))
		        		res.add(curr);
		        }	       
		    }
		}
		
		return res;
	}
	
	@SuppressWarnings("squid:CommentedOutCodeLine")
	protected String buildQuery(String param, String val) {
		final String EMPTY_VALUE = "";
		String parameter, value = val;
		
		checkIsNull(attrPassword, "ATTR_PASSWORD");
		
		/*  Sailpoint returns "*password*" as key name and Wordpress only accepts "password"
		 *  so if param contains "password" it won't be used;
		 *  a hardcoded string ATTR_PASS will be used.
		 */
		if (param.toLowerCase().contains("password")) 
			parameter = attrPassword;
		else
			parameter = param.trim();


        // Replacements
        value = value.trim()
                .replaceAll(   "%", "%25") // MUST BE FIRST
                .replaceAll("\\s+", "%20") // Replace all whitespaces with one '%20' symbol
                .replaceAll(   "!", "%21")
                .replaceAll(   "#", "%23")
                .replaceAll( "\\$", "%24")
				.replaceAll(   "&", "%26")
				.replaceAll(   "'", "%27")
				.replaceAll( "\\(", "%28")
				.replaceAll( "\\)", "%29")
				.replaceAll( "\\*", "%2A")
				.replaceAll( "\\+", "%2B")
				.replaceAll(   ",", "%2C")
				.replaceAll( "\\/", "%2F")
				.replaceAll(   ":", "%3A")
				.replaceAll(   ";", "%3B")
				.replaceAll(   "=", "%3D")
				.replaceAll( "\\?", "%3F")
				.replaceAll( "\\[", "%5B")
				.replaceAll("\\\\", "%5C")
				.replaceAll( "\\]", "%5D");

        value = normalizeGermanToASCII(value);

        return parameter
               + '='
               + (value.length() != 0 ? value : EMPTY_VALUE)
               + '&';
	}

	protected String mapToRestQuery(Map<String, Object> map) throws ClassNotFoundException {
		return mapToRestQuery(map, (String[]) null);
	}
	
	protected String mapToRestQuery(Map<String, Object> map, String... keysToIgnore) throws ClassNotFoundException {
		StringBuilder res = new StringBuilder();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String valKey = entry.getKey(),
				   valOut = "";
            Object val = entry.getValue();

            if (val != null
                && valKey != null
                && !stringArrayContains(keysToIgnore, valKey)) {

				// Check whether val is of type List or String. If not throw an exception
				if (val instanceof List) {
					List<?> list = (List<?>) val;

					if (list.size() > 1)
						throw new ConnectorException("A key cannot have more than one value!");

					try {
						valOut = list.get(0).toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						// Empty array -> leave valOut empty
					}
				} else if (val instanceof String) {
					valOut = (String) val;
				} else
					valOut = val.toString();

				res.append(buildQuery(entry.getKey(), valOut));
			}
		}

		return res.toString();
	}
	
	protected static boolean stringArrayContains(String[] array, String string) {
		if (array == null || string == null)
			return false;
		
		for (String s : array)
			if (string.equals(s))
				return true;
		return false;
	}
	
	protected String mapToJsonObject(Map<String, Object> map) {
		return mapToJsonObject(map, (String[]) null);
	}

	protected String mapToJsonObject(Map<String, Object> map, String... keysToIgnore) {
		checkIsNull(attrPassword, "ATTR_PASSWORD");
		
		final String funcName = "mapToJsonObject";
		
		logEnter(funcName);

		StringBuilder res = new StringBuilder();
		
		res.append("{");
		
		for (Map.Entry<String, Object> entry : map.entrySet()) {

			String valOut = "",
					valKey = entry.getKey();
            Object val = entry.getValue();

            if (val != null
                && valKey != null
                && !stringArrayContains(keysToIgnore, valKey)) {

				boolean isList = false;

                /*  Sailpoint returns "*password*" as key name and Wordpress only accepts "password"
                 *  so if param contains "password" it won't be used;
                 *  ATTR_PASS will be used.
                 */
                if (valKey.toLowerCase().contains("password"))
                    valKey = attrPassword;
                else
                    valKey = valKey.trim();

                // Check whether val is of type List or String. If not throw an exception
                if (val instanceof List) {
                    List<?> list = (List<?>) val;

                    if (list.size() > 1) {
                        StringBuilder builder = new StringBuilder("[");
                        isList = true;

                        for (Object o : list) {
                            builder.append("\"" + o.toString() + "\",");
                        }

                        builder.replace(builder.length() - 1, builder.length(), "]");
                        valOut = builder.toString();
                    } else {
                        try {
                            valOut = list.get(0).toString();
                        } catch (IndexOutOfBoundsException e) {
                            // Empty array -> leave valOut empty
                        }
                    }
                } else
                    valOut = val.toString();

                if (!isList)
                    valOut = "\"" + valOut + "\"";

                res.append("\"" + valKey + "\": "
                        + valOut + ",");
            }

		}

		res.replace(res.length()-1, res.length(), "}");
		
		logExit(funcName);
		
		return res.toString();
	}
	/**
	 * @deprecated Not working, so why not. Maybe someday...
	 * 
	 * @return method name
	 */
	@Deprecated
	protected static String getMethodName() {
		//TODO make it work
		
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		StringBuilder err = new StringBuilder();
		
		for (StackTraceElement s : ste) {
				err.append(s.toString() + '\n');
		}
		
		return ste[ste.length - 3].getMethodName();
	}
	
	protected void logDebug(Object msg) {
		logger.debug(msg.toString());
	}

	protected void logError(Object err) {
		logger.error(err.toString());
	}

	protected void logError(Object err, Throwable cause) {
        logger.error(err.toString(), cause);
    }

    protected void logError(Throwable exception) {
        logger.error(exception.getMessage(), exception);
    }
	
	protected void logMethod(String msg, String name) {
		logDebug("" + msg + name + "()");
	}
	
	protected void logConnectionOutput(HttpURLConnection conn) {
		StringBuilder out = new StringBuilder();

		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			for (int c; (c = in.read()) != -1; out.append((char)c));
		} catch (IOException e) {
			throw new ConnectorException(e);
		}
		
		logDebug(out);
	}
	
	protected void logEnter(String name) {
		logMethod("Entering ", name);
	}
	
	protected void logExit(String name) {
		logMethod("Exiting ", name);
	}
	
	protected static String normalizeGermanToASCII(String s)	{
		// This method probably should use StringBuilder instead

		s = s.replace("ö", "%C3%B6");
		s = s.replace("Ö", "%C3%96");
		
		s = s.replace("ä", "%C3%A4");
		s = s.replace("Ä", "%C3%84");
		
		s = s.replace("ü", "%C3%BC");
		s = s.replace("Ü", "%C3%9C");
		
		s = s.replace("ß", "%C3%9F");
		
		
		// replace all non-ASCII characters with similar ASCII characters
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll( "[\\p{M}]", "" ); 
		
		return s;
	}
	
	protected void methodUnsupported() {
		throw new UnsupportedOperationException("Method not implemented!");
	}
	

	protected void notImplemented() {
		throw new RuntimeException("Not yet implemented!");
	}
	
	protected void checkIsNull(Object o, String objectName) {
		if (o == null)
			throw new ConnectorException(objectName + " was not set!");
	}

    ////////////////////////////////////////////////////////////////////////////
    //
    // DEBUG METHODS
    //
    ////////////////////////////////////////////////////////////////////////////

    public String itemsToString(List<Item> itemList) {
	    StringBuilder res = new StringBuilder();

        res.append(itemList.getClass().getName() + '@' + Integer.toHexString(itemList.hashCode()) + "\n");

	    for (Item i : itemList)
	        res.append("\t"
                       + String.format("%7s", i.getOperation())
                       + " | "
                       + i.getName()
                       + "=>"
                       + i.getValue()
                       + "\n");

	    return res.toString();
    }

	////////////////////////////////////////////////////////////////////////////
	//
	// METHODS THAT SHOULD BE OVERRIDDEN
	//
	////////////////////////////////////////////////////////////////////////////
	
	protected Map<String, Object> getUser(String nativeIdentifier) throws IOException, JSONException {
		return getObjectsMap().get(nativeIdentifier);
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// ABSTRACT METHODS
	//
	////////////////////////////////////////////////////////////////////////////

	protected abstract void setAttributesAndFunctions();

	protected abstract void createUser(Map<String, Object> user) throws ClassNotFoundException, IOException, JSONException;

	protected abstract void updateUser(String id, Map<String, Object> user) throws ClassNotFoundException, IOException;

	protected abstract void updateSetPassword(String id, Map<String, Object> user) throws ClassNotFoundException, IOException;

	protected abstract void deleteUser(String id) throws ClassNotFoundException, IOException;


	////////////////////////////////////////////////////////////////////////////
	//
	// INITIALIZE METHODS
	//
	////////////////////////////////////////////////////////////////////////////
	
	protected void supportAccounts() {
		this.userSupported = true;
	}
	
	protected void supportGroups() {
		this.groupsSupported = true;
	}
	
	protected void setLogger(String logger) {
		this.logger = Logger.getLogger(logger);
	}
	
	protected void setPasswordAttribute(String ATTR_PASSWORD) {
		attrPassword = ATTR_PASSWORD;
	}

	protected String getAttrPassword() {
		return attrPassword;
	}

	protected void setGetAllUserAddress(String getAllUserAddress) {
		this.getAllUserAddress = getAllUserAddress;
	}
	
	protected void setGetAllGroupsAddress(String getAllGroupsAddress) {
		this.getAllGroupsAddress = getAllGroupsAddress;
	}
	
	protected void setAttributesAccount(String[] ATTRIBUTES) {
		attributesAccount = ATTRIBUTES;
	}
	
	protected void setAttributesGroup(String[] ATTRIBUTES) {
		attributesGroup = ATTRIBUTES;
	}

	protected void setCreateUserAddress(String address) {
		createUserAddress = address;
	}

	protected void setUpdateUserAddress(String address) {
		updateUserAddress = address;
	}

	protected void setDeleteUserAddress(String address) {
		deleteUserAddress = address;
	}

	protected void setPasswordUserAddress(String address) {
		passwordUserAddress = address;
	}

	protected void setEnableUserAddress(String address) {
		enableUserAddress = address;
	}


	////////////////////////////////////////////////////////////////////////////
	//
	// GETTERS
	//
	////////////////////////////////////////////////////////////////////////////

	private String fallbackToCreateUserAddress(String requested) {
		logger.warn(requested + " was not set! Checking createUserAddress...");
		String address = getCreateUserAddress();
		logger.warn("createUserAddress is set! Falling back to it.");

		return address;
	}

	public String getGetAllUserAddress() {
		if (getAllUserAddress == null)
			throw new NullPointerException();

		return getAllUserAddress;
	}

	public String getCreateUserAddress() {
		if (createUserAddress == null)
			throw new NullPointerException("createUserAddress is null!");

		return createUserAddress;
	}

	public String getUpdateUserAddress() {
		if (updateUserAddress == null)
			return fallbackToCreateUserAddress("updateUserAddress");

		return updateUserAddress;
	}

	public String getDeleteUserAddress() {
		if (deleteUserAddress == null)
			return fallbackToCreateUserAddress("deleteUserAddress");

		return deleteUserAddress;
	}

	public String getPasswordUserAddress() {
		if (passwordUserAddress == null)
			return fallbackToCreateUserAddress("passwordUserAddress");

		return passwordUserAddress;
	}

	public String getEnableUserAddress() {
		if (enableUserAddress == null)
			return fallbackToCreateUserAddress("enableUserAddress");

		return enableUserAddress;
	}

	public String getGetAllGroupsAddress() {
		if (getAllGroupsAddress == null)
			throw new NullPointerException();

		return getAllGroupsAddress;
	}


	public Logger getLogger() {
		return logger;
	}
}




















