# VPC Pipeline

The Amazon VPC architecture includes public and private subnets. The first set of private subnets share the default network access control list (ACL) from the Amazon VPC, and a second, optional set of private subnets include dedicated custom network ACLs per subnet. This divides the Amazon VPC address space in a predictable manner across multiple Availability Zones, and deploys NAT gateways in each Availability Zone, which provide highly available outbound internet access for the private subnets.

## Architecture

![architecture](vpc-design.png)

## Usage

With _spicy-automation_ added as a global library you can setup a "my-stack-name-vpc" as a repo. Add a Jenkinsfile to your repo with the following content:

```
#!/usr/bin/env groovy

@Library(["spicy-automation@development"]) _

spicyVPC(
    jenkinsAwsCredentialsId: "<credentialsID>",  // aws secrets manager plugin for jenkins, stored as un/pw
    region: "<region>",
    stackName: "<stackName>",
    ownerTag: "<owner>",
    productTag: "<product>",
    componentTag: "<component>",
    availabilityZones: "List<availabiltyZone>", // comma delimited
)
```

The following defaults are assumed and can be overridden:

```
availabilityZones: ""
createAdditionalPrivateSubnets: true
createPrivateSubnets: true
numberOfAzs: 4
privateSubnetA1Cidr: 172.1.0.0/19
privateSubnetA2Cidr: 172.1.192.0/21
privateSubnetB1Cidr: 172.1.32.0/19
privateSubnetB2Cidr: 172.1.200.0/21
privateSubnetC1Cidr: 172.1.64.0/19
privateSubnetC2Cidr: 172.1.208.0/21
privateSubnetD1Cidr: 172.1.96.0/19
privateSubnetD2Cidr: 172.1.216.0/21
privateSubnetATag: Network=Private
privateSubnetBTag: Network=Private
publicSubnetACidr: 172.1.128.0/20
publicSubnetBCidr: 172.1.144.0/20
publicSubnetCCidr: 172.1.160.0/20
publicSubnetDCidr: 172.1.176.0/20
publicSubnetTag: Network=Public
vpcCidr: 172.1.0.0/16
vpcTenancy: default
```

Here is a breakdown of each property:

- **ownerTag**: Owner tag for meta.
- **productTag**: Product tag for for meta.
- **componentTag**: Component tag for for meta.
- **build**: Build tag for for meta.
- **availabilityZones**: List of Availability Zones to use for the subnets in the VPC. Note: The logical order is preserved."
- **createAdditionalPrivateSubnets**: Set to true to create a network ACL protected subnet in each Availability Zone. If false, the CIDR parameters for those subnets will be ignored. If true, it also requires that the 'Create private subnets' parameter is also true to have any effect.
- **createPrivateSubnets**: Set to false to create only public Subnets. If false, the CIDR parameters for ALL private subnets will be ignored.
- **numberOfAzs**: Number of Availability Zones to use in the VPC. This must match your selections in the list of Availability Zones parameter.
- **privateSubnetA1Cidr**: CIDR block for Private Subnet A1 located in Availability Zone 1
- **privateSubnetA2Cidr**: CIDR block for Private Subnet A2 with dedicated network ACL located in Availability Zone 1
- **privateSubnetB1Cidr**: CIDR block for Private Subnet B1 located in Availability Zone 2
- **privateSubnetB2Cidr**: CIDR block for Private Subnet B2 with dedicated network ACL located in Availability Zone 2
- **privateSubnetC1Cidr**: CIDR block for Private Subnet C1 located in Availability Zone 3
- **privateSubnetC2Cidr**: CIDR block for Private Subnet C2 with dedicated network ACL located in Availability Zone 3
- **privateSubnetD1Cidr**: CIDR block for Private Subnet D1 located in Availability Zone 4
- **privateSubnetD2Cidr**: CIDR block for Private Subnet D2 with dedicated network ACL located in Availability Zone 4
- **privateSubnetATag**: tag to add to private subnets A, in format Key=Value (Optional)
- **privateSubnetBTag**: tag to add to private subnets B, in format Key=Value (Optional)
- **publicSubnetACidr**: CIDR block for the public DMZ subnet 1 located in Availability Zone A
- **publicSubnetBCidr**: CIDR block for the public DMZ subnet 2 located in Availability Zone B
- **publicSubnetCCidr**: CIDR block for the public DMZ subnet 3 located in Availability Zone C
- **publicSubnetDCidr**: CIDR block for the public DMZ subnet 4 located in Availability Zone D
- **publicSubnetTag**: tag to add to public Subnets, in format Key=Value (Optional)
- **vpcCidr**: CIDR block for the VPC
- **vpcTenancy**: The allowed tenancy of instances launched into the VPC
