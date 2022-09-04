#!/bin/bash

export CLASSPATH=$CLASSPATH:../build/jars/jftp.jar

javac *.java

java FtpDownload
