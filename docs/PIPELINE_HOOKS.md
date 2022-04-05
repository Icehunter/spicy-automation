# Pipeline Hooks

## Table of Contents

- [Introduction](#introduction)
- [`buildCommand`](#buildcommand)
- [`onPostBuild`](#onpostbuild)
- [`onPreDeploy`](#onpredeploy)
- [`onPostDeploy`](#onpostdeploy)
- [`blueGreenTest`](#bluegreentest)
- [`smokeTest`](#smoketest)

## Introduction

SA exposes hooks that allow users to execute custom code at various points in their pipelines. One of the most common use cases for these pipeline hooks is testing; it's possible to set up testing infrastructure, run your full test suite, and clean up temporary resources all within a SA.

Hooks are implemented as [Groovy closures](http://groovy-lang.org/closures.html) that can be set/overridden in your project's `Jenkinsfile`.

Hooks are listed below in the order in which they execute during the pipeline. Unless marked otherwise, these hooks are available to all CloudFormation-based pipelines.

## `buildCommand`

**Execution:** Used intead of default `docker build -t ${dockerUtils.getContainerName(serviceName: args.serviceName)} -f Dockerfile .`

**Sample Usage**:

```bash
buildCommand: { Map args ->
  sh "jenkins-hooks/build.sh ${dockerUtils.getContainerName(serviceName: serviceName)}"
}
```

## `onPostBuild`

**Execution:** After the build completes.

**Sample Usage**: linting, unit tests, API tests, UI component tests, cleanup for any of the above.

```bash
onPostBuild: { Map args ->
    spicyUtils.stageWithFailure("Publish Test Results") {
      dockerUtils.copyFromImage(
        imageID: "${dockerUtils.getContainerName(serviceName: serviceName)}-base",
        dockerPath: "/app/coverage",
        jenkinsPath: "${WORKSPACE}/coverage"
      )
      // TODO: enable and install publish html plugin to start seeing coverage reports in job results
      publishHTML(
        target: [
          allowMissing: false,
          alwaysLinkToLastBuild: true,
          keepAll: true,
          reportDir: "coverage/lcov-report",
          reportFiles: "index.html",
          reportName: "CodeCoverage",
          includes: "**/*"
        ]
      )
    }
  }
}
```

## `onPreDeploy`

**Execution:** After build, before deployment.

**Sample Usage**: Setup for integration or end-to-end tests

## `onPostDeploy`

**Execution:** After CloudFormation deployment, _if and only if_ it succeeds.

**Sample Usage:** test cleanup.

```bash
onPostDeploy: { Map args ->
  spicyUtils.stageWithFailure("Archive") {
    archiveArtifacts artifacts: 'src/**/*.*', fingerprint: true
    sh "touch coverage/junit.xml"
    junit 'coverage/junit.xml'
  }
}
```

## `blueGreenTest`

_(Blue/Green deployment only)_

**Execution:** After the inactive stack is brought up.

**Sample Usage**: integration tests.

```bash
blueGreenTest: { Map args, Map buildInfo ->
  // spicy-ui-services.{env}.web.spicy.com/api/ping
  sh "jenkins-hooks/integration-tests.sh ${buildInfo.inActiveHostName}${buildInfo.healthCheckUrl}"
}
```

## `smokeTest`

_(Blue/Green, S3/CloudFront, and Serverless Lambda deployments only)_

**Execution:**

Blue/Green: After blue/green stacks swap.

S3/CloudFront: After S3 upload.

Serverless Lambda: After Lambda deployment.

**Sample Usage**: smoke tests, integration tests, end-to-end tests.

```bash
smokeTest: { Map args, Map buildInfo ->
  // spicy-ui-services.{env}.web.spicy.com/api/ping
  sh "jenkins-hooks/smoke-tests.sh ${buildInfo.activeHostName}${buildInfo.healthCheckUrl}"
}
```
