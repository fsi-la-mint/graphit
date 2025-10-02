#!/usr/bin/env bash
set -euo pipefail

# Config
MAIN="${1:-Main}"                       # Entry class name (default: Main)
ROOT="$(cd "$(dirname "$0")" && pwd)"
EXAMPLES_DIR="$ROOT/examples/oneFilePlugNPlay"
DEST_DIR="$ROOT/out"

# 1) Run builder from project root
cd "$ROOT"
echo "Building project..."
python builder.py
echo "Done."

# 2) Switch to examples dir and compile to destination
cd "$EXAMPLES_DIR"
mkdir -p "$DEST_DIR"
echo "Compiling example..."
javac -encoding UTF-8 -d "$DEST_DIR" *.java

# 3) Switch to destination and run
cd "$DEST_DIR"
java "$MAIN"