# Production Releases

1. Checkout `origin/main`.
1. Update the `CHANGELOG.md` file with the changes of this release.
1. Update the version in the main `gradle.properties`.
1. Commit the changes and create a tag:
   ```
   git commit -am "Releasing version 1.0.0."
   git tag 1.0.0
   ```
1. Create the release on GitHub:
   1. Go to the [Releases](https://github.com/square/anvil/releases) page for the GitHub project.
   1. Click "Draft a new release".
   1. Enter the tag name you just pushed.
   1. Title the release with the same name as the tag.
   1. Copy & paste the changelog entry for this release into the description.
   1. If this is a pre-release version, check the pre-release box.
   1. Hit "Publish release".
   1. Wait for the GitHub Action job to complete; it will attach the artifacts to the release.
1. Close and release the staging repository at [Sonatype](https://oss.sonatype.org/#stagingRepositories).
1. Update the version in `gradle.properties`
1. Commit the change:
   ```
   git commit -am "Prepare next development version."
   ```
1. Push git changes:
   ```
   git push && git push --tags
   ```

# Installing in Maven Local

```
./gradlew clean publishToMavenLocal --no-build-cache
```

# Notes

## Snapshot Releases

Builds by default are snapshot releases and the version automatically contains the `-SNAPSHOT` suffix. 
You can verify the release [here](https://oss.sonatype.org/content/repositories/snapshots/com/gyurigrell/rxreactor/).
