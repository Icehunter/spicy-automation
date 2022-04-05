# The Spicy Automation

> DISCLAIMER: Please note that our documentation is not written by technical writers and is put together by our internal engineering teams and all the individuals who have contributed to SA source code. SA's documentation assumes you have some prior knowledge and hands-on experience with AWS core services such as EC2, IAM, S3, lambda, along with the knowledge to work with any popular programming languages.

- [Useful Quick Links](#useful-quick-links)
- [Description](#description)
- [Supported Pipelines](#supported-pipelines)
- [Contributing](#contributing)
- [Support](#support)
- [Contributing](#contributing)

## Useful Quick Links

- [Jenkins Global library documentation](https://github.com/jenkinsci/workflow-cps-global-lib-plugin)
- [Pipeline Steps Reference](https://jenkins.io/doc/pipeline/steps/)
- [Handy Groovy Scripts for Jenkins and CloudBees Jenkins Platform](https://github.com/cloudbees/jenkins-scripts)

## Description

The Spicy Automation (SA) is a managed framework that automates the creation and management of CI/CD pipelines in a consistent and reliable manner using the company's best practices. It is tightly integrated with other services on the the network and allows your application to easily leverage these resources. The document [high-level design principles](docs/HIGH_LEVEL_DESIGN.md) details the design principals and methodology which drive the development of SA

- See [FEATURES](docs/FEATURES.md) for a more detailed list of SA features.
- See [FAQs](docs/FAQS.md) for all other frequently asked questions.

## Accounts

Please read information on how accounts are setup [ACCOUNTS](docs/ACCOUNTS.md)

## Supported Pipelines

- [ECS Cluster Pipeline](docs/ECS_CLUSTER.md)
- [ECS Service Pipeline](docs/ECS_SERVICE.md)
- [VPC Pipeline](docs/VPC.md)

_Note_: Refer to [this document](docs/DEPRECATIONS.md) for a list of deprecated pipelines and features

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for a set of guidelines for contributing to the Spicy Automation.

## Support

Create an issue on Github.
