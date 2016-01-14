package com.serena.air.plugin.ssm;

import com.urbancode.air.AirPluginTool;
import com.serena.air.plugin.ssm.FailMode;

import wslite.soap.SOAPClient
import wslite.soap.SOAPFaultException
import wslite.soap.SOAPVersion

public class SerenaSSMHelper {

    private AirPluginTool pluginTool
    private String username
    private String password
    private String serverURL
    private String hostname
    private String failMode
    private def props

    /**
     * Constructs a Serena Service Manager (SSM) Helper
     * @params pluginTool The AirPluginTool containing all step properties
     */
    public SerenaSSMHelper(AirPluginTool pluginTool) {
        this.pluginTool = pluginTool
        this.props = this.pluginTool.getStepProperties()
        if ((props['username'] == null) || (props['serverUrl'] == null))
            exitFailure("A username, password and server URL have not been provided.")
        this.username = props['username']
        this.password = props['password']
        if (props['serverUrl'].endsWith("/")) {
            this.serverURL = props['serverUrl']
        } else {
            this.serverURL = props['serverUrl'] + "/"
        }
        this.hostname = getDomainName(this.serverURL)
        if (props['failMode'])
            this.failMode = FailMode.valueOf(props['failMode'])
    }

    //
    // public methods
    //

    public getUsername() { return this.username }
    public getPassword() { return this.password }
    public getHostname() { return this.hostname }
    public getServerURL() { return this.serverURL }

    /**
     * Check if a change request exists
     * @param changeNumber The change by number to check for
     * @return true if the change exists, else false
     */
    public boolean changeRequestExists(String changeNumber) {
        def changeId = changeNumber.replaceAll("[^\\d.]", "")
        if (props['debug']) println ">>> Checking if Change Request \"${changeNumber}\" exists."
        try {
            def getRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
               <soapenv:Header/>
               <soapenv:Body>
                  <urn:GetItemsByQuery>
                     <urn:auth>
                        <urn:userId>${this.username}</urn:userId>
                        <urn:password>${this.password}</urn:password>
                        <urn:hostname>${this.hostname}</urn:hostname>
                     </urn:auth>
                     <urn:table>
                        <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
                     </urn:table>
                     <urn:queryWhereClause>TS_ISSUEID LIKE '%${changeId}%'</urn:queryWhereClause>
                     <urn:options>
                        <urn:sections>SECTIONS-NONE</urn:sections>
                     </urn:options>
                  </urn:GetItemsByQuery>
               </soapenv:Body>
            </soapenv:Envelope>"""
            def getResponse = executeHttpRequest(getRequest, 200)
            if (getResponse.GetItemsByQueryResponse.return.totalCount.toInteger() > 0) {
                if (props['debug']) println ">>> Found Change Request \"${changeNumber}\"."
                return true
            } else {
                println "The change request \"${changeNumber}\" does not exist, or is not visible to the user."
                return false
            }    // } else {
        } catch (Exception ex) {
            println "The Change Request \"${changeNumber}\" does not exist, or is not visible to the user."
            return false
        }
    }

    /**
     * Get the state of a change request
     * @param changeNumber The change by number to check for
     * @return the state of the change if it exists, else -1
     */
    public String changeRequestState(String changeNumber) {
        def changeId = changeNumber.replaceAll("[^\\d.]", "")
        if (props['debug']) println ">>> Checking if Change Request \"${changeNumber}\" exists."
        try {
            def getRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
               <soapenv:Header/>
               <soapenv:Body>
                  <urn:GetItemsByQuery>
                     <urn:auth>
                        <urn:userId>${this.username}</urn:userId>
                        <urn:password>${this.password}</urn:password>
                        <urn:hostname>${this.hostname}</urn:hostname>
                     </urn:auth>
                     <urn:table>
                        <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
                     </urn:table>
                     <urn:queryWhereClause>TS_ISSUEID LIKE '%${changeId}%'</urn:queryWhereClause>
                     <urn:options>
                        <urn:sections>SECTIONS-ALL</urn:sections>
                     </urn:options>
                  </urn:GetItemsByQuery>
               </soapenv:Body>
            </soapenv:Envelope>"""
            def getResponse = executeHttpRequest(getRequest, 200)
            if (getResponse.GetItemsByQueryResponse.return.totalCount.toInteger() > 0) {
                def state = getResponse.GetItemsByQueryResponse.return.item.state.displayName
                if (props['debug']) println ">>> Found Change Request \"${changeNumber}\" with state ${state}."
                return state
            } else {
                println("The Change Request \"${changeNumber}\" does not exist, or is not visible to the user.")
                return -1
            }
        } catch (Exception ex) {
            println("The Change Request \"${changeNumber}\" does not exist, or is not visible to the user.")
            return -1
        }
    }

    /**
     * Get the internal Table Id, Item Id of a change request
     * @param changeNumber The change by number to check for
     * @return the TableId:ItemId if it exists, else -1
     */
    public String changeRequestId(String changeNumber) {
        def changeId = changeNumber.replaceAll("[^\\d.]", "")
        if (props['debug']) println ">>> Retrieving Change Request \"${changeNumber}\" id."
        try {
            def getRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
               <soapenv:Header/>
               <soapenv:Body>
                  <urn:GetItemsByQuery>
                     <urn:auth>
                        <urn:userId>${this.username}</urn:userId>
                        <urn:password>${this.password}</urn:password>
                        <urn:hostname>${this.hostname}</urn:hostname>
                     </urn:auth>
                     <urn:table>
                        <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
                     </urn:table>
                     <urn:queryWhereClause>TS_ISSUEID LIKE '%${changeId}%'</urn:queryWhereClause>
                     <urn:options>
                        <urn:sections>SECTIONS-ALL</urn:sections>
                     </urn:options>
                  </urn:GetItemsByQuery>
               </soapenv:Body>
            </soapenv:Envelope>"""
            def getResponse = executeHttpRequest(getRequest, 200)
            if (getResponse.GetItemsByQueryResponse.return.totalCount.toInteger() > 0) {
                def tableItemId = getResponse.GetItemsByQueryResponse.return.item.id.tableIdItemId
                if (props['debug']) println ">>> Found Change Request \"${changeNumber}\" with id ${tableItemId}."
                return tableItemId
            } else {
                println("The Change Request \"${changeNumber}\" does not exist, or is not visible to the user.")
                return -1
            }
        } catch (Exception ex) {
            println(ex.getLocalizedMessage())
            return -1
        }
    }

    /**
     * Get the implementation dates of a change request and check the current date is in range
     * @param changeNumber The change by number to check for
     * @return true if the current date is in range else false
     */
    public boolean checkChangeRequestDates(String changeNumber) {
        def changeId = changeNumber.replaceAll("[^\\d.]", "")
        if (props['debug']) println ">>> Retrieving Change Request \"${changeNumber}\" dates."
        def startDate = new Date()
        def endDate = new Date()
        def todayDate = new Date()
        try {
            def getRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
               <soapenv:Header/>
               <soapenv:Body>
                  <urn:GetItemsByQuery>
                     <urn:auth>
                        <urn:userId>${this.username}</urn:userId>
                        <urn:password>${this.password}</urn:password>
                        <urn:hostname>${this.hostname}</urn:hostname>
                     </urn:auth>
                     <urn:table>
                        <urn:dbName>TSM_CHANGEREQUEST</urn:dbName>
                     </urn:table>
                     <urn:queryWhereClause>TS_ISSUEID LIKE '%${changeId}%'</urn:queryWhereClause>
                     <urn:options>
                        <urn:sections>SECTIONS-ALL</urn:sections>
                     </urn:options>
                  </urn:GetItemsByQuery>
               </soapenv:Body>
            </soapenv:Envelope>"""
            def getResponse = executeHttpRequest(getRequest, 200)
            if (getResponse.GetItemsByQueryResponse.return.totalCount.toInteger() > 0) {
                def state = getResponse.GetItemsByQueryResponse.return.item.state.displayName
                if (props['debug']) println ">>> Found Change Request \"${changeNumber}\" with state ${state}."

                def extendedFields = getResponse.GetItemsByQueryResponse.return.item.extendedField
                extendedFields.each { field ->
                    if (field.id.dbName == "IMPLEMENTATION_START_DATE") {
                        startDate = new Date().parse("yyyy-mm-dd", field.value.displayValue.toString())
                        if (props['debug']) println ">> Found start date: " + startDate.toString()
                    }
                    if (field.id.dbName == "IMPLEMENTATION_END_DATE") {
                        endDate = new Date().parse("yyyy-mm-dd", field.value.displayValue.toString())
                        if (props['debug']) println ">>> Found end date: " + endDate.toString()
                    }
                }

                if (props['debug']) println ">>> Comparing with today's date: " + todayDate.toString()
                if (todayDate.before(startDate)) {
                    println "Implementation start date ${startDate.toString()} has not yet been reached."
                    return false
                }
                if (todayDate.after(endDate)) {
                    println "Implementation end date ${startDate.toString()} has passed."
                    return false
                }
                if ((todayDate.after(startDate) || todayDate.equals(startDate)) && (todayDate.before(endDate) || todayDate.equals(endDate))) {
                    println "Today's date is valid date between Implementation start date ${startDate.toString()} and Implementation end date ${startDate.toString()}."
                    return true
                }
            } else {
                println("The Change Request \"${changeNumber}\" does not exist, or is not visible to the user.")
                return false
            }
        } catch (Exception ex) {
            println(ex.getLocalizedMessage())
            return false
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
        if ((body == null) || (expectedStatus == null)) exitFailure("An error occurred executing the request.")

        if (props['debug']) {
            println ">>>Sending request:"
            println body
        }

        def client = new SOAPClient(this.serverURL + "gsoap/gsoap_ssl.dll?sbmappservices72")

        def response = null
        try {
            response = client.send(SOAPVersion.V1_2,
                    connectTimeout:7000,
                    readTimeout:9000,
                    """${body}""")
        } catch (Exception sfe) {
            exitFailure(sfe.getLocalizedMessage())
        }

        if (props['debug']) {
            println ">>Received the response:"
            println response.text
        }

        if (!(response.httpResponse.getStatusCode() == expectedStatus))
            httpFailure(response)

        return response
    }

    /**
     * Write an error message to console and exit on a fail status.
     * @param message The error message to write to the console.
     */
    def exitFailure(String message) {
        println "${message}"
        System.exit(1)
    }

    /**
     * Write a HTTP error message to console and exit on a fail status.
     * @param message The error message to write to the console.
     */
    def httpFailure(response) {
        println "Request failed with error code : " + response.getStatusCode()
        String responseString = response.getStatusMessage()
        println "${responseString}"
        System.exit(1)
    }

    //
    // private methods
    //

    private def getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

}