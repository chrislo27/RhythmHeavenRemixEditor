#!/bin/zsh
cd -- "$(dirname "$0")"

java -XstartOnFirstThread -jar -Xmx1024m bin/RHRE.jar

echo 'Press any key to continue...'; read -k1 -s
