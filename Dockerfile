FROM openjdk:8 as build

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
    nowsecure-auto /usr/local/bin

