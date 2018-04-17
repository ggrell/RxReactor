#!/bin/bash

SLUG="gyurigrell/rxreactor"
JDK="oraclejdk8"

set -e
if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping upload: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  echo "Skipping upload: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping upload: was pull request."
elif [ -z "$BINTRAY_USER" ]; then
  echo "Skipping upload: Expected BINTRAY_USER to be set."
elif [ -z "$BINTRAY_KEY" ]; then
  echo "Skipping upload: Expected BINTRAY_KEY to be set."
elif [ "$TRAVIS_BRANCH" = "master" ]; then
  echo "Uploading SNAPSHOT build to oss.jfrog.org..."
  ./gradlew clean assemble publishSdkPublicationToSnapshotRepository
  echo "Build uploaded!"
elif [[ $TRAVIS_BRANCH =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Uploading RELEASE build to Bintray..."
  ./gradlew clean assemble githubPublish bintrayUpload -Prelease=true
  echo "Build uploaded!"
else
  echo "Nothing to do"
fi
