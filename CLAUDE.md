# RxReactor — Claude Notes

## Working style

Always confirm with the user before making any changes.

## Danger / danger-kotlin notes

The project uses `danger/kotlin@1.3.4` (Docker-based) via `.github/Dangerfile.df.kts`.

Plugins in use:
- `io.github.ackeecz:danger-kotlin-lint` — detekt inline comments (requires `basePath` set in root `build.gradle` detekt block)
- `com.gianluz:danger-kotlin-android-lint-plugin` — Android lint inline comments
- `io.github.ackeecz:danger-kotlin-testing` — JUnit test result reporting

Kotlin compiler warnings are parsed from `build.log` (captured via `tee` in CI) using the
K2 compiler format: `w: <file>:<line>:<col> <message>`.

The `danger/kotlin` action uses the built-in `GITHUB_TOKEN`; no separate PAT is required.
