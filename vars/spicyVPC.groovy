
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
            ansibleUtils.ensureAnsibleConfiguration("vpc")
            ansibleUtils.buildAnsibleImage()
            githubUtils.setSuccess("setup")
          }
          if(args.onPreDeploy) {
            spicyUtils.stageWithFailure("PreDeploy") {
              args.onPreDeploy.call(args, [
                account: account
              ]);
            }
          }
          stage("Ansible") {
            if(gitUtils.isMain()) {
              try {
                def availabilityZones = args.availabilityZones ?: ""
                def numberOfAzs = args.numberOfAzs ?: availabilityZones.split(",").length
                def createAdditionalPrivateSubnets = args.createAdditionalPrivateSubnets || false
                def createPrivateSubnets = args.createPrivateSubnets || false

                def vars = []

                vars.add(args.stackName ? "stackName='${args.stackName}'" : null)
                vars.add(args.region ? "region='${args.region}'" : null)
                vars.add(args.ownerTag ? "ownerTag='${args.ownerTag}'" : null)
                vars.add(args.productTag ? "productTag='${args.productTag}'" : null)
                vars.add(args.componentTag ? "componentTag='${args.componentTag}'" : null)
                vars.add("build='${args.build || gitUtils.getShortSHA()}'")
                vars.add("availabilityZones='${availabilityZones}'")
                vars.add("createAdditionalPrivateSubnets='${createAdditionalPrivateSubnets}'")
                vars.add("createPrivateSubnets='${createPrivateSubnets}'")
                vars.add("numberOfAzs='${numberOfAzs}'")
                vars.add(args.privateSubnetA1Cidr ? "privateSubnetA1Cidr='${args.privateSubnetA1Cidr}'" : null)
                vars.add(args.privateSubnetA2Cidr ? "privateSubnetA2Cidr='${args.privateSubnetA2Cidr}'" : null)
                vars.add(args.privateSubnetB1Cidr ? "privateSubnetB1Cidr='${args.privateSubnetB1Cidr}'" : null)
                vars.add(args.privateSubnetB2Cidr ? "privateSubnetB2Cidr='${args.privateSubnetB2Cidr}'" : null)
                vars.add(args.privateSubnetC1Cidr ? "privateSubnetC1Cidr='${args.privateSubnetC1Cidr}'" : null)
                vars.add(args.privateSubnetC2Cidr ? "privateSubnetC2Cidr='${args.privateSubnetC2Cidr}'" : null)
                vars.add(args.privateSubnetD1Cidr ? "privateSubnetD1Cidr='${args.privateSubnetD1Cidr}'" : null)
                vars.add(args.privateSubnetD2Cidr ? "privateSubnetD2Cidr='${args.privateSubnetD2Cidr}'" : null)
                vars.add(args.privateSubnetATag ? "privateSubnetATag='${args.privateSubnetATag}'" : null)
                vars.add(args.privateSubnetBTag ? "privateSubnetBTag='${args.privateSubnetBTag}'" : null)
                vars.add(args.publicSubnet1CIDR ? "publicSubnet1CIDR='${args.publicSubnet1CIDR}'" : null)
                vars.add(args.publicSubnet2CIDR ? "publicSubnet2CIDR='${args.publicSubnet2CIDR}'" : null)
                vars.add(args.publicSubnet3CIDR ? "publicSubnet3CIDR='${args.publicSubnet3CIDR}'" : null)
                vars.add(args.publicSubnet4CIDR ? "publicSubnet4CIDR='${args.publicSubnet4CIDR}'" : null)
                vars.add(args.publicSubnetTag ? "publicSubnetTag='${args.publicSubnetTag}'" : null)
                vars.add(args.vpcCidr ? "vpcCidr='${args.vpcCidr}'" : null)
                vars.add(args.vpcTenancy ? "vpcTenancy='${args.vpcTenancy}'" : null)

                vars.removeAll([null])

                def account = [:];

                account.put("region", args.region)
                account.put("jenkinsAwsCredentialsId", args.jenkinsAwsCredentialsId)

                ansibleUtils.runAnsible(
                  account: account,
                  command: "ansible-playbook ./playbooks/vpc.yml -vvv --extra-vars \"${vars.join(' ')}\""
                )

                githubUtils.setSuccess("Deploy")
                if(args.onPostDeploy) {
                  spicyUtils.stageWithFailure("PostDeploy - ${account.environmentName}") {
                    args.onPostDeploy.call(args, [
                      account: account
                    ]);
                  }
                }
              } catch(err) {
                githubUtils.setFailed("Deploy")
                throw err;
              }
            }
          }
          githubUtils.setSuccess("ansible")
        }
      }
    }
  }
}
