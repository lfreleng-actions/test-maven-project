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

| Item               | Value                                                   |
| ------------------ | ------------------------------------------------------- |
| Java               | `maven.compiler.release` 17 (builds on JDK 17 or newer) |
| Tests              | JUnit 6 (`junit-bom` 6.1.3, test scope)                 |
| Runtime dependency | Jackson (`jackson-bom` 2.22.2, compile scope)           |
| Coverage           | JaCoCo 0.8.15, XML report per module                    |

The parent POM declares the Java version as `maven.compiler.release`
rather than `maven.compiler.source`/`target`, matching modern practice.

## Why a compile-scope dependency

`core` depends on `jackson-databind` at compile scope, declared **with no
version of its own** — the version arrives from the `jackson-bom` import
in the parent POM. This is not decoration. The declaration is what makes
the fixture usable for SBOM and vulnerability-scanning tests.

A test-scoped dependency cannot serve that purpose, and the two kinds of
generator fail it differently. Because test dependencies are absent from
the shipped artefact, a resolved-graph generator such as
`cyclonedx-maven-plugin` excludes them by default, so a fixture carrying
nothing else yields a BOM with no third-party components at
all. A static scan of the source tree does list the test dependency, but
reports its BOM-managed version as `UNKNOWN`, which is unscannable for a
different reason. Either way there is nothing worth measuring.

The declaration exercises two behaviours at once:

- **Transitive resolution.** One declared coordinate resolves to three
  components: `jackson-databind`, plus `jackson-core` and
  `jackson-annotations` beneath it.
- **BOM-managed version resolution.** The version materialises when Maven
  processes the imported BOM. That BOM also pins the three artifacts at
  *different* versions (`jackson-databind` 2.22.2, `jackson-annotations`
  2.22), so a tool that guesses one version for the whole family gets it
  wrong in a visible way.

Measured against this fixture, the difference between reading `pom.xml`
as text and driving Maven's own resolver:

<!-- markdownlint-disable MD013 MD060 -->

|                                             | Third-party components | Versions resolved     | Transitives |
| ------------------------------------------- | ---------------------- | --------------------- | ----------- |
| Static scan of the source tree              | 2                      | none — both `UNKNOWN` | ❌          |
| `cyclonedx-maven-plugin` `makeAggregateBom` | 3                      | all                   | ✅          |

<!-- markdownlint-enable MD013 MD060 -->

A vulnerability database cannot match a component whose version reads
`UNKNOWN`, so that component escapes scanning altogether rather than
losing precision.

Compile scope also makes Jackson transitive to `app`, giving the BOM a
real dependency graph to express:
`app` → `core` → `jackson-databind` → `jackson-core`.

## Usage

Build and test:

```console
mvn clean verify
```

That runs 10 tests across the two code modules and writes, per module:

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
