def getAnsibleImageTag() {
  return "ansible:${gitUtils.getShortSHA()}"
}

def buildAnsibleImage() {
  sh("docker build -t ${getAnsibleImageTag()} -f ansible/Dockerfile ./ansible")
}

def ensureAnsibleConfiguration(templateName) {
  sh "mkdir -pv ansible ansible/playbooks ansible/playbooks/cloudformation ansible/playbooks/vars";
  resources.copyResourceFile(
    source: "ansible/Dockerfile",
    destination: "ansible/Dockerfile"
  )
  resources.copyResourceFile(
    source: "ansible/requirements.txt",
    destination: "ansible/requirements.txt"
  )
  resources.copyResourceFile(
    source: "ansible/playbooks/${templateName}.yml",
    destination: "ansible/playbooks/${templateName}.yml"
  )
  resources.copyResourceFile(
    source: "ansible/playbooks/vars/${templateName}.yml",
    destination: "ansible/playbooks/vars/${templateName}.yml"
  )
  resources.copyResourceFile(
    source: "ansible/playbooks/cloudformation/${templateName}.yml",
    destination: "ansible/playbooks/cloudformation/${templateName}.yml"
  )
  resources.copyResourceFile(
    source: "ansible/playbooks/templates/${templateName}.yml",
    destination: "ansible/playbooks/templates/${templateName}.yml"
  )
}

def runAnsible(Map args) {
  withCredentials([usernamePassword(credentialsId: args.account.jenkinsAwsCredentialsId, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
    sh("docker run -e AWS_REGION=${args.account.region} -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} -t ${getAnsibleImageTag()} ${args.command}")
  }
}
