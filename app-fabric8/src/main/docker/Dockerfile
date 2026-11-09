# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: 2026 The Linux Foundation
#
# Built by the fabric8 docker-maven-plugin from app-fabric8/pom.xml.
# The plugin's assembly places the app jar and its runtime dependencies
# in maven/. The jar's manifest sets no Main-Class, so the entry point
# names the class on an explicit class path.

FROM eclipse-temurin:17.0.21_9-jre-alpine-3.24

COPY maven/ /app/lib/

ENTRYPOINT ["java", "-cp", "/app/lib/*", "org.lfreleng.test.app.App"]
