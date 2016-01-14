import com.urbancode.air.AirPluginTool;
import com.serena.air.plugin.ssm.SerenaSSMHelper;
import com.serena.air.plugin.ssm.FailMode;

final def apTool = new AirPluginTool(args[0], args[1]);
final def props = apTool.getStepProperties();
final def helper = new SerenaSSMHelper(apTool);
long waitInterval = Long.parseLong(props['waitInterval'])*1000;
int maxRetryCount = Integer.parseInt(props['maxRetryCount']);

def stateNames = props['stateNames'].split(',') as List;
def failStateNames = props['failStateNames'].split(',') as List;
def failMode = FailMode.valueOf(props['failMode']);
def changeIds = props['changeIds'].split(',') as List;
def stateCount = 0;

boolean inProgress = true
boolean forever = (waitInterval == -1 ? true : false)
def totalNum = changeIds.size();
int retryCount = 0
while (inProgress) {
    for (def changeId : changeIds.sort()) {
        if (helper.changeRequestExists(changeId)) {
            def actualState = helper.changeRequestState(changeId);
            if (stateNames.contains(actualState)) {
                println "Found \"${changeId}\" with the state: ${actualState}.";
                stateCount++;
            } else if (failStateNames.contains(actualState)) {
                if (failMode != FailMode.WARN_ONLY) {
                    helper.exitFailure("Change Request \"${changeId}\" has failed state: ${actualState}.");
                }
            } else {
                if (failMode == FailMode.FAIL_FAST) {
                    helper.exitFailure("Change Request \"${changeId}\" has an different state: ${actualState}.");
                } else {
                    println "Change Request \"${changeId}\" has an different state: ${actualState}.";
                }
            }
        } else {
            if (failMode == FailMode.FAIL_FAST) {
                helper.exitFailure("Error: Change Request \"${changeId}\" not found.");
            } else {
                println "Could not find Change Request \"${changeId}\".";
            }
        }
    }

    if (failMode == FailMode.FAIL_ON_NO_UPDATES) {
        if (!changeIds) {
            helper.exitFailure("No Change Requests found to check.");
        }
    }

    if (stateCount == totalNum) {
        println "Found the correct state on ${stateCount} out of ${totalNum} Change Requests.";
        inProgress = false;
    } else {
        println "Only found correct state on ${stateCount} out of ${totalNum} Change Requests, sleeping for ${props['waitInterval']} secs..."
    }

    // wait
    if (!forever && inProgress) {
        if (++retryCount == maxRetryCount)
            inProgress = false
    }
    if (inProgress) sleep(waitInterval)
}

if (maxRetryCount != 1 && (retryCount == maxRetryCount)) {
    helper.exitFailure("Maximum number of retries ${maxRetryCount} reached, terminating...")
}

if (failMode == FailMode.FAIL_ON_ANY_FAILURE) {
    if (!changeIds || (stateCount != totalNum)) {
        helper.exitFailure("Only found correct state on ${stateCount} out of ${totalNum} Change Requests.");
    }
}

System.exit(0)