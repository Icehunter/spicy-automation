def setPipelineProperties(baseProperties, propertiesToRemove) {
  def props = []
  if(baseProperties) {
    props.addAll(baseProperties)
  }
  if(propertiesToRemove) {
    props.removeAll(propertiesToRemove)
  }
  echo("Setting pipeline properties to ${props}")
  properties(props)
}

def stageWithFailure(stageName, Map args = [:], Closure body) {
  customStage([
    stageName: stageName,
    failOnError: true
  ] + args, body)
}

def stageWithWarning(stageName, Map args = [:], Closure body) {
  customStage([
    stageName: stageName,
    failOnError: false
  ] + args, body)
}

def customStage(Map args, Closure body) {
  stage(args.stageName) {
    try {
      body()
    } catch(err) {
      if(args.failOnError) {
        throw err
      }
      println(err)
    }
  }
}
