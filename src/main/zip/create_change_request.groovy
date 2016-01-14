import com.urbancode.air.AirPluginTool;
import com.serena.air.plugin.ssm.SerenaSSMHelper;

final def apTool = new AirPluginTool(args[0], args[1])
final def props = apTool.getStepProperties()
final def helper = new SerenaSSMHelper(apTool)

def project = props['project']
def title = props['title']
def description = props['description']
def changeReason = props['changeReason']
def crType = props['crType']
def crCategory = props['crCategory']
def crImpact = props['crImpact']
def crUrgency = props['crUrgency']
def crPriority = props['crPriority']
def notes = props['notes']
def configItem = props['configItem']
def repositoryUrl = props['repositoryUrl']
def requestId = props['requestId']

def createItemRequest = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:CreatePrimaryItem>
         <urn:auth>
            <urn:userId>${helper.getUsername()}</urn:userId>
            <urn:password>${helper.getPassword()}</urn:password>
            <urn:hostname>${helper.getHostname()}</urn:hostname>
            <urn:loginAsUserId></urn:loginAsUserId>
         </urn:auth>
         <urn:project>
            <urn:fullyQualifiedName>${project}</urn:fullyQualifiedName>
         </urn:project>
         <urn:item>
            <urn:itemType>${crType}</urn:itemType>
            <urn:title>${title}</urn:title>
            <urn:description>${description}</urn:description>

            <!-- Change Category -->
            <urn:extendedField>
               <urn:id>
                  <urn:displayName>Change Category</urn:displayName>
                  <urn:dbName></urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>${crCategory}</urn:displayValue>
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
                  <urn:displayValue>${crUrgency}</urn:displayValue>
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
                  <urn:displayValue>${crImpact}</urn:displayValue>
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
                  <urn:displayValue>${crPriority}</urn:displayValue>
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
                  <urn:displayValue>${changeReason}</urn:displayValue>
               </urn:value>
            </urn:extendedField>

            <!-- Reason for Change -->
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>AFFECTED_CI</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>${configItem}</urn:displayValue>
               </urn:value>
            </urn:extendedField>
"""

// additional fields
if (props['additionalFields']) {
    props['additionalFields'].split('\n').collect {
        def (fldName, fldVal) = it.tokenize('=');
        createItemRequest += """
            <urn:extendedField>
               <urn:id>
                  <urn:dbName>${fldName}</urn:dbName>
               </urn:id>
               <urn:setValueBy>DISPLAY-VALUE</urn:setValueBy>
               <urn:setValueMethod>REPLACE-VALUES</urn:setValueMethod>
               <urn:value>
                  <urn:displayValue>${fldVal}</urn:displayValue>
               </urn:value>
            </urn:extendedField>
"""
    }
}

createItemRequest += """
            <urn:note>
               <urn:id></urn:id>
               <urn:title>Serena Deployment Automation</urn:title>
               <urn:note>${notes}</urn:note>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:note>

            <urn:urlAttachment>
               <urn:id></urn:id>
               <urn:name>Deployment Request</urn:name>
               <urn:url>${repositoryUrl}/app#/application-process-request/${requestId}/log</urn:url>
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


def createItemResponse = helper.executeHttpRequest(createItemRequest, 200)
def itemName = createItemResponse.CreatePrimaryItemResponse.return.item.id.displayName
def itemTableId = createItemResponse.CreatePrimaryItemResponse.return.item.id.tableId
def itemId = createItemResponse.CreatePrimaryItemResponse.return.item.id.id
def tableIdItemId = createItemResponse.CreatePrimaryItemResponse.return.item.id.tableIdItemId
def itemUrl = helper.getServerURL() + "/workcenter/tmtrack.dll?shell=swc&IssuePage&TableId=${itemTableId}&RecordId=${itemId}&Template=view"
println "Successfully created Change Request ${itemName}; for more information see:"
println itemUrl

apTool.setOutputProperty("changeName", itemName.toString())
apTool.setOutputProperty("changeId", tableIdItemId.toString())
apTool.setOutputProperty("changeUrl", itemUrl.toString())
apTool.setOutputProperties()

System.exit(0)

