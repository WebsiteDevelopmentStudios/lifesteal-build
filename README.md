# LifeSteal

Paper 26.2 and newer LifeSteal plugin targeting Java 25.

The project compiles against Paper API `26.2.build.143-beta` and declares `api-version: '26.2'` so Paper 26.2 can load it. The project has no runtime dependencies other than the Paper API. GitHub Actions builds it automatically with Temurin 25 and uploads the plugin JAR as an artifact.

LifeSteal is enabled only in the configured worlds (default: `LifeSteal`). LifeSteal state is stored in the configured LifeSteal world's `lifesteal-data` directory; non-LifeSteal state is stored in the plugin's `outside-state` directory.

## Build

Run `mvn -B clean package` with Java 25, or use the **Build LifeSteal** workflow under the repository's Actions tab. Download the `LifeSteal` artifact from a successful workflow run.