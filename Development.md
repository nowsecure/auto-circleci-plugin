# NowSecure AUTO Jenkins Plugin

## Building and Testing
```
gradle test
```

## Local Installing
```
CircleCI 2.1 doesn't support local testing yet
```

## Initialize 
```
circleci setup
```

## Creating new version
Edit src/main/resources/version.txt and update version number.

Build 
```
gradle
```

## CircleCI Orb config
The main config is defined in .circle/orb/orb.yml

### Validate config
```
circleci orb validate .circle/orb/orb.yml
```

### Publish config
```
circleci orb publish dev .circleci/orb/orb.yml ci-auto-test1 ci-auto-orb1  dev:alpha
```

## Resources
- https://circleci.com/docs/2.0/language-java/
- https://github.com/CircleCI-Public/config-preview-sdk/blob/master/docs/orbs-authoring.md
- https://circleci.com/docs/2.0/artifacts/
- https://circleci.com/blog/deep-diving-into-circleci-workspaces/
