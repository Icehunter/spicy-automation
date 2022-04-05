def call(Map args) {
  return args + [
    /*
     * Properties for the Jenkins Pipeline. Does not allow concurrent builds by default. If you'd
     * like to schedule a job using a cron syntax, then set the value to:
     *
     *     [disableConcurrentBuilds(), pipelineTriggers([cron('H 13 * * *')])]
     */
    pipelineProperties: [disableConcurrentBuilds()].plus(args.pipelineProperties ?: []),
  ]
}
