def copyFromImage(Map args) {
  sh(
    script:  """#!/bin/bash +x
      set -e
      IMG_ID=\$(dd if=/dev/urandom bs=1k count=1 2> /dev/null | LC_CTYPE=C tr -cd "a-z0-9" | cut -c 1-22)
      docker create --name \${IMG_ID} ${args.imageID}
      docker cp \${IMG_ID}:${args.dockerPath} ${args.jenkinsPath}
      docker rm \${IMG_ID}
      """
  )
}

def getContainerName(Map args) {
    return "${args.serviceName}-${gitUtils.getShortSHA()}"
}

def getImageTag(Map args) {
    return "${args.serviceName}:${gitUtils.getShortSHA()}"
}

def getECRImageTagSHA(Map args) {
    return "${args.account.ecrBaseRepository}/${args.serviceName}:${gitUtils.getShortSHA()}"
}

def getECRImageTagLatest(Map args) {
    return "${args.account.ecrBaseRepository}/${args.serviceName}:latest"
}

def tagImage(Map args) {
  sh("docker tag ${getContainerName(args)} ${getECRImageTagSHA(args)}")
  if(gitUtils.isMain()) {
    sh("docker tag ${getContainerName(args)} ${getECRImageTagLatest(args)}")
  }
}

def pushImage(Map args) {
  withCredentials([usernamePassword(credentialsId: args.account.jenkinsAwsCredentialsId, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
    sh("aws ecr get-login-password --region ${args.account.region} | docker login --username AWS --password-stdin ${args.account.accountId}.dkr.ecr.us-east-1.amazonaws.com/${args.serviceName}")
    sh("docker push ${getECRImageTagSHA(args)}")
    if(gitUtils.isMain()) {
      sh("docker push ${getECRImageTagLatest(args)}")
    }
  }
}
