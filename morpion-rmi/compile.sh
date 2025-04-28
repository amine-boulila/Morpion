#!/bin/bash
# Exit immediately if a command exits with a non-zero status
set -e

# Source the JDK 8 environment setup script
source jdk8.sh

# Clean the 'bin' directory before building
rm -rf bin
mkdir -p bin

# Compile all Java files
javac -d bin -cp . src/shared/*.java src/model/*.java src/server/*.java src/client/*.java

# Check if compilation succeeded
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Generate RMI stubs for both server classes
rmic -d bin -classpath bin server.MorpionServer server.GameRoom

echo "Build complete with JDK 8"
ls -l bin/server/*.class