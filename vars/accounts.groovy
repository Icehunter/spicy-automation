def get() {
  def accounts = getAccountsWithEnvironments()

  def ELBInfo = [:]

  for (key in accounts.keySet()) {
    ELBInfo.put(key + "_PRIVATE", accounts.get(key) + resources.getProperties("elb-internal.yml"))
    ELBInfo.put(key + "_PUBLIC", accounts.get(key) + resources.getProperties("elb-internet-facing.yml"))
  }

  return accounts + ELBInfo
}

def getAccountsWithEnvironments() {
  return getAccounts() + getDevelopment() + getSandbox() + getQA() + getStaging() + getProduction()
}

def getAccounts() {
  def accounts = [:]

  accounts.put("SPICY_US_EAST_1", resources.getProperties("accounts/aws-spicy-us-east-1.yml"))

  return accounts;
}

def getDevelopment() {
  def accounts = [:]

  accounts.put(
    "SPICY_US_EAST_1_DEV",
    getAccounts().SPICY_US_EAST_1 + resources.getProperties("accounts/environments/aws-spicy-us-east-1-dev.yml")
  )

  return accounts
}

def getSandbox() {
  def accounts = [:]

  accounts.put(
    "SPICY_US_EAST_1_SANDBOX",
    getAccounts().SPICY_US_EAST_1 + resources.getProperties("accounts/environments/aws-spicy-us-east-1-sandbox.yml")
  )

  return accounts
}

def getQA() {
  return [:]
}

def getStaging() {
  def accounts = [:]

  accounts.put(
    "SPICY_US_EAST_1_STAGING",
    getAccounts().SPICY_US_EAST_1 + resources.getProperties("accounts/environments/aws-spicy-us-east-1-staging.yml")
  )

  return accounts
}

def getProduction() {
  def accounts = [:]

  accounts.put(
    "SPICY_US_EAST_1_PROD",
    getAccounts().SPICY_US_EAST_1 + resources.getProperties("accounts/environments/aws-spicy-us-east-1-prod.yml")
  )

  return accounts
}
