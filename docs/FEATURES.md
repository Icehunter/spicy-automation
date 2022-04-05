# SA Features

Some [high-level design principles](HIGH_LEVEL_DESIGN.md) of the Spicy Automation.

- Supports deploying to ECS, S3/CloudFront, CloudFormation. All configuration about your application's deployment is stored inside your application's repository.
- Account level information and pipeline defaults are stored in this repository.
- Your application's repository contains a Jenkinsfile that tells Jenkins Pipeline how to perform the deployment.
- For the ECS-based deployments:
  - **_IN_PROGRESS_** You'll get full blue/green deployments and you can roll back to a previous deployment in less than 30 seconds. After a prod deployment, the old environment is kept around for a default of 2 hours.
  - **_IN_PROGRESS_** Before a production deployment, a single canary container is introduced with the new application version for a default of 5 minutes before the full application deployment occurs. You have the option to roll back at anytime.
  - **_IN_PROGRESS_** Your application's automated integration test suite can be ran against the inactive stack before the blue/green deployment sends production traffic to the new deployment.
  - The ECS Cluster CloudFormation template is fully managed and your Dev/Sandbox/Staging ECS clusters get automatic updates. Upgrading production requires manually clicking a single link in Jenkins.
  - **_IN_PROGRESS_** Integration with [autospotting](https://github.com/cristim/autospotting) so that you can run your Dev and QA environments on spot instances when the spot bid price is low. When the bid price is too high, your instances automatically fall back to on-demand instances. We could see a 70-80% cost savings on the posted on-demand price by using autospotting.
  - Supports publishing Docker images into AWS's Elastic Container Registry (ECR).
- Makes it very easy for your application to be compliant with tagging policies.
- **_IN_PROGRESS_** You can also have embedded with your project a separate CloudFormation stack for your infrastructure such as databases, S3 buckets, and other AWS resources.
- **_IN_PROGRESS_** Deployment notifications can be sent to New Relic.
- **_IN_PROGRESS_** Various deployment notification messages can be sent to Slack or published on the GitHub pull request.
- **_IN_PROGRESS_** All of the relevant AWS service logs are shown in the Jenkins deployment console during deployment so that it is easy to troubleshoot any deployment failures.
- **_IN_PROGRESS_** Hooks that you can use to customize your SA pipeline: [SA Pipeline Hooks](PIPELINE_HOOKS.md)
