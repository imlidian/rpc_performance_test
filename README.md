


启动client和server
/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar pojo-client-1.0.0-m1.jar
/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar pojo-server-1.0.0-m1.jar


***microservice.ymal 配置云计算内网地址 server端yaml的highway和rest配置本机内网地址

运行sc （内网地址）
docker run -d -p 45.55.29.141:30100:30100 servicecomb/service-center:latest
/10.134.14.246

环境配置
add-apt-repository ppa:webupd8team/java
apt-get update
apt-get install oracle-java8-installer
apt-get install oracle-java9-installer

https://blog.csdn.net/huanhuanq1209/article/details/72673015

docker pull servicecomb/service-center
docker run -d -p 10.134.27.52:30100:30100 servicecomb/service-center:latest
docker run -d -p 192.168.0.224:30100:30100 servicecomb/service-center:latest


