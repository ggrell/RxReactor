NOTE: This is ![stability-WIP](https://img.shields.io/badge/stability-WIP-orange.svg) pre-release code, use at your own risk. 
 
# RxReactor
[![Build Status](https://travis-ci.org/ggrell/RxReactor.svg?branch=master)](https://travis-ci.org/ggrell/RxReactor) 
[![Coverage Status](https://coveralls.io/repos/github/ggrell/RxReactor/badge.svg?branch=master)](https://coveralls.io/github/ggrell/RxReactor?branch=master)  
[![Release](https://jitpack.io/v/ggrell/RxReactor.svg)](https://jitpack.io/#ggrell/RxReactor) 
[Javadocs](https://jitpack.io/com/github/ggrell/RxReactor/rxreactor1/master-SNAPSHOT/javadoc/)

RxReactor is a framework for a reactive and unidirectional RxJava-based application architecture. 
This repository introduces the basic concept of RxReactor and describes how to build an application 
using it.

## Usage

TODO: Usage stuff

## Download

Snapshots and releases available courtesy JitPack.io:

**Snapshot**
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor1:master-SNAPSHOT'
```

**Release**
```groovy
compile 'com.github.ggrell.RxReactor:rxreactor1:master'
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
