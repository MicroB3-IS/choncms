#!/bin/bash

export MAVEN_OPTS="-Xmx1024M"

set -e

cd bnd-libs

echo Building bnd-libs
mvn clean install
cd ..

cd bundles
echo Building Core Bundles
mvn clean install
cd ..

cd wiki-feature-bundles/wiki-chon-libs
echo Building wiki-libs
mvn clean install
cd ..

echo Building wiki bundles
mvn clean install
cd ..

cd chon-rest-feature-bundles/chon-rest-libs
echo Building chon-rest-libs
mvn clean install
cd ..

echo Building chon rest bundles
mvn clean install
cd ..


cd chon-cms-web-container
echo Package war
mvn clean package

echo Going to build p2 site
cd ../../tools/chon-p2

mvn clean package


