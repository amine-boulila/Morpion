#!/bin/bash
# Source the JDK 8 environment setup script
source jdk8.sh

# Run the MorpionServer Java program
java -cp bin -Djava.rmi.server.codebase=file:bin/ server.MorpionServer