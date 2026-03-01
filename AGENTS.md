# AGENTS.md

## Project overview for newcomers
RxReactor is a multi-module Gradle/Kotlin codebase that implements the ReactorKit architecture pattern for multiple RxJava generations.

### Repository structure
- `rxreactor1`, `rxreactor2`, `rxreactor3`: core library modules targeting RxJava 1/2/3.
- `rxreactor1-android`, `rxreactor2-android`, `rxreactor3-android`: Android-specific extensions that enforce main-thread scheduling for streams.
- `sample`: Android sample app (RxJava 2) showing end-to-end usage.
- `config/detekt`: static analysis configuration.
- `.github/workflows`: CI and release workflows.
- `gradle/libs.versions.toml`: centralized dependency/version catalog.

### Important concepts
- `Reactor`: central state-management primitive (actions -> mutations -> state).
- `ReactorWithEffects`: separates side-effects from state transitions.
- `SimpleReactor`: simplified variant where actions are treated as mutations.
- Tests in each module are the best executable specification for expected behavior.

### Recommended learning path
1. Read module tests under `*/src/test` first.
2. Walk through the `sample` app to see real integration with Android UI.
3. Read `README.md` / release docs / workflows to understand publishing and maintenance.
4. Run `./gradlew check` locally to validate formatting, static analysis, and tests.

## Code style instructions
- Keep code Kotlin-idiomatic and concise; prefer immutable data/state where possible.
- Match existing naming and package conventions in each module.
- Follow Detekt rules configured in `config/detekt`.
- Prefer small, focused classes and functions with clear reactive pipelines.
- Add or update tests for behavior changes.

## Unit test instructions
- Test frameworks and style:
  - Use JUnit 4 (`org.junit.Test`) and RxJava `TestObserver` like the existing tests in `rxreactor1`, `rxreactor2`, `rxreactor3`, and `sample`.
  - Use descriptive test names in backticks and keep the Arrange / Act / Assert structure with inline section comments.
- Coverage expectations for reactor changes:
  - Validate state progression including the initial replayed state.
  - Validate error/completion resilience when mutation streams emit terminal events.
  - For effect-capable reactors, assert `state` and `effect` streams independently.
- Scope and placement:
  - Add tests in the matching module under `src/test` with package paths that mirror production code.
  - If behavior should be consistent across RxJava versions, update equivalent tests across affected modules (`rxreactor1`, `rxreactor2`, `rxreactor3`).
- Assertion guidance:
  - Prefer strict value assertions (`assertValues`, `assertValue`) and no-error checks (`assertNoErrors`) over loose assertions.
  - Keep test reactors minimal and local to the test file unless shared reuse clearly improves readability.
- Execution:
  - Run targeted module tests during development, then run `./gradlew check` before finalizing substantial changes.

## File header instructions
When creating a new source/resource file in this repository, add the existing BSD-3-Clause project header style used across modules.

Use this header template for Kotlin/Java files:

```kotlin
/*
 * Copyright (c) <YEAR>, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */
```

For XML files, use the XML comment equivalent in the same wording and order.

If editing an existing file, preserve the current header style and update only when necessary for consistency.
