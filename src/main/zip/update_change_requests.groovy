import com.urbancode.air.AirPluginTool;
import com.serena.air.plugin.ssm.SerenaSSMHelper;
import com.serena.air.plugin.ssm.FailMode;

final def apTool = new AirPluginTool(args[0], args[1])
final def props = apTool.getStepProperties()
final def helper = new SerenaSSMHelper(apTool)

def failMode = FailMode.valueOf(props['failMode'])
def changeIds = props['changeIds'].split(',') as List
def transitionName = props['transitionName']
def notes = props['notes']
def repositoryUrl = props['repositoryUrl']
def requestId = props['requestId']

def updateCount = 0;
for (def changeId : changeIds.sort()) {
    if (helper.changeRequestExists(changeId)) {
        println "Found Change Request \"${changeId}\"."
        def itemTableItemId = helper.changeRequestId(changeId)
        def updateItemRequest = """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:sbmappservices72">
   <soapenv:Header/>
   <soapenv:Body>
      <urn:TransitionItem>
         <urn:auth>
            <urn:userId>${helper.getUsername()}</urn:userId>
            <urn:password>${helper.getPassword()}</urn:password>
            <urn:hostname>${helper.getHostname()}</urn:hostname>
            <urn:loginAsUserId></urn:loginAsUserId>
         </urn:auth>
         <urn:item>
            <urn:id>
               <urn:tableIdItemId>${itemTableItemId}</urn:tableIdItemId>
            </urn:id>

            <urn:state>
               <urn:displayName></urn:displayName>
               <urn:id></urn:id>
               <urn:uuid></urn:uuid>
               <urn:internalName></urn:internalName>
               <urn:isClosed></urn:isClosed>
            </urn:state>
"""

// additional fields
        if (props['additionalFields']) {
            props['additionalFields'].split('\\n').collect {
                def (fldName, fldVal) = it.tokenize('=');
                updateItemRequest += """
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

        updateItemRequest += """
            <urn:note>
               <urn:title>Serena Deployment Automation</urn:title>
               <urn:note>${notes}</urn:note>
               <urn:modificationDateTime></urn:modificationDateTime>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:note>

            <urn:urlAttachment>
               <urn:id></urn:id>
               <urn:name>Deployment Request</urn:name>
               <urn:url>${repositoryUrl}/app#/application-process-request/${requestId}/log</urn:url>
               <urn:showAsImage></urn:showAsImage>
               <urn:modificationDateTime></urn:modificationDateTime>
               <urn:accessType>ATTACHACCESS-DEFAULT</urn:accessType>
            </urn:urlAttachment>
         </urn:item>

         <urn:transition>
            <urn:displayName></urn:displayName>
            <urn:id></urn:id>
            <urn:uuid></urn:uuid>
            <urn:internalName>${transitionName}</urn:internalName>
         </urn:transition>
         <urn:breakLock>true</urn:breakLock>

         <urn:options>
            <urn:specifiedSections></urn:specifiedSections>
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

        def updateItemResponse = helper.executeHttpRequest(updateItemRequest, 200)
        def itemName = updateItemResponse.TransitionItemResponse.return.item.id.displayName
        def itemTableId = updateItemResponse.TransitionItemResponse.return.item.id.tableId
        def itemId = updateItemResponse.TransitionItemResponse.return.item.id.id
        def itemState = updateItemResponse.TransitionItemResponse.return.item.state.displayName
        def itemUrl = helper.getServerURL() + "/workcenter/tmtrack.dll?shell=swc&IssuePage&TableId=${itemTableId}&RecordId=${itemId}&Template=view"
        println "Successfully updated Change Request ${itemName} in/to \"${itemState}\"."
        println "For more information see: ${itemUrl}"

        updateCount++;
    } else {
        if (failMode == FailMode.FAIL_FAST) {
            helper.exitFailure("Change Request \"${changeId}\" not found.");
        } else {
            println "Could not find Change Request \"${changeId}\".";
        }
    }
}
def totalNum = changeIds.size();
if (failMode == FailMode.FAIL_ON_NO_UPDATES) {
    if (!changeIds) {
        helper.exitFailure("No Change Requests found.");
    }
}
if (failMode == FailMode.FAIL_ON_ANY_FAILURE) {
    if (!changeIds || (updateCount != totalNum)) {
        helper.exitFailure("Only updated ${updateCount} out of ${totalNum} Change Requests.");
    }
}
println "Updated ${updateCount} out of ${totalNum} Change Requests.";

System.exit(0);
