#!/usr/bin/env sh
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
CLASSPATH="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle wrapper JAR no encontrado. Ejecuta gradle wrapper o instala Gradle localmente."
  exit 1
fi
exec java -jar "$CLASSPATH" "$@"
