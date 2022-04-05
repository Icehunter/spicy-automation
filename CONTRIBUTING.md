# How to Contribute

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

## Table of Contents

- [Contribution Process](#contribution-process)
- [Testing Your Changes](#testing-your-changes)
- [The DevOps Team](#the-devops-team)
- [Programming Languages and Frameworks](#programming-languages-and-frameworks)
  - [Languages](#languages)

In order for the Spicy Automation (SA) to meet the needs of the delivery teams, it will require code and code-review contributions from a variety of different teams. The goal is to make it as easy as possible to contribute changes to Jenkins Shared Libraries (aka Spicy Automation) in a manner consistent with the existing architecture and implementation.

Additionally, we hope to describe the conventions and best practices established by DevOps such that contributions can align with the existing paradigm.

## Contribution Process

The following is a set of guidelines for contributing to Jenkins Shared Libraries (aka Spicy Automation). These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document.

1.  Create JIRA Story for DevOps project
2.  Create a branch from `main` from `spicy-automation`
3.  Test the your branch against an example project. For example configure your project to use the branched version of `spicy-automation`
    ```bash
    @Library("spicy-automation@YOUR_BRANCH") _
    ```
4.  Open a Pull Request once all your Jenkins tests pass (including the ones in this project)
5.  Add the appropriate team as a reviewer for your Pull Request
6.  The DevOps team will:

    - review Pull Request
    - run integration testing to ensure proposed changes do not break the existing functionality and
    - either approve or provide additional feedback

7.  One of the DevOps team members will merge your change

## Testing Your Changes

The DevOps team is working to improve the testing for `spicy-automation` until this repo has proper code coverage and testing manual testing is **required**.

1.  Create a branch on [spicy-automation](https://github.com/Icehunter/spicy-automation) repo.
2.  Modify the `Jenkinsfile` to use your branch of `spicy-automation`.
    ```bash
    @Library("spicy-automation@YOUR_BRANCH") _
    ```
3.  Build the project to make sure your changes don't break any existing functionality.
4.  Post the link to the above testing you have performed when create a Pull Request for this project.

# Programming Languages and Frameworks

## Languages

Spicy Automation is implemented in [Groovy](http://groovy-lang.org).
