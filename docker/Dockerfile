# https://github.com/home-assistant/docker-base/blob/master/ubuntu/Dockerfile

ARG BUILD_FROM
# amd64: adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.0.14.1_1-slim
# aarch64: adoptopenjdk/openjdk11:aarch64-ubuntu-jdk-11.0.14.1_1-slim
# armv7: adoptopenjdk/openjdk11:armv7l-ubuntu-jdk-11.0.14.1_1-slim
FROM ${BUILD_FROM}

# Default ENV
ENV \
    LANG="C.UTF-8" \
    DEBIAN_FRONTEND="noninteractive" \
    S6_BEHAVIOUR_IF_STAGE2_FAILS=2 \
    S6_CMD_WAIT_FOR_SERVICES=1

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Version
ENV BASHIO_VERSION="0.14.3" \
  TEMPIO_VERSION="2021.09.0" \
  S6_OVERLAY_VERSION="2.1.0.2"

# Base system
WORKDIR /usr/src

# Copy data for add-on
COPY docker/run.sh /
RUN chmod a+x /run.sh

ARG BUILD_ARCH

RUN \
    set -x \
    && apt-get update && apt-get install -y --no-install-recommends \
        bash \
        jq \
        tzdata \
        curl \
        ca-certificates \
    \
    && if [ "${BUILD_ARCH}" = "armv7" ]; then \
            export S6_ARCH="arm"; \
        elif [ "${BUILD_ARCH}" = "i386" ]; then \
            export S6_ARCH="x86"; \
        else \
            export S6_ARCH="${BUILD_ARCH}"; \
        fi \
    \
    && curl -L -f -s "https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-${S6_ARCH}.tar.gz" \
        | tar zxvf - -C / \
    && mkdir -p /etc/fix-attrs.d \
    && mkdir -p /etc/services.d \
    \
    && curl -L -f -s -o /usr/bin/tempio \
        "https://github.com/home-assistant/tempio/releases/download/${TEMPIO_VERSION}/tempio_${BUILD_ARCH}" \
    && chmod a+x /usr/bin/tempio \
    \
    && mkdir -p /usr/src/bashio \
    && curl -L -f -s "https://github.com/hassio-addons/bashio/archive/v${BASHIO_VERSION}.tar.gz" \
        | tar -xzf - --strip 1 -C /usr/src/bashio \
    && mv /usr/src/bashio/lib /usr/lib/bashio \
    && ln -s /usr/lib/bashio/bashio /usr/bin/bashio \
    \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/src/*

#SHELL ["/bin/sh", "-c"]

# S6-Overlay
WORKDIR /
ENTRYPOINT ["/init"]

# Copy application jar
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} /app.jar

CMD ["/run.sh"]