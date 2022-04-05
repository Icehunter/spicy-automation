
def _internalDeployECSCluster(stageName, account, Map args) {
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

      def instanceType = combinedArgs.instanceType;
      def instanceSize = awsUtils.getECSInstanceSizes().get(instanceType);

      if(!instanceSize) {
        throw new Exception("Cannot find instance type ${instanceType} — instance type either doesn't exist or isn't supported by SA")
      }

      def containerMemoryReservation = combinedArgs.largestContainerMemoryReservation ?: 4096
      def maxByMemory = instanceSize[0] / containerMemoryReservation

      def containerCPUReservation = combinedArgs.largestContainerCpuReservation ?: 1024
      def maxByCPU = instanceSize[0] / containerCPUReservation

      def maxContainers = (maxByMemory < maxByCPU ? maxByMemory : maxByCPU).toInteger()

      if(maxContainers <= 1) {
        def message = """|
                |The scale up/down threshold are both set to 1 so autoscaling is not going to work
                |as expected. You need to either use a larger instance type or decrease the size of
                |the largest container memory/cpu.
                |
                |instanceType: ${instanceType}
                |Largest Container CPU = ${containerCPUReservation}, Available CPU = ${instanceSize[1]}
                |Largest Container Memory = ${containerMemoryReservation}, Available Memory = ${instanceSize[0]}
              """.trim().stripMargin()
        echo("println ${message}")
      }

      echo("Scale down threshold: memory=${maxByMemory}, cpu=${maxByCPU}; using ${maxContainers}")

      def build = combinedArgs.build ?: gitUtils.getShortSHA();
      def changeBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME;
      def createExternalFacingLoadBalancer = combinedArgs.createExternalFacingLoadBalancer || false
      def createInternalLoadBalancer = combinedArgs.createInternalLoadBalancer || false
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

      def vars = []

      vars.add("build='${build}'")
      vars.add("branchName='${env.BRANCH_NAME}'")
      vars.add("changeBranch='${changeBranch}'")
      vars.add("clusterScaleDownThreshold='${maxContainers}'")
      vars.add("createExternalFacingLoadBalancer='${createExternalFacingLoadBalancer}'")
      vars.add("createInternalLoadBalancer='${createInternalLoadBalancer}'")
      vars.add("vpcExternalSubnets='${vpcExternalSubnets}'")
      vars.add("vpcInternalSubnets='${vpcInternalSubnets}'")
      vars.add(combinedArgs.stackName ? "stackName='${combinedArgs.stackName}-${account.environmentName}'" : null)
      vars.add(combinedArgs.containerInsights ? "containerInsights='${combinedArgs.containerInsights}'" : null)
      vars.add(combinedArgs.ownerTag ? "ownerTag='${combinedArgs.ownerTag}'" : null)
      vars.add(combinedArgs.productTag ? "productTag='${combinedArgs.productTag}'" : null)
      vars.add(combinedArgs.componentTag ? "componentTag='${combinedArgs.componentTag}'" : null)
      vars.add(combinedArgs.instanceType ? "instanceType='${combinedArgs.instanceType}'" : null)
      vars.add(combinedArgs.minClusterSize ? "minClusterSize='${combinedArgs.minClusterSize}'" : null)
      vars.add(combinedArgs.maxClusterSize ? "maxClusterSize='${combinedArgs.maxClusterSize}'" : null)
      vars.add(combinedArgs.maxBatchSize ? "maxBatchSize='${combinedArgs.maxBatchSize}'" : null)
      vars.add(combinedArgs.largestContainerCpuReservation ? "largestContainerCpuReservation='${combinedArgs.largestContainerCpuReservation}'" : null)
      vars.add(combinedArgs.largestContainerMemoryReservation ? "largestContainerMemoryReservation='${combinedArgs.largestContainerMemoryReservation}'" : null)
      vars.add(combinedArgs.clusterScaleUpAdjustment ? "clusterScaleUpAdjustment='${combinedArgs.clusterScaleUpAdjustment}'" : null)
      vars.add(combinedArgs.clusterScaleUpAdjustmentType ? "clusterScaleUpAdjustmentType='${combinedArgs.clusterScaleUpAdjustmentType}'" : null)
      vars.add(combinedArgs.clusterScaleUpCooldown ? "clusterScaleUpCooldown='${combinedArgs.clusterScaleUpCooldown}'" : null)
      vars.add(combinedArgs.clusterScaleUpMins ? "clusterScaleUpMins='${combinedArgs.clusterScaleUpMins}'" : null)
      vars.add(combinedArgs.clusterScaleUpThreshold ? "clusterScaleUpThreshold='${combinedArgs.clusterScaleUpThreshold}'" : null)
      vars.add(combinedArgs.clusterScaleDownAdjustment ? "clusterScaleDownAdjustment='${combinedArgs.clusterScaleDownAdjustment}'" : null)
      vars.add(combinedArgs.clusterScaleDownAdjustmentType ? "clusterScaleDownAdjustmentType='${combinedArgs.clusterScaleDownAdjustmentType}'" : null)
      vars.add(combinedArgs.clusterScaleDownCooldown ? "clusterScaleDownCooldown='${combinedArgs.clusterScaleDownCooldown}'" : null)
      vars.add(combinedArgs.clusterScaleDownMins ? "clusterScaleDownMins='${combinedArgs.clusterScaleDownMins}'" : null)
      vars.add(combinedArgs.additonalEc2SecurityGroups ? "additonalEc2SecurityGroups='${combinedArgs.additonalEc2SecurityGroups}'" : null)
      vars.add(combinedArgs.externalElbSecurityGroups ? "externalElbSecurityGroups='${combinedArgs.externalElbSecurityGroups}'" : null)
      vars.add(combinedArgs.ebsVolumeSize ? "ebsVolumeSize='${combinedArgs.ebsVolumeSize}'" : null)
      vars.add(combinedArgs.logsS3BucketName ? "logsS3BucketName='${combinedArgs.logsS3BucketName}'" : null)
      vars.add(combinedArgs.purgeS3LogsOnStackDelete ? "purgeS3LogsOnStackDelete='${combinedArgs.purgeS3LogsOnStackDelete}'" : false)
      vars.add(combinedArgs.asgTerminateTimeout ? "asgTerminateTimeout='${combinedArgs.asgTerminateTimeout}'" : null)
      vars.add(combinedArgs.containerDeviceManagerSize ? "containerDeviceManagerSize='${combinedArgs.containerDeviceManagerSize}'" : null)
      vars.add(combinedArgs.externalFacingLoadBalancerIdleTimeout ? "externalFacingLoadBalancerIdleTimeout='${combinedArgs.externalFacingLoadBalancerIdleTimeout}'" : null)
      vars.add(combinedArgs.internalLoadBalancerIdleTimeout ? "internalLoadBalancerIdleTimeout='${combinedArgs.internalLoadBalancerIdleTimeout}'" : null)
      vars.add(combinedArgs.userDataVersion ? "userDataVersion='${combinedArgs.userDataVersion}'" : null)
      vars.add(combinedArgs.task1ToStartOnAllInstances ? "task1ToStartOnAllInstances='${combinedArgs.task1ToStartOnAllInstances}'" : null)

      for (arg in account) {
          vars.add("${arg.key}=${arg.value}")
      }

      vars.removeAll([null])

      ansibleUtils.runAnsible(
        account: account,
        command: "ansible-playbook ./playbooks/ecs-cluster.yml -vvv --extra-vars \"${vars.join(' ')}\""
      )

      githubUtils.setSuccess(stageName)
      if(args.onPostDeploy) {
        spicyUtils.stageWithFailure("PostDeploy - ${account.environmentName}") {
          args.onPostDeploy.call(args, [
            account: account
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
            ansibleUtils.ensureAnsibleConfiguration("ecs-cluster")
            ansibleUtils.buildAnsibleImage()
            githubUtils.setSuccess("setup")
          }
          stage("Ansible") {
            if(args.devAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.devDeploymentBranch)) {
              _internalDeployECSCluster("Deploy - Development", args.devAccount, args)
            }
            if(args.sandboxAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.sandboxDeploymentBranch)) {
              _internalDeployECSCluster("Deploy - Sandbox", args.sandboxAccount, args)
            }
            if(args.stagingAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.stagingDeploymentBranch)) {
              _internalDeployECSCluster("Deploy - Staging", args.stagingAccount, args)
            }
            if(args.prodAccount && (gitUtils.isMain() || "${JOB_BASE_NAME}" == args.prodDeploymentBranch)) {
              manualApproval(
                time: 4,
                timeUnit: "HOURS",
                message: "Deploy - Production",
                allowConcurrentBuildsDuringInput: true,
                pipelineProperties: args.pipelineProperties
              ) {
                _internalDeployECSCluster("Deploy - Production", args.prodAccount, args)
              }
            }
            githubUtils.setSuccess("ansible")
          }
        }
      }
    }
  }
}
