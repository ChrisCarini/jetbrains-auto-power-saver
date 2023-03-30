<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Platform Plugin Template Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [3.0.0] - 2023-03-29
### Changed
- Upgrading IntelliJ from 2022.3.3 to 2023.1.0

## [2.8.3] - 2023-03-13
### Changed
- Upgrading IntelliJ from 2022.3.2 to 2022.3.3

## [2.8.2] - 2023-02-04
### Changed
- Upgrading IntelliJ from 2022.3.1 to 2022.3.2

## [2.8.1] - 2022-12-28
### Changed
- Upgrading IntelliJ from 2022.3 to 2022.3.1

## [2.8.0] - 2022-12-28
### Changed
- Upgrading IntelliJ from 2022.2.4 to 2022.3.0

## [2.7.1] - 2022-11-28
### Changed
- Upgrading IntelliJ from 2022.2 to 2022.2.4

## [2.7.0] - 2022-07-29
### Changed
- Upgrading IntelliJ to 2022.2

## [2.6.0] - 2022-04-14
### Changed
- Upgrading IntelliJ to 2022.1

## [2.5.2] - 2021-11-30
### Changed
- Upgrading IntelliJ to 2021.3

## [2.5.2-EAP] - 2021-11-14
### Removed
- Remove `description` from `plugin.xml` _(value is taken from `README.md` as part of Gradle `patchPluginXml` task)_
EOM)

## [2.5.1] - 2021-10-12
### Added
- Restructured file to extract all variables into file.
- Adding ability to publish to different channels based on SemVer pre-release labels.
- Adding [JetBrains Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
- Update Dependabot to include Gradle dependencies.
- Adding GitHub build & release workflows.
- Adding JetBrains Qodana (experimental, testing only)

### Changed
- Upgrading Gradle to 6.6
- Upgrading IntelliJ to 2021.2.2
- Upgrading IntelliJ Gradle plugin to 1.2.0

## [2.5.0] - 2021-07-30
### Changed
- Upgrading IntelliJ to 2021.2

## [2.4.1] - 2021-07-04
### Fixed
- <a href="https://github.com/ChrisCarini/jetbrains-auto-power-saver/issues/11">#11 - [Enhancement] Specify units for delay bars in preferences window.</a>

## [2.4.0] - 2021-04-10
### Changed
- Upgrading to 2021.1
- Upgrading IntelliJ Gradle plugin to 0.7.2

## [2.3.0] - 2020-12-04
### Changed
- Upgrading to 2020.3
- Upgrading IntelliJ Gradle plugin to 0.6.5
- Upgrading Java 11 - see [the JetBrains Platform blog post announcing the migration](https://blog.jetbrains.com/platform/2020/09/intellij-project-migrates-to-java-11/)

## [2.2.0] - 2020-07-29
### Changed
- Upgrading to 2020.2
- Upgrading IntelliJ Gradle plugin to 0.4.21

## [2.1.0] - 2020-04-09
### Changed
- Upgrading to 2020.1
- Upgrading IntelliJ Gradle plugin to 0.4.16
- Upgrading Gradle to 6.2

### Added
- GitHub Workflow Action for <a href="https://github.com/marketplace/actions/intellij-platform-plugin-verifier">IntelliJ Platform Plugin Verifier</a>

## [1.1.0] - 2019-11-28
### Changed
- Upgrading to 2019.3.

### Fixed
- <a href="https://github.com/ChrisCarini/jetbrains-auto-power-saver/issues/4">#4</a> - "Must be executed on UI thread" exception when alt+tabbing into the IDE

## [1.0.2] - 2019-10-02
### Changed
- Upgrading to 2019.2.3.

## [1.0.1] - 2019-08-27
### Changed
- Upgrading to 2019.2.1.

## [1.0.0] - 2019-07-25
### Changed
- Upgrading to 2019.2.

## [0.0.5] - 2019-07-24
### Changed
- Upgrading to 2019.1.3.

### Fixed
- Fixing bug for non-`en` locales - No generic resource bundle causes stack trace for non-`en` languages.

## [0.0.4] - 2019-04-01
### Added
- Adding an icon for use in 2019.1 release.

### Changed
- Upgrading for 2019.1.

## [0.0.2] - 2019-02-04
### Added
- This works for any 2018.3 release; there seems to be a bug with the release that locked it to 2018.3.4 and above.

## [0.0.1] - 2019-01-31
### Added
- Initial release.