#!/bin/sh
# Maven Wrapper script - runs Maven without requiring global installation

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$SCRIPT_DIR}"
export MAVEN_PROJECTBASEDIR

# Resolve JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
  if [ -x "/usr/libexec/java_home" ]; then
    export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
  fi
fi
if [ -z "$JAVA_HOME" ]; then
  JAVACMD="$(command -v java 2>/dev/null)"
  if [ -n "$JAVACMD" ] && [ -x "$JAVACMD" ]; then
    JAVABIN="$(cd "$(dirname "$JAVACMD")" && pwd)"
    export JAVA_HOME="$(cd "$(dirname "$JAVABIN")" && pwd)"
  fi
fi

if [ -z "$JAVA_HOME" ]; then
  echo "Error: JAVA_HOME is not set. Please install JDK 21 and set JAVA_HOME." >&2
  exit 1
fi

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Error: maven-wrapper.jar not found. Run: curl -o .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar" >&2
  exit 1
fi

exec "$JAVA_HOME/bin/java" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
