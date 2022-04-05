def updateStatus(Map args) {
  try {
    step([
      $class: 'GitHubCommitStatusSetter',
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: gitUtils.getRemoteURL()],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: gitUtils.getSHA()],
      contextSource: [
        $class: 'ManuallyEnteredCommitContextSource',
        context: "continuous-integration/jenkins/${args.context}",
      ],
      statusResultSource: [
        $class: 'ConditionalStatusResultSource',
        results: [
          [$class: 'AnyBuildResult', state: args.type, message: args.message]
        ],
      ],
    ])
  } catch(err) {
    print "Error updating commit status, proceeding without updating: ${err.getClass().getSimpleName()}"
  }
}

def setSuccess(context) {
  updateStatus(
    context: context,
    type: 'SUCCESS',
    message: 'Completed'
  )
}

def setPending(context) {
  updateStatus(
    context: context,
    type: 'PENDING',
    message: 'Pending'
  )
}

def setFailed(context) {
  updateStatus(
    context: context,
    type: 'FAILED',
    message: 'Failed'
  )
}
