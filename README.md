# LifeSteal

Paper 26.3 LifeSteal plugin targeting Java 25.

The project has no runtime dependencies other than the Paper API. GitHub Actions builds it automatically with Temurin 25 and uploads the plugin JAR as an artifact.

LifeSteal is enabled only in the configured worlds (default: LifeSteal). LifeSteal state is stored in the configured LifeSteal world's lifesteal-data directory; non-LifeSteal state is stored in the plugin's outside-state directory.