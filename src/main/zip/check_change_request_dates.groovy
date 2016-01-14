import com.urbancode.air.AirPluginTool;
import com.serena.air.plugin.ssm.SerenaSSMHelper;
import com.serena.air.plugin.ssm.FailMode;

final def apTool = new AirPluginTool(args[0], args[1]);
final def props = apTool.getStepProperties();
final def helper = new SerenaSSMHelper(apTool);
long waitInterval = Long.parseLong(props['waitInterval'])*1000;
int maxRetryCount = Integer.parseInt(props['maxRetryCount']);

def failMode = FailMode.valueOf(props['failMode']);
def changeIds = props['changeIds'].split(',') as List;
def dateCount = 0;

boolean inProgress = true
boolean forever = (waitInterval == -1 ? true : false)
def totalNum = changeIds.size();
int retryCount = 0
while (inProgress) {
    for (def changeId : changeIds.sort()) {
        if (helper.changeRequestExists(changeId)) {
            if (helper.checkChangeRequestDates(changeId)) {
                dateCount++;
            } else {
                if (failMode == FailMode.FAIL_FAST) {
                    helper.exitFailure("Change Request \"${changeId}\" is not in valid implementation date.");
                } else {
                    println "Change Request \"${changeId}\" is not in valid implementation date.";
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

    if (dateCount == totalNum) {
        println "Found today's date in range on ${dateCount} out of ${totalNum} Change Requests.";
        inProgress = false;
    } else {
        println "Only found today's date in range on ${dateCount} out of ${totalNum} Change Requests, sleeping for ${props['waitInterval']} secs..."
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
    if (!changeIds || (dateCount != totalNum)) {
        helper.exitFailure("Only found today's date in range on ${dateCount} out of ${totalNum} Change Requests.");
    }
}

System.exit(0)