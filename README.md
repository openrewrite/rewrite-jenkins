![Logo](https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss.png)
### Modernize Jenkins plugins. Automatically.

[![ci](https://github.com/openrewrite/rewrite-jenkins/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite-jenkins/actions/workflows/ci.yml)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-jenkins.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite.recipe/rewrite-jenkins.svg)](https://mvnrepository.com/artifact/org.openrewrite.recipe/rewrite-jenkins)
[![Revved up by Gradle Enterprise](https://img.shields.io/badge/Revved%20up%20by-Gradle%20Enterprise-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.openrewrite.org/scans)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)

## What is this?

This project contains a series of Rewrite recipes and visitors to automatically upgrade and migrate Jenkins plugins.

[Jenkins][jenkins] is a fast-moving project that recommends plugins [update quarterly][choosing-version].
As Jenkins evolves, functionality is regularly factored out from core into plugins.
Implicit dependencies are added to plugins based on the version of Jenkins they depend on to retain backwards compatibility.
By regularly marching plugins forward, we can drop these dependencies when unnecessary, making our deployments smaller and simpler.

Additionally, depending on newer versions of Jenkins allows our builds to pick up deprecations.
These deprecations can often be migrated with new OpenRewrite recipes.
Proactively handling deprecations in the plugins we depend on allows us to avoid runtime issues if and when the deprecated code is removed.

## How to use?

See the full documentation at [docs.openrewrite.org](https://docs.openrewrite.org/).

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.


[jenkins]: https://jenkins.io
[choosing-version]: https://www.jenkins.io/doc/developer/plugin-development/choosing-jenkins-baseline/
