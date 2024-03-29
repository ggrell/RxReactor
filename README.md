# RxReactor
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gyurigrell.rxreactor/rxreactor1/badge.svg)](https://search.maven.org/search?q=g:%22com.gyurigrell.rxreactor%22) [![Master Build](https://github.com/ggrell/RxReactor/actions/workflows/merge_master.yml/badge.svg)](https://github.com/ggrell/RxReactor/actions/workflows/merge_master.yml)
[![Coverage](https://codecov.io/gh/ggrell/RxReactor/branch/main/graph/badge.svg?token=8JHGJPU2M8)](https://codecov.io/gh/ggrell/RxReactor)
 [![RxJava 1 javadocs](https://img.shields.io/badge/Javadocs-RxJava%201-green)](https://jitpack.io/com/github/ggrell/RxReactor/rxreactor1/main-SNAPSHOT/javadoc/) [![RxJava 2 javadocs](https://img.shields.io/badge/Javadocs-RxJava%202-green)](https://jitpack.io/com/github/ggrell/RxReactor/rxreactor2/main-SNAPSHOT/javadoc/) [![RxJava 3 javadocs](https://img.shields.io/badge/Javadocs-RxJava%203-green)](https://jitpack.io/com/github/ggrell/RxReactor/rxreactor3/main-SNAPSHOT/javadoc/)

RxReactor is a Kotlin framework for a reactive and unidirectional RxJava-based application architecture. 
This repository introduces the basic concept of RxReactor and describes how to build an application 
using it. It is available to using with Kotlin on any JVM as well as Android.

## Usage

TODO: Usage stuff

## Download

Releases are published to Maven Central, and individual archives are also available in the 
[Releases](https://github.com/ggrell/RxReactor/releases) for the project.

```groovy
subprojects {
    repositiories {
        mavenCentral()
    }
}
```

Add this repository to have access to Maven Central snapshots:
```groovy
subprojects {
    repositiories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
            mavenContent { snapshotsOnly() }
        }
    }
}
```

For RxJava 1:
```groovy
compile 'com.gyurigrell.rxreactor:rxreactor1:1.0.0' // Add -SNAPSHOT for snapshot versions
compile 'com.gyurigrell.rxreactor:rxreactor1-android:1.0.0' // Optional, add -SNAPSHOT for snapshot versions
```
or for RxJava 2:
```groovy
compile 'com.gyurigrell.rxreactor:rxreactor2:1.0.0' // Add -SNAPSHOT for snapshot versions
compile 'com.gyurigrell.rxreactor:rxreactor2-android:1.0.0' // Optional, add -SNAPSHOT for snapshot versions
```
or for RxJava 3:
```groovy
compile 'com.gyurigrell.rxreactor:rxreactor3:1.0.0' // Add -SNAPSHOT for snapshot versions
compile 'com.gyurigrell.rxreactor:rxreactor3-android:1.0.0' // Optional, add -SNAPSHOT for snapshot versions
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
