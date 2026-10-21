#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for Unix / Git Bash (NUEVOLOOKSR Backend)
# ----------------------------------------------------------------------------

# Ensure JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/c/Program Files/Java/jdk-21.0.13" ]; then
        export JAVA_HOME="/c/Program Files/Java/jdk-21.0.13"
    elif [ -d "C:/Program Files/Java/jdk-21.0.13" ]; then
        export JAVA_HOME="C:/Program Files/Java/jdk-21.0.13"
    fi
fi
if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

MAVEN_VERSION="3.9.9"
MAVEN_DIR="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION"
MAVEN_CMD="$MAVEN_DIR/bin/mvn"


if [ -x "$MAVEN_CMD" ]; then
    exec "$MAVEN_CMD" "$@"
fi

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

mkdir -p "$HOME/.m2/wrapper"
MAVEN_ZIP="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"

if [ ! -f "$MAVEN_ZIP" ]; then
    echo "[NUEVOLOOKSR] Descargando Apache Maven $MAVEN_VERSION..."
    curl -fsSL "$MAVEN_URL" -o "$MAVEN_ZIP" || wget "$MAVEN_URL" -O "$MAVEN_ZIP"
fi

echo "[NUEVOLOOKSR] Descomprimiendo Maven..."
unzip -qo "$MAVEN_ZIP" -d "$HOME/.m2/wrapper"
chmod +x "$MAVEN_CMD"

exec "$MAVEN_CMD" "$@"
