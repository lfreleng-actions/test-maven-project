<!--
SPDX-License-Identifier: Apache-2.0
SPDX-FileCopyrightText: 2026 The Linux Foundation
-->

# 📦 Test Maven Project

Sample multi-module Maven project used for testing actions.

This repository is a **fixture**: it exists so that GitHub Actions and
reusable workflows have a small, fast, predictable Java project to build,
test and scan. Nothing here ships as a published artefact.

## Why multi-module

A single-module project would not exercise the behaviour that matters.
The layout mirrors real Linux Foundation projects such as
ONAP `cps`:

```text
pom.xml            aggregator - no parent, no compiler property
parent/            holds the toolchain, dependency and plugin config
core/              library module
app/               depends on core, so the reactor has a build order
```

Two consequences worth knowing:

- Build metadata tooling running at the repository root finds **no** Java
  version in the aggregator POM, and has to resolve it from the parent
  module. This is the case that naive detection gets wrong.
- `app` depends on `core`, so the modules cannot build independently.

## Toolchain

| Item | Value |
| ---- | ----- |
| Java | `maven.compiler.release` 17 (builds on JDK 17 or newer) |
| Tests | JUnit 5 (`junit-bom` 5.14.4) |
| Coverage | JaCoCo 0.8.15, XML report per module |

The parent POM declares the Java version as `maven.compiler.release`
rather than `maven.compiler.source`/`target`, matching modern practice.

## Usage

Build and test:

```console
mvn clean verify
```

That runs 9 tests across the two code modules and writes, per module:

- JUnit XML to `<module>/target/surefire-reports/`
- JaCoCo coverage XML to `<module>/target/site/jacoco/jacoco.xml`

A full build completes in under ten seconds, which keeps action test
workflows fast.

### Producing a deliberate test failure

The build excludes tests named `Failing*Test`. Activate the
`failing-tests` profile to include them and get a build that fails during
the test phase:

```console
mvn clean verify -P failing-tests
```

Use this to exercise failure handling: soft-fail inputs, test report
rendering, and quality gates. The failures are assertion failures in
`core`, so the reactor stops before `app`.

## SonarQube analysis

Both analysis paths work against this project, which is deliberate:

- **Maven mode** (`sonar-maven-plugin`) derives sources, tests, binaries
  and coverage paths from the Maven project model. It needs no extra
  configuration.
- **Scanner CLI** has no project model, so `sonar-project.properties`
  spells out the same information.

Keeping both working means one fixture covers both backends.

## Version metadata

`version.properties` carries `major`/`minor`/`patch` in the flat form
used by Linux Foundation release tooling, alongside the POM version
`1.0.0-SNAPSHOT`. Metadata detection thus has a realistic pair to
reconcile, where the POM carries the `-SNAPSHOT` suffix and the
properties file does not.
