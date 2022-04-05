def call(Map args, Closure body) {
  def userInput = null;
  def aborted = false;
  def allowConcurrentBuildsDuringInput = args.containsKey('allowConcurrentBuildsDuringInput') ? args.allowConcurrentBuildsDuringInput : true

  stage("${args.message} User Input") {
    if(allowConcurrentBuildsDuringInput) {
      spicyUtils.setPipelineProperties(args.pipelineProperties, [disableConcurrentBuilds()])
    }

    try {
      timeout(time: args.time, unit: args.timeUnit) {
        input(
          message: "${args.message}?",
          parameters: (args.parameters ?: [])
        )
      }
    } catch(err) {
      try {
        def user = err.getCauses()[0].getUser();
        if(user.toString() == 'SYSTEM') {
          aborted = !args.runCommandsOnTimeout
        } else {
          aborted = true
        }
      } catch(ugh) {
        aborted = true
      }
    }

    spicyUtils.setPipelineProperties(args.pipelineProperties, null)

    if(aborted) {
      return -1
    }

    catchError {
      if(args.passUserInputToBody) {
        body(userInput)
      } else {
        body()
      }
    }
  }
}
