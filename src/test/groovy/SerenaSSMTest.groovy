import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import wslite.soap.*

public class TestSerenaSSMHelper {

    private String username;
    private String password;
    private String serverURL;

    public TestSerenaSSMHelper(String username, String password, String serverURL) {
        this.username = username;
        this.password = password;
        if (serverURL.endsWith("/")) {
            this.serverURL = serverURL;
        } else {
            this.serverURL = serverURL + "/";
        }
    }

    /**
     * Executes the given HTTP request and checks for a correct response status
     * @param request The HttpRequest to execute
     * @param expectedStatus The response status that indicates a successful request
     * @param body An XML String containing the request body
     * @return An Object containing the response to the HTTP request executed
     */
    def executeHttpRequest(String body, int expectedStatus) {
        // Make sure the required parameters are there
        if ((body == null) || (expectedStatus == null)) exitFailure("An error occurred executing the request.");

        println ">>>Sending request:"
        println body

        def client = new SOAPClient(this.serverURL + "/gsoap/gsoap_ssl.dll?sbmappservices72")

        def response = null
        try {
            response = client.send(SOAPVersion.V1_2,
                    connectTimeout: 7000,
                    readTimeout: 9000,
                    """${body}""")
        } catch (Exception sfe) {
            exitFailure(sfe.getLocalizedMessage())
        }

        println ">>Received the response:"
        println response.text

        if (!(response.httpResponse.getStatusCode() == expectedStatus))
            httpFailure(response)

        return response
    }

    /**
     * Write an error message to console and exit on a fail status.
     * @param message The error message to write to the console.
     */
    def exitFailure(String message) {
        println "${message}";
        System.exit(1);
    }

    /**
     * Write a HTTP error message to console and exit on a fail status.
     * @param message The error message to write to the console.
     */
    def httpFailure(response) {
        println "Request failed with error code : " + response.getStatusCode();
        String responseString = response.getStatusMessage();
        println "${responseString}";
        System.exit(1);
    }
}

def changeNumber = "STD_000575"
def changeId = changeNumber.replaceAll("[^\\d.]", "")
println changeId

TestSerenaSSMHelper helper = new TestSerenaSSMHelper("admin", "", "http://localhost:80");

def getProjectsRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:GetSubmitProjects>
         <urn:auth>
            <urn:userId>admin</urn:userId>
            <urn:password></urn:password>
            <urn:hostname>localhost</urn:hostname>
            <urn:loginAsUserId></urn:loginAsUserId>
         </urn:auth>
         <urn:table>
            <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
         </urn:table>
      </urn:GetSubmitProjects>
   </soapenv:Body>
</soapenv:Envelope>
"""

def createItemRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:CreatePrimaryItem>
         <urn:auth>
            <urn:userId>admin</urn:userId>
            <urn:password></urn:password>
            <urn:hostname>localhost</urn:hostname>
            <urn:loginAsUserId></urn:loginAsUserId>
         </urn:auth>
         <urn:project>
            <urn:fullyQualifiedName>Base Project||Change Management||Changes||Applications</urn:fullyQualifiedName>
         </urn:project>
         <urn:item>
            <urn:itemType>Standard</urn:itemType>
            <urn:title>Test Title</urn:title>
            <urn:description>Test Description</urn:description>

            <!-- Change Category -->
            <urn:extendedField>
               <urn:id>
                  <urn:displayName>Change Category</urn:displayName>
                  <urn:dbName></urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>Minor</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <!-- Urgency -->
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>URGENCY</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>Medium</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <!-- Impact -->
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>IMPACT</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>Department</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <!-- Priority -->
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>PRIORITY</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>3</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <!-- Reason for Change -->
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>REASON_FOR_CHANGE</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>The reason for change</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <urn:note>
               <urn:id></urn:id>
               <urn:title>test note</urn:title>
               <urn:note>this is a test note</urn:note>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:note>

            <urn:urlAttachment>
               <urn:id></urn:id>
               <urn:name>Test Attachement</urn:name>
               <urn:url>http://localhost:8080/</urn:url>
               <urn:showAsImage></urn:showAsImage>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:urlAttachment>

         </urn:item>
         <urn:submitTransition>
            <urn:displayName></urn:displayName>
            <urn:id></urn:id>
            <urn:uuid></urn:uuid>
            <urn:internalName></urn:internalName>
         </urn:submitTransition>
         <urn:options>
            <urn:sections></urn:sections>
            <urn:specifiedSections></urn:specifiedSections>
            <!--Zero or more repetitions:-->
            <urn:limitedField>
               <urn:displayName></urn:displayName>
               <urn:id></urn:id>
               <urn:uuid></urn:uuid>
               <urn:dbName></urn:dbName>
            </urn:limitedField>
         </urn:options>
      </urn:CreatePrimaryItem>
   </soapenv:Body>
</soapenv:Envelope>
"""

def updateItemRequest = """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:TransitionItem>
         <urn:auth>
            <urn:userId>admin</urn:userId>
            <urn:password></urn:password>
            <urn:hostname>localhost</urn:hostname>
            <urn:loginAsUserId></urn:loginAsUserId>
         </urn:auth>
         <urn:item>
            <urn:id>
               <urn:tableIdItemId>1031:27</urn:tableIdItemId>
            </urn:id>
            <urn:title>Updated title</urn:title>
            <urn:description>Updated description</urn:description>

            <urn:state>
               <urn:displayName></urn:displayName>
               <urn:id></urn:id>
               <urn:uuid></urn:uuid>
               <urn:internalName></urn:internalName>
               <urn:isClosed></urn:isClosed>
            </urn:state>

            <!--Zero or more repetitions:-->
            <urn:note>
               <urn:title>New note</urn:title>
               <urn:note>a new note</urn:note>
               <urn:modificationDateTime></urn:modificationDateTime>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:note>

            <!--Zero or more repetitions:-->
            <urn:urlAttachment>
               <urn:id></urn:id>
               <urn:name>A new attachment</urn:name>
               <urn:url>http://localhost:8080</urn:url>
               <urn:showAsImage></urn:showAsImage>
               <urn:modificationDateTime></urn:modificationDateTime>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:urlAttachment>
         </urn:item>

         <urn:transition>
            <urn:displayName></urn:displayName>
            <urn:id></urn:id>
            <urn:uuid></urn:uuid>
            <urn:internalName>CHANGE_MANAGEMENT.UPDATE1</urn:internalName>
         </urn:transition>
         <urn:breakLock>true</urn:breakLock>

         <urn:options>
            <urn:specifiedSections></urn:specifiedSections>
            <!--Zero or more repetitions:-->
            <urn:limitedField>
               <urn:displayName></urn:displayName>
               <urn:id></urn:id>
               <urn:uuid></urn:uuid>
               <urn:dbName></urn:dbName>
            </urn:limitedField>
         </urn:options>
      </urn:TransitionItem>
   </soapenv:Body>
</soapenv:Envelope>
"""

def getItemRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:GetItemsByQuery>
         <urn:auth>
            <urn:userId>admin</urn:userId>
            <urn:password></urn:password>
            <urn:hostname>localhost</urn:hostname>
         </urn:auth>
         <urn:table>
            <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
         </urn:table>
         <urn:queryWhereClause>TS_ISSUEID LIKE '%${changeId}%'</urn:queryWhereClause>
         <urn:orderByClause></urn:orderByClause>
         <urn:firstRecord></urn:firstRecord>
         <urn:maxReturnSize></urn:maxReturnSize>
         <urn:options>
            <urn:extraOption>
               <urn:name></urn:name>
               <urn:value></urn:value>
            </urn:extraOption>
            <urn:sections></urn:sections>
            <urn:specifiedSections></urn:specifiedSections>
            <!--Zero or more repetitions:-->
            <urn:limitedField>
               <urn:displayName></urn:displayName>
               <urn:id></urn:id>
               <urn:uuid></urn:uuid>
               <urn:dbName></urn:dbName>
            </urn:limitedField>
            <urn:multiOption></urn:multiOption>
         </urn:options>
      </urn:GetItemsByQuery>
   </soapenv:Body>
</soapenv:Envelope>"""

/*def getProjectsResponse = helper.executeHttpRequest(getProjectsRequest, 200)
println getProjectsResponse.text*/

/*
def updateItemResponse = helper.executeHttpRequest(updateItemRequest, 200)
println updateItemResponse.text
def itemName = updateItemResponse.TransitionItemResponse.return.item.id.displayName
def itemTableItemId = updateItemResponse.TransitionItemResponse.return.item.id.tableIdItemId
def stateName = updateItemResponse.TransitionItemResponse.return.item.state.displayName

println itemName
println itemTableItemId
println stateName */

/*def createItemResponse = helper.executeHttpRequest(createItemRequest, 200)
println createItemResponse.text
def itemName = createItemResponse.CreatePrimaryItemResponse.return.item.id.displayName
def itemTableItemId = createItemResponse.CreatePrimaryItemResponse.return.item.id.tableIdItemId

println itemName
println itemTableItemId*/

def getItemResponse = helper.executeHttpRequest(getItemRequest, 200)
println getItemResponse.text
if (getItemResponse.GetItemsByQueryResponse.return.totalCount.toInteger() > 0) {
    println "Found Item "
    println getItemResponse.text
} else {
    println "No Items Found"
}

//println getItemResponse.GetItemsByQueryResponse.return.item.extendedField

def startDate = new Date()
def endDate = new Date()
def today = new Date()

testSet = getItemResponse.GetItemsByQueryResponse.return.item.extendedField
testSet.each { field ->
    //println field.id.dbName
    if (field.id.dbName == "IMPLEMENTATION_START_DATE") {
        startDate = new Date().parse("yyyy-mm-dd", field.value.displayValue.toString())
        println startDate.toString()
    }
    if (field.id.dbName == "IMPLEMENTATION_END_DATE") {
        println field.value.displayValue
        endDate = new Date().parse("yyyy-mm-dd", field.value.displayValue.toString())
        println endDate.toString()
    }
};

println "Today is " + today.toString()
if (today.after(startDate) && today.before(endDate)) {
    println "Valid date"
}

private def getDomainName(String url) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    return domain.startsWith("www.") ? domain.substring(4) : domain;
}

println getDomainName("https://tst001-78787.sausage.com:8080/jkjk/tyt")


