def _internalDeployECSService(stageName, account, Map args) {
  if(args.onPreDeploy) {
    spicyUtils.stageWithFailure("PreDeploy") {
      args.onPreDeploy.call(args, [
        account: account
      ]);
    }
  }
  stage(stageName) {
    try {
      def commonProperties = readYaml file: "deployment/vars.common.yml"
      def envProperties = readYaml file: "deployment/vars.${account.environmentName}.yml"

      def deploymentArgs = commonProperties + envProperties;
      def combinedArgs = deploymentArgs + args

      def hostName = combinedArgs.hostName
      def InActiveHostName = "inactive-${hostName}"

      def build = args.build ?: gitUtils.getShortSHA();
      def changeBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME;
      def useClusterAlb = args.useClusterAlb || false
      def commonStackName = "${args.serviceName}-${account.environmentName}-bg-common"
      def serviceStackName = "${args.serviceName}-${account.environmentName}-blue"
      def containerName = "${args.serviceName}-${build}"

      def sharedVars = []

      for (arg in account) {
        sharedVars.add("${arg.key}='${arg.value}'")
      }

      for (arg in deploymentArgs) {
        sharedVars.add((arg.value != null) ? "${arg.key}='${arg.value}'" : null)
      }

      sharedVars.add("build='${build}'")
      sharedVars.add("commonStackName='${commonStackName}'")
      sharedVars.add(combinedArgs.clusterName ? "clusterName='${combinedArgs.clusterName}-${account.environmentName}'" : null)
      sharedVars.add("branchName='${env.BRANCH_NAME}'")
      sharedVars.add("changeBranch='${changeBranch}'")

      sharedVars.removeAll([null])

      def UseACLSubnets = combinedArgs.useACLSubnets || false

      def DefaultExternalSubnets = [
        account.publicSubnetA ?: null,
        account.publicSubnetB ?: null,
        account.publicSubnetC ?: null,
        account.publicSubnetD ?: null
      ]
      DefaultExternalSubnets.removeAll([null]);

      def DefaultInternalSubnets = UseACLSubnets ? [
        account.privateSubnetA2 ?: null,
        account.privateSubnetB2 ?: null,
        account.privateSubnetC2 ?: null,
        account.privateSubnetD2 ?: null
      ] : [
        account.privateSubnetA1 ?: null,
        account.privateSubnetB1 ?: null,
        account.privateSubnetC1 ?: null,
        account.privateSubnetD1 ?: null
      ]
      DefaultInternalSubnets.removeAll([null]);

      def vpcExternalSubnets = combinedArgs.vpcExternalSubnets ?: DefaultExternalSubnets.join(',')
      def vpcInternalSubnets = combinedArgs.vpcInternalSubnets ?: DefaultInternalSubnets.join(',')

      def commonVars = []

      commonVars.add("vpcExternalSubnets='${vpcExternalSubnets}'")
      commonVars.add("vpcInternalSubnets='${vpcInternalSubnets}'")

      // hostname binding is for route53 in common stack; must be unmodified
      // it creates both regular hostname and one prepended with "inactive-""
      commonVars.add(combinedArgs.hostName ? "hostName='${combinedArgs.hostName}'" : null)

      commonVars.removeAll([null])

      ansibleUtils.runAnsible(
        account: account,
        command: "ansible-playbook ./playbooks/ecs-service-bg-common.yml -vvv --extra-vars \"${sharedVars.join(' ')}\" --extra-vars \"${commonVars.join(' ')}\""
      )

      def ecrImageId = dockerUtils.getECRImageTagSHA(
        account: account,
        serviceName: args.serviceName
      );

      def serviceVars = []

      serviceVars.add("ecrImageId='${ecrImageId}'")
      serviceVars.add(args.serviceName ? "serviceName='${args.serviceName}'" : null)
      serviceVars.add("containerName='${containerName}'")
      serviceVars.add("serviceStackName='${serviceStackName}'")

      // this is to be adjusted based on blue vs green
      // blue is the default, alb priority 100
      // a new deployment gets pushed to priority 200 for testing
      serviceVars.add(combinedArgs.hostName ? "hostName='${combinedArgs.hostName}'" : null)

      serviceVars.removeAll([null])

      ansibleUtils.runAnsible(
        account: account,
        command: "ansible-playbook ./playbooks/ecs-service.yml -vvv --extra-vars \"${sharedVars.join(' ')}\" --extra-vars \"${serviceVars.join(' ')}\""
      )

      echo awsUtils.runCLI(
        account: account,
        command: "aws cloudformation describe-stacks --stack-name ${serviceStackName}"
      )

      githubUtils.setSuccess(stageName)
      if(args.onPostDeploy) {
        spicyUtils.stageWithFailure("PostDeploy - ${account.environmentName}") {
          args.onPostDeploy.call(args, [
            account: account
          ]);
        }
      }
      if(args.blueGreenTest) {
        spicyUtils.stageWithFailure("BlueGreenTest - ${account.environmentName}") {
          args.smokeTest.call(args, [
            account: account,
            activeHostName: hostName,
            inActiveHostName: InActiveHostName,
            healthCheckUrl: combinedArgs.healthCheckUrl
          ]);
        }
      }
      if(args.smokeTest) {
        spicyUtils.stageWithFailure("SmokeTest - ${account.environmentName}") {
          args.smokeTest.call(args, [
            account: account,
            activeHostName: hostName,
            healthCheckUrl: combinedArgs.healthCheckUrl
          ]);
        }
      }
    } catch(err) {
      githubUtils.setFailed(stageName)
      throw err;
    }
  }
}

def call(Map args) {
  args = spicyDefaults(args)
  def account = args.account
  timeout(time: 1, unit: "DAYS") {
    timestamps {
      node("docker") {
        properties(args.pipelineProperties)
        ansiColor("xterm") {
          stage("Checkout") {
            checkout scm
            githubUtils.setSuccess("checkout")
          }
          stage("Setup") {
            // get base ansible files
            ansibleUtils.ensureAnsibleConfiguration("ecs-service")
            ansibleUtils.ensureAnsibleConfiguration("ecs-service-bg-common")
            // copy over templates before building ansible
            sh("cp deployment/*.* ansible/playbooks/templates/")
            // build ansible with templates stored
            ansibleUtils.buildAnsibleImage();
            githubUtils.setSuccess("setup")
          }
          if(!args.buildCommand) {
            echo("using default build command")
            spicyUtils.stageWithFailure("build") {
              sh "docker build -t ${dockerUtils.getContainerName(serviceName: args.serviceName)} -f Dockerfile ."
            }
          } else {
            spicyUtils.stageWithFailure("build") {
              args.buildCommand.call(args);
            }
          }
          githubUtils.setSuccess("build")
          if(args.onPostBuild) {
            spicyUtils.stageWithFailure("PostBuild") {
              args.onPostBuild.call(args);
            }
          }
          stage("Tag Images") {
            dockerUtils.tagImage(
              account: account,
              serviceName: args.serviceName,
            )
            githubUtils.setSuccess("tag-images")
          }
          stage("Publish Images") {
            dockerUtils.pushImage(
              account: account,
              serviceName: args.serviceName,
            )
            githubUtils.setSuccess("publish-images")
          }
          stage("Ansible") {
            if(args.devAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.devDeploymentBranch)) {
              _internalDeployECSService("Deploy - Development", args.devAccount, args)
            }
            if(args.sandboxAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.sandboxDeploymentBranch)) {
              _internalDeployECSService("Deploy - Sandbox", args.sandboxAccount, args)
            }
            if(args.stagingAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.stagingDeploymentBranch)) {
              _internalDeployECSService("Deploy - Staging", args.stagingAccount, args)
            }
            if(args.prodAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.prodDeploymentBranch)) {
              manualApproval(
                time: 4,
                timeUnit: "HOURS",
                message: "Deploy - Production",
                allowConcurrentBuildsDuringInput: true,
                pipelineProperties: args.pipelineProperties
              ) {
                _internalDeployECSService("Deploy - Production", args.prodAccount, args)
              }
            }
            githubUtils.setSuccess("ansible")
          }
        }
      }
    }
  }
}
