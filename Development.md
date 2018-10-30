# NowSecure AUTO CircleCI Plugin

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

### Create namespace
```
circleci namespace create nowsecure github nowsecure
```
Note: This is already done so you don't need to recreate namespace.

### Create auto orb
```
circleci orb create nowsecure/ci-auto-orb
```
Note: This is already done so you don't need to recreate orb.

### Validate config
```
circleci orb validate .circleci/orb/orb.yml
```

### Github release
Create new tag and release for the new version. Note: the orb.yml should reference new version 

### Publish Dev config
```
circleci orb publish .circleci/orb/orb.yml nowsecure/ci-auto-orb@dev:alpha
```

### Publish Prod config
```
circleci orb publish promote nowsecure/ci-auto-orb@dev:alpha major
```
or
```
circleci orb publish promote nowsecure/ci-auto-orb@dev:alpha patch
```

## Resources
- https://github.com/CircleCI-Public/config-preview-sdk/blob/master/docs/orbs-authoring.md
- https://github.com/CircleCI-Public/config-preview-sdk/blob/master/docs/README.md
- https://circleci.com/docs/2.0/language-java/
- https://github.com/CircleCI-Public/config-preview-sdk/blob/master/docs/orbs-authoring.md
- https://circleci.com/docs/2.0/artifacts/
- https://circleci.com/blog/deep-diving-into-circleci-workspaces/
