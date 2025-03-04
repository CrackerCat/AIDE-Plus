<p align="center">
  <img src=".idea/icon.svg" style="width: 30%;" />
</p>

> [!TIP]
> This project can only be self-hosted on AIDE+
> If you want to use Gradle, please visit the Gradle project


- [中文](README_zh.md)
- [Gradle Project](https://github.com/neu233/AIDE-Plus)

# AIDE-Plus

## Repository Information
[![GitHub contributors](https://img.shields.io/github/contributors/AndroidIDE-CN/AIDE-Plus)](https://github.com/AndroidIDE-CN/AIDE-Plus/graphs/contributors)
[![GitHub last commit](https://img.shields.io/github/last-commit/AndroidIDE-CN/AIDE-Plus)](https://github.com/AndroidIDE-CN/AIDE-Plus/commits/)
[![Repository Size](https://img.shields.io/github/repo-size/AndroidIDE-CN/AIDE-Plus)](https://github.com/AndroidIDE-CN/AIDE-Plus)
[![GitHub Release](https://img.shields.io/github/v/release/AndroidIDE-CN/AIDE-Plus)](https://github.com/AndroidIDE-CN/AIDE-Plus/releases)
[![Total downloads](https://img.shields.io/github/downloads/AndroidIDE-CN/AIDE-Plus/total)](https://github.com/AndroidIDE-CN/AIDE-Plus/releases)

## Implemented Features
- [x] Build service optimization and rewrite
- [x] Replaced aapt with aapt2
- [x] Replaced dx with D8
- [x] Implementation of some Java 8 syntax (default syntax analysis)
- [x] Implementation of some Java 9 syntax (default syntax analysis)
- [x] Implementation of some Java 11 syntax (default syntax analysis)
- [x] Java 23 compilation (implemented through ecj, needs to be enabled in settings)
- [x] High version Java formatting (implemented through ecj, needs to be enabled in settings)
- [x] Custom class parser to implement high version class features
- [x] Implemented `runtimeOnly`, `compileOnly`, `libgdxNatives`
- [x] Rewritten Gradle parser
- [x] New Maven downloader (bom supported)
- [x] Application cold start optimization
- [x] Added more syntax highlighting
- [x] Using D8 for obfuscation
- [x] Replaced AIDE default framework with Androidx
- [x] Restored some of AIDE's dex obfuscation
- [x] Custom code colors
- [x] ViewBinding support
- [x] DataBinding support
- [x] Manifest merger tool update
- [x] Fixed silent installation and support for more installers (shizuku)
- [x] New UI implementation
- [x] Fixed completion and highlighting loss issues
- [x] Fixed signature creation issues
- [x] ApkSign now supports v1-v3 signatures
- [x] Added some basic syntax completions
- [x] Fixed git issues within the software
- [x] APK resource alignment
- [x] Java projects support Android API
- [x] Lambda implementation (ecj) [fbf450d](https://github.com/AndroidIDE-CN/AIDE-Plus/commit/fbf450dba15ccaf51a7a6dd77db300d50551e98b)
- [x] CMake build support [e702347](https://github.com/AndroidIDE-CN/AIDE-Plus/commit/e702347df0c10b718df5aeb4798402802334e310)
- [x] Modified XML completion logic [0ecb637](https://github.com/AndroidIDE-CN/AIDE-Plus/commit/f7960418b9326231d55726514f10385396e9e8b6)
- [x] Support exclude (exclude group: "xxx", module: "xxx")


### Planned Features
- [ ] More completions (LSP)
- [ ] Apks/AAB generation (not added)
- [ ] Manifest file editing
- [ ] Vector graphics acquisition
- [ ] Layout visualization rewrite

## Related Resources
- [AIDE-Ndk-Install](https://github.com/ZeroAicy/AIDE-Ndk-Install) NDK Installer
- [AIDE-Repair](https://github.com/ZeroAicy/AIDE-Repair) Deobfuscation

# Related Information
- QQ Groups
  * [487145957](https://qm.qq.com/q/W0WJq5qne2)
  * [1002980489](https://qm.qq.com/q/W0WJq5qne2)
- [QQ Channel](https://pd.qq.com/s/auq589py2)
- [Website](https://plus.androidide.cn)

# Special Thanks
> [!TIP]
> If you or your project is not listed, please contact us to add it

### Project Related
- [@墨凡尘轩](https://github.com/ZeroAicy) Project Founder
- [@烂泥扶上墙](https://github.com/eirv) Project Collaborator
- [@原罪](https://github.com/neu233) Project Collaborator
- [@0047ol](https://github.com/0047ol) Project Collaborator
- [@hujiayucc](https://github.com/hujiayucc) Project Collaborator
- [@dev-bz](https://github.com/dev-bz) (不知者来此) Provided assistance

### Open Source Integration
- [Gradle Project](https://github.com/neu233/AIDE-Plus) Android Studio version
- [AndroidIDE](https://github.com/AndroidIDEOfficial/AndroidIDE) Referenced some code
- [CodeAssist](https://github.com/tyron12233/CodeAssist) Referenced some code
- [Termux-app](https://github.com/termux/termux-app) Integrated into software
- [AIDE-Pro](https://github.com/AndroidIDE-CN/) UI reference
