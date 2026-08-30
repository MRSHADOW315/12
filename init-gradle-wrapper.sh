#!/bin/bash

# This script initializes the Gradle wrapper
# It downloads gradle-wrapper.jar if it doesn't exist

set -e

GRADLE_VERSION="8.9"
WRAPPER_DIR="gradle/wrapper"
JAR_FILE="$WRAPPER_DIR/gradle-wrapper.jar"
PROPERTIES_FILE="$WRAPPER_DIR/gradle-wrapper.properties"

# Ensure wrapper directory exists
mkdir -p "$WRAPPER_DIR"

# If gradle-wrapper.jar doesn't exist, we'll use gradle from system or create a basic setup
if [ ! -f "$JAR_FILE" ]; then
    echo "Downloading Gradle Wrapper JAR..."
    curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.zip -o /tmp/gradle-wrapper.zip
    unzip -j /tmp/gradle-wrapper.zip gradle-${GRADLE_VERSION}/lib/gradle-wrapper-${GRADLE_VERSION}.jar -d "$WRAPPER_DIR" 2>/dev/null || true
    if [ -f "$WRAPPER_DIR/gradle-wrapper-${GRADLE_VERSION}.jar" ]; then
        mv "$WRAPPER_DIR/gradle-wrapper-${GRADLE_VERSION}.jar" "$JAR_FILE"
    fi
    rm -f /tmp/gradle-wrapper.zip
fi

echo "Gradle wrapper initialization complete"
