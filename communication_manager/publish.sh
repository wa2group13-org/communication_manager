#!/bin/bash

set -e
set -u

source ./secrets.sh

VERSION=$(./gradlew -q projectVersion)

git tag "v$VERSION"
git push origin tag "v$VERSION"

./gradlew bootBuildImage --publishImage --info
