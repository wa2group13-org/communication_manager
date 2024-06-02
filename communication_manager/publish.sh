#!/bin/bash

set -e
set -u

source ./secrets.sh

VERSION=$(./gradlew -q projectVersion)

git tag "v$VERSION"
git push origin tag "v$VERSION"

# Push image to DockerHub
./gradlew bootBuildImage --publishImage --info

# Get JTW token from DockerHub
RES=$(jq -n --arg u "$DOCKER_USERNAME" --arg p "$DOCKER_PASSWORD" '{"username":$u, "password":$p}' | curl -X POST  -d @- -H "Content-Type: application/json" https://hub.docker.com/v2/users/login)
JWT=$(jq -r '.token' <<<"$RES")

# Update container description
jq -n --arg d "$(<DOCKER.md)" '{"full_description":$d}' | curl -X PATCH -d @- -H "Content-Type: application/json" -H "Authorization: JWT $JWT" https://hub.docker.com/v2/repositories/wa2group13/communication_manager
