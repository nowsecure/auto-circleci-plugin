FROM gradle:4.7.0-jdk8
#
WORKDIR /home/gradle

#
# plugin version from https://github.com/nowsecure/auto-circleci-plugin/releases
ARG plugin_version=1.2.0
#
# Default auto url
ARG auto_url=https://lab-api.nowsecure.com
# Show status messages
ARG show_status_messages=true
# Default max wait in minutes for the mobile analysis
ENV max_wait 30
# Minimum score the app must have otherwise it would fail
ENV min_score 50
# Specify auto token from your account
ENV auto_token default_token
# Specify group for your account
ENV auto_group default_group
# Path to Android apk or IOS ipa - this file must be mounted via volume for the access
ENV binary_file default_binary
#
# artifacts directory where json files are stored
ENV artifacts_dir /home/gradle/artifacts
#
# Copying arg to environment so that we can access them in entrypoint
ENV plugin_version $plugin_version
ENV auto_url $auto_url
ENV show_status_messages $show_status_messages
#
# Download release file
RUN curl -Ls https://github.com/nowsecure/auto-circleci-plugin/archive/${plugin_version}.tar.gz | tar -xzf - -C /home/gradle
#
WORKDIR /home/gradle/auto-circleci-plugin-${plugin_version}
#
# Execute gradle task

ENTRYPOINT java -jar dist/all-in-one-jar-1.2.0.jar --plugin-name circleci-nowsecure-auto-security-test --plugin-version 1.2.0 --auto-url $auto_url --auto-token $auto_token --auto-dir $artifacts_dir --auto-file $binary_file --auto-group $auto_group --auto-wait $max_wait --auto-score $min_score --auto-show-status-messages $show_status_messages --debug


## EXAMPLE FOR EXECUTING DOCKER IMAGE
# docker run -v ~/Desktop/apk:/source -v /tmp:/artifacts -e auto_token=$TOKEN -e auto_group=$GROUP -e binary_file=/source/test.apk -e artifacts_dir=/artifacts -e max_wait=30 -e min_score=50 -it --rm $IMAGE_ID
# If min_score is higher than zero and security score is below that number, the docker job would fail, e.g.
# java.io.IOException: Test failed because score (45.0) is lower than threshold 50
