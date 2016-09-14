package com.stratio.specs;

import com.csvreader.CsvReader;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.ning.http.client.Response;
import com.stratio.cucumber.converter.ArrayListConverter;
import com.stratio.cucumber.converter.NullableStringConverter;
import com.stratio.tests.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.Transform;
import cucumber.api.java.en.When;
import org.apache.zookeeper.KeeperException;
import org.hjson.JsonArray;
import org.hjson.JsonValue;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.stratio.assertions.Assertions.assertThat;

public class WhenGSpec extends BaseGSpec {

    public static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Default constructor.
     *
     * @param spec
     */
    public WhenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Wait seconds.
     *
     * @param seconds
     * @throws InterruptedException
     */
    @When("^I wait '(\\d+?)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }

    /**
     * Searchs for two webelements dragging the first one to the second
     *
     * @param source
     * @param destination
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    @When("^I drag '([^:]*?):([^:]*?)' and drop it to '([^:]*?):([^:]*?)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }

    /**
     * Click on an numbered {@code url} previously found element.
     *
     * @param index
     * @throws InterruptedException
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) throws InterruptedException {

        try {
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        } catch (AssertionError e) {
            Thread.sleep(1000);
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        }
        }

    /**
     * Clear the text on a numbered {@code index} previously found element.
     *
     * @param index
     */
    @When("^I clear the content on text input at index '(\\d+?)'$")
    public void seleniumClear(Integer index) {
	assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
        	.hasAtLeast(index);

	assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).isTextField(commonspec.getTextFieldCondition());

	commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();
    }


    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     *
     * @param text
     * @param index
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumType(@Transform(NullableStringConverter.class) String text, Integer index) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
		.hasAtLeast(index);
        while (text.length() > 0) {
            if (-1 == text.indexOf("\\n")) {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text);
                text = "";
            } else {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text.substring(0, text.indexOf("\\n")));
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.ENTER);
                text = text.substring(text.indexOf("\\n") + 2);
            }
        }
    }

    /**
     * Send a {@code strokes} list on an numbered {@code url} previously found element or to the driver. strokes examples are "HOME, END"
     * or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( )
     *
     * @param strokes
     * @param foo
     * @param index
     */
    @When("^I send '(.+?)'( on the element on index '(\\d+?)')?$")
    public void seleniumKeys(@Transform(ArrayListConverter.class) List<String> strokes, String foo, Integer index) {
	if (index != null) {
	    assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
	    	.hasAtLeast(index);
	}
	assertThat(strokes).isNotEmpty();

	for (String stroke : strokes) {
	    if (stroke.contains("+")) {
		List<Keys> csl = new ArrayList<Keys>();
		for (String strokeInChord : stroke.split("\\+")) {
		    csl.add(Keys.valueOf(strokeInChord.trim()));
		}
		Keys[] csa = csl.toArray(new Keys[csl.size()]);
		if (index == null) {
		    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), csa).perform();
		} else {
		    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(csa);
		}
	    } else {
		if (index == null) {
		    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), Keys.valueOf(stroke)).perform();
		} else {
		    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.valueOf(stroke));
		}
	    }
	}
    }

    /**
     * Choose an @{code option} from a select webelement found previously
     *
     * @param option
     * @param index
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
    public void elementSelect(String option, Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        sel.selectByVisibleText(option);
    }

    /**
     * Choose no option from a select webelement found previously
     *
     * @param index
     */
    @When("^I de-select every item on the element on index '(\\d+?)'$")
    public void elementDeSelect(Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }

    /**
     * Send a request of the type specified
     *
     * @param requestType type of request to be sent. Possible values:
     * GET|DELETE|POST|PUT|CONNECT|PATCH|HEAD|OPTIONS|REQUEST|TRACE
     * @param endPoint end point to be used
     * @param foo parameter generated by cucumber because of the optional expression
     * @param baseData path to file containing the schema to be used
     * @param type element to read from file (element should contain a json)
     * @param modifications DataTable containing the modifications to be done to the
     * base schema element. Syntax will be:
     * 		| <key path> | <type of modification> | <new value> |
     * where:
     *     key path: path to the key to be modified
     *     type of modification: DELETE|ADD|UPDATE
     *     new value: in case of UPDATE or ADD, new value to be used
     * for example:
     * if the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     * and we want to modify the value in "key3" with "new value3"
     * the modification will be:
     *  	| key2.key3 | UPDATE | "new value3" |
     * being the result of the modification: {"key1": "value1", "key2": {"key3": "new value3"}}
     * @throws Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')? based on '([^:]+?)'( as '(json|string)')? with:$")
    public void sendRequest(String requestType, String endPoint, String foo, String loginInfo, String baseData, String baz, String type, DataTable modifications) throws Exception {
	// Retrieve data
	String retrievedData = commonspec.retrieveData(baseData, type);

	// Modify data
	commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
	String modifiedData;
	modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        String user = null;
        String password = null;
        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
        }


	commonspec.getLogger().debug("Generating request {} to {} with data {} as {}", requestType, endPoint, modifiedData, type);
    Future<Response> response = commonspec.generateRequest(requestType, false, user, password, endPoint, modifiedData, type, "");

	// Save response
	commonspec.getLogger().debug("Saving response");
	commonspec.setResponse(requestType, response.get());
    }

    /**
     * Same sendRequest, but in this case, we do not receive a data table with modifications.
     * Besides, the data and request header are optional as well.
     * In case we want to simulate sending a json request with empty data, we just to avoid baseData
     *
     * @param requestType
     * @param endPoint
     * @param foo
     * @param baseData
     * @param bar
     * @param type
     *
     * @throws Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')?( based on '([^:]+?)')?( as '(json|string)')?$")
    public void sendRequestNoDataTable (String requestType, String endPoint, String foo, String loginInfo, String bar, String baseData, String baz, String type) throws Exception {
	Future<Response> response;
        String user = null;
        String password = null;

        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
        }

	if (baseData != null) {
	    // Retrieve data
	    String retrievedData = commonspec.retrieveData(baseData, type);
	    // Generate request
        response = commonspec.generateRequest(requestType, false, user, password, endPoint, retrievedData, type, "");
	} else {
	    // Generate request
        response = commonspec.generateRequest(requestType, false, user, password, endPoint, "", type, "");
	}

	// Save response
	commonspec.setResponse(requestType, response.get());
    }


    /**
     * Same sendRequest, but in this case, the rersponse is checked until it contains the expected value
     *
     * @param timeout
     * @param wait
     * @param requestType
     * @param endPoint
     * @param responseVal
     * @throws Exception
     */
    @When("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, I send a '(.+?)' request to '(.+?)' so that the response contains '(.+?)'$")
    public void sendRequestTimeout (Integer timeout, Integer wait, String requestType, String endPoint, String responseVal) throws Exception {

        Boolean found = false;
        AssertionError ex = null;

        String type = "";
        Future<Response> response;
        Pattern pattern = CommonG.matchesOrContains(responseVal);

        for (int i = 0; (i <= timeout); i += wait) {
            if (found) break;
            response = commonspec.generateRequest(requestType, false, null, null, endPoint, "", type, "");
            commonspec.setResponse(requestType, response.get());
            commonspec.getLogger().debug("Checking response value");
            try {
                assertThat(commonspec.getResponse().getResponse()).containsPattern(pattern);
                found = true;
                timeout = i;
            } catch (AssertionError e) {
                commonspec.getLogger().info("Response value don't found yet after " + i + " seconds");
                Thread.sleep(wait * 1000);
                ex = e;
            }
        }
        if (!found) {
            throw (ex);
        }
        commonspec.getLogger().info("Response value found after " + timeout + " seconds");
    }

    @When("^I attempt a login to '(.+?)' based on '([^:]+?)' as '(json|string)'$")
    public void loginUser(String endPoint, String baseData, String type) throws Exception {
	sendRequestNoDataTable("POST", endPoint, null, null, null, baseData, null, type);
    }

    @When("^I attempt a login to '(.+?)' based on '([^:]+?)' as '(json|string)' with:$")
    public void loginUser(String endPoint, String baseData, String type, DataTable modifications) throws Exception {
	sendRequest("POST", endPoint, null, null, baseData, "", type, modifications);
    }

    @When("^I attempt a logout to '(.+?)'$")
    public void logoutUser(String endPoint) throws Exception {
	sendRequestNoDataTable("GET", endPoint, null, null, null, "", null, "");
    }

    /**
     * Execute a query with schema over a cluster
     *
     * @param fields columns on which the query is executed. Example: "latitude,longitude" or "*" or "count(*)"
     * @param schema the file of configuration (.conf) with the options of mappin. If schema is the word "empty", method will not add a where clause.
     * @param type type of the changes in schema (string or json)
     * @param table table for create the index
     * @param magic_column magic column where index will be saved. If you don't need index, you can add the word "empty"
     * @param keyspace keyspace used
     * @param modifications all data in "where" clause. Where schema is "empty", query has not a where clause. So it is necessary to provide an empty table. Example:  ||.
     * @throws Exception
     */
    @When("^I execute a query over fields '(.+?)' with schema '(.+?)' of type '(json|string)' with magic_column '(.+?)' from table: '(.+?)' using keyspace: '(.+?)' with:$")
    public void sendQueryOfType( String fields, String schema, String type, String magic_column, String table, String keyspace, DataTable modifications){
        try {
        commonspec.setResultsType("cassandra");
        commonspec.getCassandraClient().useKeyspace(keyspace);
        commonspec.getLogger().debug("Starting a query of type "+commonspec.getResultsType());

        String query="";

        if(schema.equals("empty") && magic_column.equals("empty")){

         query="SELECT "+fields+" FROM "+ table +";";

        }else if(!schema.equals("empty") && magic_column.equals("empty")){
            String retrievedData = commonspec.retrieveData(schema, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
            query="SELECT "+fields+" FROM "+ table +" WHERE "+modifiedData+";";


        }
        else{
            String retrievedData = commonspec.retrieveData(schema, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
            query="SELECT " + fields + " FROM "+ table +" WHERE "+ magic_column +" = '"+ modifiedData +"';";

        }
        commonspec.getLogger().debug("query: "+query);
        commonspec.setCassandraResults(commonspec.getCassandraClient().executeQuery(query));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }


    }

    /**
     * Execute a query on (mongo|elasticsearch) database
     *
     * @param query path to query
     * @param type type of data in query (string or json)
     * @param database mongo database name
     * @param collection collection in database
     * @param modifications modifications to perform in query
     */
    @When("^I execute query '(.+?)' of type '(json|string)' in '(mongo)' database '(.+?)' using collection '(.+?)' with:$")
    public void sendQueryOfType(String query, String type, String dbType,String database, String collection, DataTable modifications) throws Exception {
        try {
            commonspec.setResultsType(dbType);

            String retrievedData = commonspec.retrieveData(query, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

            switch (dbType) {
                case "mongo":
                    commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
                    DBCollection dbCollection = commonspec.getMongoDBClient().getMongoDBCollection(collection);

                    commonspec.getLogger().debug("Starting a query of type " + commonspec.getResultsType());

                    DBObject dbObject = (DBObject) JSON.parse(modifiedData);
                    commonspec.getLogger().debug("find: " + modifiedData);

                    DBCursor cursor = dbCollection.find(dbObject);
                    commonspec.setMongoResults(cursor);
                    break;
                default:
                     throw new Exception("Invalid database type: " + dbType);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Execute query with filter over elasticsearch
     * @param indexName
     * @param mappingName
     * @param columnName
     * @param filterType it could be equals, gt, gte, lt and lte.
     * @param value value of the column to be filtered.
     */
    @When("^I execute an elasticsearch query over index '(.*?)' and mapping '(.*?)' and column '(.*?)' with value '(.*?)' to '(.*?)'$")
    public void elasticSearchQueryWithFilter(String indexName, String mappingName, String
            columnName ,String filterType, String value){
        try {
            commonspec.setResultsType("elasticsearch");
            commonspec.setElasticsearchResults(
                    commonspec.getElasticSearchClient()
                            .searchSimpleFilterElasticsearchQuery(indexName, mappingName, columnName,
                                    value, filterType)
            );
        }catch(Exception e){
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }



    /**
     * Create a Cassandra index.
     *
     * @param index_name index name
     * @param schema the file of configuration (.conf) with the options of mappin
     * @param type type of the changes in schema (string or json)
     * @param table table for create the index
     * @param magic_column magic column where index will be saved
     * @param keyspace keyspace used
     * @param modifications data introduced for query fields defined on schema
     *
     *
     */
    @When("^I create a Cassandra index named '(.+?)' with schema '(.+?)' of type '(json|string)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)' with:$")
    public void createCustomMapping(String index_name, String schema, String type, String table, String magic_column, String keyspace, DataTable modifications) throws Exception {
        String retrievedData = commonspec.retrieveData(schema, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        String query="CREATE CUSTOM INDEX "+ index_name +" ON "+ keyspace +"."+ table +"("+ magic_column +") "
                + "USING 'com.stratio.cassandra.lucene.Index' WITH OPTIONS = "+ modifiedData;
        System.out.println(query);
        commonspec.getCassandraClient().executeQuery(query);
    }

    /**
     * Drop table
     *
     * @param table
     * @param keyspace
     *
     */
    @When("^I drop a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void dropTableWithData(String table, String keyspace){
        try{
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getCassandraClient().dropTable(table);
        }catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
        }

    /**
     * Truncate table
     *
     * @param table
     * @param keyspace
     *
     */
    @When("^I truncate a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void truncateTable(String table, String keyspace){
        try{
            commonspec.getCassandraClient().useKeyspace(keyspace);
            commonspec.getCassandraClient().truncateTable(table);
        }catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().debug("Exception captured");
            commonspec.getLogger().debug(e.toString());
            commonspec.getExceptions().add(e);
        }
    }

    /**
     * Read csv file and store result in list of maps
     *
     *  @param csvFile
     *
     */
    @When("^I read info from csv file '(.+?)'$")
    public void readFromCSV(String csvFile) throws Exception {
        CsvReader rows = new CsvReader(csvFile);

        String[] columns = null;
        if (rows.readRecord()) {
            columns = rows.getValues();
            rows.setHeaders(columns);
        }

        List<Map<String,String>> results = new ArrayList<Map<String,String>>();
        while (rows.readRecord()) {
            Map<String,String> row = new HashMap<String, String>();
            for (String column: columns) {
                row.put(column, rows.get(rows.getIndex(column)));
            }
            results.add(row);
        }

        rows.close();

        commonspec.setResultsType("csv");
        commonspec.setCSVResults(results);
    }


    /**
     * Change current window to another opened window.
     *
     */
    @When("^I change active window$")
    public void seleniumChangeWindow() {
        String originalWindowHandle = commonspec.getDriver().getWindowHandle();
        Set<String> windowHandles = commonspec.getDriver().getWindowHandles();

        for (String window: windowHandles) {
            if(!window.equals(originalWindowHandle)){
                commonspec.getDriver().switchTo().window(window);
            }
        }

    }

    /**
     * Sort elements in envVar by a criteria and order.
     *
     * @param envVar Environment variable to be sorted
     * @param criteria alphabetical,...
     * @param order ascending or descending
     *
     */
    @When("^I sort elements in '(.+?)' by '(.+?)' criteria in '(.+?)' order$")
    public void sortElements(String envVar, String criteria, String order) {

        String value = ThreadProperty.get(envVar);
        JsonArray jsonArr = JsonValue.readHjson(value).asArray();

        List<JsonValue> jsonValues = new ArrayList<JsonValue>();
        for (int i = 0; i < jsonArr.size(); i++) {
            jsonValues.add(jsonArr.get(i));
        }

        Comparator<JsonValue> comparator;
        switch (criteria) {
            case "alphabetical":
                commonspec.getLogger().debug("Alphabetical criteria selected.");
                comparator = new Comparator<JsonValue>() {
                    public int compare(JsonValue json1, JsonValue json2) {
                        int res = String.CASE_INSENSITIVE_ORDER.compare(json1.toString(), json2.toString());
                        if (res == 0) {
                            res = json1.toString().compareTo(json2.toString());
                        }
                        return res;
                    }
                };
                break;
            default:
                commonspec.getLogger().debug("No criteria selected.");
                comparator = null;
        }

        if ("ascending".equals(order)) {
            Collections.sort(jsonValues,comparator);
        } else {
            Collections.sort(jsonValues,comparator.reversed());
        }

        ThreadProperty.set(envVar,jsonValues.toString());
    }

    /**
     * Create a Kafka topic.
     *
     * @param topic_name topic name
     *
     */
    @When("^I create a Kafka topic named '(.+?)'")
    public void createKafkaTopic(String topic_name) throws Exception {
            commonspec.getKafkaUtils().createTopic(topic_name);
    }
    /**
     * Delete a Kafka topic.
     *
     * @param topic_name topic name
     *
     */
     @When("^I delete a Kafka topic named '(.+?)'")
        public void deleteKafkaTopic(String topic_name) throws Exception {
                commonspec.getKafkaUtils().deleteTopic(topic_name);
            }
     

    /** Delete zPath, it should be empty
     *
     *  @param zNode path at zookeeper
     *
     */
    @When("^I remove the zNode '(.+?)'$")
    public void removeZNode(String zNode) throws KeeperException, InterruptedException {
        commonspec.getZookeeperClient().delete(zNode);
    }


    /**
     * Create zPath and domcument
     *
     *  @param path path at zookeeper
     *  @param foo a dummy match group
     *  @param content if it has content it should be defined
     *  @param ephemeral if it's created as ephemeral or not
     *
     */
    @When("^I create the zNode '(.+?)'( with content '(.+?)')? which (IS|IS NOT) ephemeral$")
    public void createZNode(String path, String foo, String content, boolean ephemeral) throws KeeperException, InterruptedException {
        if(content != null){
            commonspec.getZookeeperClient().zCreate(path,content,ephemeral);
        }else{
            commonspec.getZookeeperClient().zCreate(path,ephemeral);
        }
    }

    /**
     * Modify partitions in a Kafka topic.
     *
     * @param topic_name topic name
     * @param numPartitions number of partitions
     *
     */
    @When("^I increase '(.+?)' partitions in a Kafka topic named '(.+?)'")
    public void modifyPartitions(int numPartitions, String topic_name) throws Exception {
        commonspec.getKafkaUtils().modifyTopicPartitioning(topic_name,numPartitions);
    }



    /**
     * Sending a message in a Kafka topic.
     *
     * @param topic_name topic name
     * @param message string that you send to topic
     *
     */
    @When("^I send a message '(.+?)' to the kafka topic named '(.+?)'")
    public void sendAMessage(String message, String topic_name) throws Exception {
        commonspec.getKafkaUtils().sendMessage(topic_name,message);
    }


}
