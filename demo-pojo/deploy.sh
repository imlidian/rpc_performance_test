#!/bin/bash
D="d"
UP="u"
INSTALL="i"
ALL="a"
#export PATH1="/Volumes/256G/imlidian/chassis/incubator-servicecomb-java-chassis"
export PATH1="/Volumes/256G/imlidian/Chasiss"
export SJAR="pojo-server-1.0.0-m2-SNAPSHOT.jar"
export CJAR="pojo-client-1.0.0-m2-SNAPSHOT.jar"

#/Volumes/256G/imlidian/Chasiss/demo/demo-pojo

#echo $PATH1/demo/demo-pojo/benchmark-base/target/benchmark-base-round-1.jar

if [ $INSTALL = "1" -o $ALL = "a" ];then
#rm -rf /Users/dean/.m2/repository/benchmark/rpc/benchmark-base/round-1/benchmark-base-round-1.jar
#m -rf /Users/dean/.m2/repository/org/apache/servicecomb/provider-pojo/
#rm -rf /Users/dean/.m2/repository/org/apache/servicecomb/demo/pojo-client
#rm -rf /Users/dean/.m2/repository/org/apache/servicecomb/demo/pojo-server
#rm -rf $PATH1/demo/demo-pojo/benchmark-base/target/benchmark-base-round-1.jar
rm -rf $PATH1/demo/target/Java\ Chassis\:\:Demo\:\:POJO\:\:Client/$CJAR
rm -rf $PATH1/demo/target/Java\ Chassis\:\:Demo\:\:POJO\:\:Server/$SJAR
cd $PATH1/demo/demo-pojo
mvn clean install -Pdemo-run-release
fi

if [ $D = "d" -o $ALL = "a" ];then
ssh root@hwc2 "cd java ; ls ;rm $CJAR ; killall java"
ssh root@hws "cd java ; ls ;rm $SJAR ; killall Java"
fi
if [ $UP = "u" -o $ALL = "a" ];then
scp $PATH1/demo/target/Java\ Chassis\:\:Demo\:\:POJO\:\:Client/$CJAR root@hwc2:~/java  #hwc2
scp $PATH1/demo/target/Java\ Chassis\:\:Demo\:\:POJO\:\:Server/$SJAR root@hws:~/java  #hws
scp $PATH1/demo/demo-pojo/benchmark-base/target/benchmark-base-round-1.jar root@hwc2:~/java
scp $PATH1/demo/demo-pojo/benchmark-base/target/benchmark-base-round-1.jar root@hws:~/java
fi