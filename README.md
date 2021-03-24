NOTE: This is ![stability-WIP](https://img.shields.io/badge/stability-WIP-orange.svg) pre-release code, use at your own risk. 
 
# RxReactor
[![Master Build](https://github.com/ggrell/RxReactor/actions/workflows/merge_master.yml/badge.svg)](https://github.com/ggrell/RxReactor/actions/workflows/merge_master.yml)
[![Coverage](https://codecov.io/gh/ggrell/RxReactor/branch/main/graph/badge.svg?token=8JHGJPU2M8)](https://codecov.io/gh/ggrell/RxReactor)
[![Release](https://jitpack.io/v/ggrell/RxReactor.svg)](https://jitpack.io/#ggrell/RxReactor) [![Javadocs](https://img.shields.io/badge/documentation-Javadocs-brightgreen)](https://jitpack.io/com/github/ggrell/RxReactor/rxreactor1/master-SNAPSHOT/javadoc/)

RxReactor is a framework for a reactive and unidirectional RxJava-based application architecture. 
This repository introduces the basic concept of RxReactor and describes how to build an application 
using it.

## Usage

TODO: Usage stuff

## Download

Snapshots and releases currently available courtesy [JitPack.io](https://jitpack.io):
```groovy
subprojects {
    repositiories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
Snapshots and releases will soon be released directly to Maven Central.

**Snapshot**

For RxJava 1:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor1:master-SNAPSHOT'
```
or for RxJava 2:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor2:master-SNAPSHOT'
```
or for RxJava 3:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor3:master-SNAPSHOT'
```

**Release**

For RxJava 1:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor1:$version'
```
For RxJava 1 on Android (optional):
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor1-android:$version'
```

or for RxJava 2:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor2:$version'
```
For RxJava 2 on Android (optional):
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor2-android:$version'
```

or for RxJava 3:
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor3:$version'
```
For RxJava 3 on Android (optional):
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor3-android:$version'
```

## Demo Projects

The repo currently contains a simple login test app with lookup of existing emails on the device.
The `LoginViewModel` handles loading on-device email addresses for lookup as the user is typing.

## Contributing

TBD

## License

[BSD 3-Clause License](https://github.com/ggrell/RxReactor/blob/master/LICENSE)

## Credits

Port of https://github.com/ReactorKit/ReactorKit to Kotlin
