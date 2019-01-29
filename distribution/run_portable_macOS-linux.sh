#!/bin/bash

java -version

printf "\nYou NEED Java 1.8, at least. If the version above is not 1.8+, or if you get an UnsupportedClassVersionError, update Java.\n\n"

java -jar RHRE.jar --portable-mode

read -n1 "Press any key to continue..."
