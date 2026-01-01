# ReTerraForged

a 1.19+ continuation of https://github.com/TerraForged/TerraForged

## Build / Release notes (Repo fixes)

This repository contains a multi-module Architectury + Loom setup:

- `common/` shared code/resources
- `fabric/` Fabric mod jar
- `forge/` Forge mod jar

### Important fixes applied in this repo

1) **ResourcePack info error (missing `pack.mcmeta`)**
- Symptoms: launcher/log shows something like *"failed to load valid ResourcePack info"* when loading the Forge jar.
- Fix: added `common/src/main/resources/pack.mcmeta` so the produced mod jar is a valid resource pack container.

2) **Avoid using removed vanilla field `Biome.BIOME_INFO_NOISE`**
- Symptoms: IDE/compiler warnings like *"BIOME_INFO_NOISE is deprecated and marked for removal"*.
- Fix: replaced its usage in `SwampSurfaceFeature` with a low-frequency noise based on this project's `Noises.simplex(...)`.

3) **Reduce "forRemoval" chain warnings on internal biome modifier API**
- `BiomeModifier` was annotated as `@Deprecated(forRemoval = true)` which can make many downstream references look like hard errors in some IDE setups.
- Fix: changed to plain `@Deprecated` (behavior unchanged).

4) **Registry typing restored for build stability**
- `RTFRegistries.BIOME_MODIFIER`, `RTFRegistries.BIOME_MODIFIER_TYPE`, and `RTFRegistries.STRUCTURE_RULE_TYPE` must stay strongly typed to avoid `RegistrySetBuilder` / `ResourceKey.create(...)` generic inference failures.

### Local build (Windows PowerShell)

```powershell
cd E:\PROJECT_JAVA\ReTerraForged
.\gradlew.bat --no-daemon clean build :fabric:remapJar :forge:remapJar
```

### Output jars

Final, playable jars are:

- Fabric: `fabric/build/libs/reterraforged-<mod_version>-fabric-<minecraft_version>.jar`
- Forge: `forge/build/libs/reterraforged-<mod_version>-forge-<minecraft_version>.jar`

### GitHub Actions

Workflows live in `.github/workflows/`.

- `build.yml`: CI build on PR/push (uploads jars as artifacts)
- `build-and-release.yml`: manual build + optional GitHub Release upload

Note: the workflows use **JDK 17** because the project uses Gradle 8.1; running the build with Java 21 in CI caused `Unsupported class file major version 65` during settings script analysis.
