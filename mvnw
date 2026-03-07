#!/bin/sh
set -e

MVNW_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_DIR="$MVNW_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPS="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPS" ]; then
  echo "[ERROR] No se encontro $WRAPPER_PROPS"
  exit 1
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "[INFO] Descargando Maven Wrapper JAR..."
  WRAPPER_URL="$(grep '^wrapperUrl=' "$WRAPPER_PROPS" | cut -d'=' -f2-)"
  [ -z "$WRAPPER_URL" ] && WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  mkdir -p "$WRAPPER_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "[ERROR] Se requiere curl o wget para descargar el wrapper."
    exit 1
  fi
fi

JAVA_EXE="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$JAVA_EXE" -Dmaven.multiModuleProjectDirectory="$MVNW_DIR" -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
