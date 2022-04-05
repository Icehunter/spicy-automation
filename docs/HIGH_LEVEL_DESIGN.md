# Spicy Automation: Design

- [Solve the 90% Case](#solve-the-90-case)
- [Standardize around AWS](#standardize-around-aws)
- [Docker builds on the Jenkins workers](#docker-builds-on-the-jenkins-workers)
- [Make it easy to do things the right way](#make-it-easy-to-do-things-the-right-way)
- [Provide pluggable integration points](#provide-pluggable-integration-points)
- [Be opinionated within each context](#be-opinionated-within-each-context)
- [Stateless](#stateless)

## Solve the 90% Case

The majority of use cases for SA deployments consists of:

- A single microservice running within a Docker container, or:
- A static web frontend that is compiled, versioned, and mapped to a route, plus:
- Any required AWS infrastructure such as S3 buckets, RDS databases, SNS topics, SQS queues, etc.

The most appropriate and flexible AWS solution for this use case is Elastic Container Service (ECS) for running docker containers, Cloudfront/S3 for static file hosting, and AWS Cloudformation for managing required infrastructure resources. In order to maximize cluster utilization, teams are encouraged to deploy multiple services to a single cluster.

## Standardize around AWS

Since we only use a single cloud provider, it was decided early on to use CloudFormation for deploying all services since that is AWS's flagship product for managing resources on their platform. SA doesn't directly update AWS resources, and instead ensures that all AWS changes are performed through CloudFormation so that all changes are tracked. In cases where CloudFormation support is not present, such as the upcoming `ECS Canary`, CloudFormation custom resources are used so that those AWS changes are tracked as well. When a service is decommissioned, teams can have confidence that all AWS resources are removed when their CloudFormation stacks are deleted.

Reliance on Cloudformation also allows engineers to utilize AWS support for services deployed through the Spicy Automation. AWS support has no knowledge of SA, and cannot support it. However, with the current design, teams can open support tickets with AWS stating that service X deployed through CloudFormation stack Y is having problem Z. Likewise, AWS support typically won't assist with Terraform-related issues, and since we’re not utilizing multiple cloud providers, there is not enough benefit to justify deviating from an AWS-supported option.

CloudFormation also makes it simple to properly apply tags to all AWS resources that are managed by CloudFormation. This is how all SA-generated infrastructure guarantees correct tagging of resources.

At a very high level, one can think of SA as a glorified CloudFormation template generator/deployer.

## Docker builds on the Jenkins workers

SA encourages groups to perform builds of their services within Docker containers so that teams can execute their builds locally using the same tooling versions that will be used on Jenkins. Teams are encouraged to not depend on the tooling that's available on the Jenkins workers since there are competing interests between projects under active development versus projects that are in maintenance mode. New development may prefer to use the latest versions of those tools, while projects in maintenance mode often prefer older versions in order to maintain stability. By building projects independently within docker containers, all contention around build tooling is avoided.

## Make it easy to do things the right way

Various organization-specific settings are available directly within SA as variables which are injected during pipeline execution, eliminating the tedium in locating and copying frequently accessed properties such as AWS account ID’s, subnet ID’s, and SSL certificate ID’s, as well as New Relic API keys.

## Provide pluggable integration points

**_IN_PROGRESS_** In the context of the build pipeline, several extension points exist which enable custom behavior per-project. These include callbacks before and after builds, deployments, stack deletions, as well
as blue/green and smoke test callbacks.

Additionally, the entire pipeline itself may be forked on a per-project basis by utilizing the import statement, which specifies the branch or tag to use of the spicy-automation repository. This allows developers to easily experiment and test new SA features using actual projects.

## Be opinionated within each context

There is a balance between excessive configuration and unintuitive assumptions; if the deployment configuration is overly complex and tedious, then frustrating mistakes will occur Likewise, if the configuration makes too many assumptions, then it will lead to dead ends and unexpected behavior.

Each type of pipeline balances these competing characteristics with a bias towards a simple configuration with reasonable defaults, which exposes properties that may be overridden.

## Stateless

On the backend, SA does not have a datastore and will query AWS for any information that it may need. This eliminates discrepancies between the expected state versus the actual state, and prevents the storage and maintenance of redundant data.
