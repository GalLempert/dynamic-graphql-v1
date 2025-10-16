#!/usr/bin/env bash
set -euo pipefail

BASEDIR=$(cd "$(dirname "$0")"; pwd -P)
WRAPPER_DIR="$BASEDIR/.mvn/wrapper"
PROPS_FILE="$WRAPPER_DIR/maven-wrapper.properties"
if [ ! -f "$PROPS_FILE" ]; then
  echo "Missing maven-wrapper.properties" >&2
  exit 1
fi

DIST_URL=$(grep -E '^distributionUrl=' "$PROPS_FILE" | cut -d'=' -f2-)
if [ -z "$DIST_URL" ]; then
  echo "distributionUrl not set in $PROPS_FILE" >&2
  exit 1
fi

DIST_NAME=$(basename "$DIST_URL" .zip)
INSTALL_DIR="$WRAPPER_DIR/$DIST_NAME"
ARCHIVE="$WRAPPER_DIR/$DIST_NAME.zip"

if [ ! -d "$INSTALL_DIR" ]; then
  mkdir -p "$WRAPPER_DIR"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then
      if ! curl -fsSL "$DIST_URL" -o "$ARCHIVE"; then
        echo "Failed to download Maven from $DIST_URL" >&2
        exit 1
      fi
    elif command -v wget >/dev/null 2>&1; then
      if ! wget -q "$DIST_URL" -O "$ARCHIVE"; then
        echo "Failed to download Maven from $DIST_URL" >&2
        exit 1
      fi
    else
      echo "Neither curl nor wget is available to download Maven" >&2
      exit 1
    fi
    fi
  fi
  unzip -q "$ARCHIVE" -d "$WRAPPER_DIR"
fi

MVN_CMD="$INSTALL_DIR/bin/mvn"
if [ ! -x "$MVN_CMD" ]; then
  echo "Maven executable not found at $MVN_CMD" >&2
  exit 1
fi

exec "$MVN_CMD" "$@"
