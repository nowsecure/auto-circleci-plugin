<<<<<<< HEAD
FROM openjdk:8
# plugin version from https://github.com/nowsecure/auto-circleci-plugin/releases
ENV PLUGIN_VERSION 1.2.1


# Download nowsecure plugin source
RUN mkdir -p /usr/local/share/nowsecure
RUN curl -Ls https://github.com/nowsecure/auto-circleci-plugin/archive/${PLUGIN_VERSION}.tar.gz | tar -xzf - -C /usr/local/share/nowsecure
RUN cp /usr/local/share/nowsecure/auto-circleci-plugin-${PLUGIN_VERSION}/bin/nowsecure.sh /usr/local/bin/nowsecure.sh
=======
FROM openjdk:8 as build
>>>>>>> 8f7e91f (remove unwanted bins and jars, update dockerfile to bundle the entire plugin)

COPY . .
ENV JVM_OPTS -Xmx500m

RUN ./package.sh

FROM openjdk:8-jre

ENV APP_USER=javauser
ENV APP_DIR="/${APP_USER}"

RUN addgroup --gid 1000 ${APP_USER} \
 && adduser --disabled-login --disabled-password \
    ${APP_USER} --gid 1000 --uid 1000 --shell /bin/sh

WORKDIR ${APP_DIR}

COPY --from=build --chown=$APP_USER:$APP_USER \
    nowsecure-ci /usr/local/bin

