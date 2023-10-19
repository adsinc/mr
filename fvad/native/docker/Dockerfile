FROM parrotstream/ubuntu-java:xenial-8

RUN \
    apt-get update && \
    apt-get install -y automake cmake git gcc g++ make libtool && \
    rm -rf /var/lib/apt/lists/*

ADD run-build.sh /run-build.sh
RUN chmod a+x /run-build.sh
ENTRYPOINT ["/run-build.sh"]
CMD ["make"]
