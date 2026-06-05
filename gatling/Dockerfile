# Gatling (Java DSL) image — standalone bundle, compiles the simulation at run time.
FROM eclipse-temurin:17-jdk-jammy

ARG GATLING_VERSION=3.10.5
ENV GATLING_HOME=/opt/gatling

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends wget unzip ca-certificates; \
    rm -rf /var/lib/apt/lists/*; \
    wget -q "https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/${GATLING_VERSION}/gatling-charts-highcharts-bundle-${GATLING_VERSION}-bundle.zip" -O /tmp/gatling.zip; \
    unzip -q /tmp/gatling.zip -d /opt; \
    rm /tmp/gatling.zip; \
    mv "/opt/gatling-charts-highcharts-bundle-${GATLING_VERSION}" "${GATLING_HOME}"

WORKDIR ${GATLING_HOME}
COPY gatling/simulations ./user-files/simulations

ENTRYPOINT ["./bin/gatling.sh"]
