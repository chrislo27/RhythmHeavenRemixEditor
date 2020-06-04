#!/bin/bash
cd "$(dirname "$0")"

java -jar -Xmx1024m bin/RHRE.jar

read -n1 "Press any key to continue..."
