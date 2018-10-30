# NowSecure AUTO CircleCI Plugin


This plugin adds the ability to perform automatic mobile app security testing for Android and iOS mobile apps through the NowSecure AUTO test engine.

## Summary:
Purpose-built for mobile app teams, NowSecure AUTO provides fully automated, mobile appsec testing coverage (static+dynamic+behavioral tests) optimized for the dev pipeline. Because NowSecure tests the mobile app binary post-build from CircleCI, it can test software developed in any language and provides complete results including newly developed code, 3rd party code, and compiler/operating system dependencies. With near zero false positives, NowSecure pinpoints real issues in minutes, with developer fix details, and routes tickets automatically into ticketing systems, such as Jira. NowSecure is frequently used to perform security testing in parallel with functional testing in the dev cycle. Requires a license for and connection to the NowSecure AUTO software.
 https://www.nowsecure.com

## Job Parameters
Following are parameters needed for the job
- auto_token: mandatory parameter for API token. visit https://docs.nowsecure.com/auto/integration-services/jenkins-integration to generate token. Also, we recommend using environment variable AUTO_TOKEN to define this parameter instead of using job parameter.
- auto_file: mandatory parameter to specify mobile binary. Note you must attach workspace from previous step where this file was created.
- auto_url: optional parameter for nowsecure auto API URL with default value of https://lab-api.nowsecure.com
- auto_group: optional parameter for group-id. You can also use environment variable AUTO_GROUP to specify this
- auto_wait: optional parameter to specify maximum wait in minutes until security test is completed. The default value is 30 minutes and you can skip wait by specifying 0 value.

## Sample Usage
You can use Auto CircleCI Orb as follows:
```
version: 2.1
orbs:
  auto_ci: nowsecure/ci-auto-orb@1.0.0
jobs:
  build:
    docker:
    - image: circleci/openjdk:8-jdk
    steps:
    - attach_workspace:
        at: /tmp/myworkspace
    - checkout
    - run: cp apkpure_app_887.apk /tmp/myworkspace/test.apk
    - auto_ci/mobile_security_test:
        auto_file: /tmp/myworkspace/test.apk
        auto_wait: "30"
        auto_score: "50"
```

Note that you will generate mobile binary using gradle, Makefile, Fastlane or other tools instead of copying file but it shows how binary file will be created and then passed to the Auto CircleCI Orb for security analysis.

