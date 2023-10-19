#!/bin/sh

set -e

case $1 in
bash)
    shift
    exec bash $*
    ;;

make)
    cd /build
    mkdir -p /build/target/linux_64
    cd /build/target/linux_64 && cmake -DCMAKE_BUILD_TYPE=Release -G "Unix Makefiles" ../.. && make -j8
    ;;
esac
