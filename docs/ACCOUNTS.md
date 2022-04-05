# ACCOUNTS

These tools are made under the assumption you are using a VPC created with the wizard, with the default 4 AZ setup.

Accounts are setup as: DEV, SANDBOX, QA, STAGING, PROD

Minimum properties are noted below:

## `aws-<accountName>-<region>.yml`

```
dnsRoute53HostedZoneId: 1A
elbAccountId: 1
accountId: 1
privateSubnetA1: subnet-<hash>
privateSubnetA2: subnet-<hash>
privateSubnetB1: subnet-<hash>
privateSubnetB2: subnet-<hash>
privateSubnetC1: subnet-<hash>
privateSubnetC2: subnet-<hash>
privateSubnetD1: subnet-<hash>
privateSubnetD2: subnet-<hash>
publicSubnetA: subnet-<hash>
publicSubnetB: subnet-<hash>
publicSubnetC: subnet-<hash>
publicSubnetD: subnet-<hash>
region: us-east-1
sslCertificateId: arn:aws:acm:<region>:<accountId>:certificate/<certID>
vpcCidr: <vpcCidr>
vpcId: vpc-<hash>
# can be found at https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-optimized_AMI.html
ecsAmiId: ami-<ECSOptimized64Image>
jenkinsAwsCredentialsId: <awsSecretsManagerID>
ecrBaseRepository: <accountId>.dkr.ecr.us-east-1.amazonaws.com
```

## `environments/aws-<accountName>-<region>-<env>.yml`

```
dnsRoute53ZoneBase: <domain>
ec2KeyName: <keyName>
environmentName: development
shortEnvironmentName: dev
```
