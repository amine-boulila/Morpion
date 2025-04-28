#!/bin/bash
# Set JAVA_HOME to the JDK 8 installation path
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# Display the current Java version
echo "Now using:"
java -version