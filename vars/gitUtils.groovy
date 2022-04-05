def getRemoteURL() {
  return sh(
    script: 'git config --get remote.origin.url',
    returnStdout: true
  ).trim()
}

def getSHA() {
  return sh(
    script: 'git rev-parse HEAD',
    returnStdout: true
  ).trim()
}

def getShortSHA() {
  return sh(
    script: 'git rev-parse --short HEAD',
    returnStdout: true
  ).trim()
}

def isMain() {
    return "${BRANCH_NAME}" == "main" || "${BRANCH_NAME}" == "main";
}
