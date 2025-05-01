<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-jenkins</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/openrewrite/rewrite-jenkins/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite-jenkins/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite.recipe/rewrite-jenkins.svg)](https://mvnrepository.com/artifact/org.openrewrite.recipe/rewrite-jenkins)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.openrewrite.org/scans)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
</div>

## What is this?

This project contains a series of Rewrite recipes and visitors to automatically upgrade and migrate Jenkins plugins.

[Jenkins][jenkins] is a fast-moving project that recommends plugins [update quarterly][choosing-version].
As Jenkins evolves, functionality is regularly factored out from core into plugins.
Implicit dependencies are added to plugins based on the version of Jenkins they depend on to retain backwards compatibility.
By regularly marching plugins forward, we can drop these dependencies when unnecessary, making our deployments smaller and simpler.

Additionally, depending on newer versions of Jenkins allows our builds to pick up deprecations.
These deprecations can often be migrated with new OpenRewrite recipes.
Proactively handling deprecations in the plugins we depend on allows us to avoid runtime issues if and when the deprecated code is removed.

## Quick start

[Running Rewrite on a Maven project without modifying the build][mvn-cli] is very helpful for getting started.
To run the `org.openrewrite.jenkins.github.AddTeamToCodeowners` recipe:

```shell
$ mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
      -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-jenkins:RELEASE \
      -Drewrite.activeRecipes=org.openrewrite.jenkins.github.AddTeamToCodeowners
```

[mvn-cli]: https://docs.openrewrite.org/running-recipes/running-rewrite-on-a-maven-project-without-modifying-the-build

## How to use?

See the full documentation at [docs.openrewrite.org](https://docs.openrewrite.org/).

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

### Licensing

For more information about licensing, please visit our [licensing page](https://docs.openrewrite.org/licensing/openrewrite-licensing).

[jenkins]: https://jenkins.io
[choosing-version]: https://www.jenkins.io/doc/developer/plugin-development/choosing-jenkins-baseline/
