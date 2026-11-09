# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: 2026 The Linux Foundation
#
# Plain-Dockerfile image pattern: a packaged jar feeds an ordinary
# docker build. Run "mvn package" first, then build with app/ as the
# context. The image copies the module's jar and nothing else from the
# build, so it cannot run the application (core and Jackson are absent);
# its default command lists the jar, which proves from outside that the
# image holds it.

FROM eclipse-temurin:17.0.21_9-jre-alpine-3.24

COPY target/test-maven-app-*.jar /app/app.jar

CMD ["ls", "-l", "/app/app.jar"]
