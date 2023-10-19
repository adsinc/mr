#!/bin/bash

set -e

# Locate sources and destination files
CURRENT_DIR=`pwd`
FWD=`dirname $0`
cd $FWD/..
PROJECT_DIR=`pwd`

echo "Running OS X build..."
cd $PROJECT_DIR
mkdir -p $PROJECT_DIR/native/target/osx_64 && cd $PROJECT_DIR/native/target/osx_64
cmake -DCMAKE_BUILD_TYPE=Release -G "CodeBlocks - Unix Makefiles" ../..
make -j8

echo "Running Linux build in Docker..."
# Make docker container with devtools
cd $PROJECT_DIR/native/docker
docker build . -t fvad-native-build
cd $PROJECT_DIR/native
docker run --rm -v $PROJECT_DIR/native:/build fvad-native-build make

echo "Copying artifacts..."
mkdir -p  -v $PROJECT_DIR/src/main/resources/natives/osx_64
cp  $PROJECT_DIR/native/target/osx_64/libfvad-jni.dylib $PROJECT_DIR/src/main/resources/natives/osx_64/
mkdir -p  -v $PROJECT_DIR/src/main/resources/natives/linux_64
cp  $PROJECT_DIR/native/target/linux_64/libfvad-jni.so  $PROJECT_DIR/src/main/resources/natives/linux_64/
ls -laR $PROJECT_DIR/src/main/resources/natives

cd $CURRENT_DIR
