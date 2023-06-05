## Rewrite Jenkins

[Jenkins][jenkins] is a fast-moving project that recommends plugins [update quarterly][choosing-version].
This repository contains [OpenRewrite][open-rewrite] recipes that aim to automate these regular updates with [Moderne][moderne].

As Jenkins evolves, functionality is regularly factored out from core into plugins.
Implicit dependencies are added to plugins based on the version of Jenkins they depend on to retain backwards compatibility.
By regularly marching plugins forward, we can drop these dependencies when unnecessary, making our deployments smaller and simpler.

Additionally, depending on newer versions of Jenkins allows our builds to pick up deprecations.
These deprecations can often be migrated with new OpenRewrite recipes.
Proactively handling deprecations in the plugins we depend on allows us to avoid runtime issues if and when the deprecated code is removed.

[jenkins]: https://jenkins.io
[choosing-version]: https://www.jenkins.io/doc/developer/plugin-development/choosing-jenkins-baseline/
[open-rewrite]: https://docs.openrewrite.org/
[moderne]: https://www.moderne.io/
