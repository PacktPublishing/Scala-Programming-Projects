# Installation
## Zeppelin
- download Spark: https://www.apache.org/dyn/closer.lua/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz
- download Zeppelin: http://www.apache.org/dyn/closer.cgi/zeppelin/zeppelin-0.8.0/zeppelin-0.8.0-bin-all.tgz
```bash
cd /opt/zeppelin-0.8.0-bin-all/conf
mv zeppelin-env.sh.template zeppelin-env.sh
vi zeppelin-env.sh
export SPARK_HOME=/opt/spark-2.3.1-bin-hadoop2.7

cd /opt/spark-2.3.1-bin-hadoop2.7/jars
wget http://central.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.11/2.3.1/spark-sql-kafka-0-10_2.11-2.3.1.jar
wget http://central.maven.org/maven2/org/apache/kafka/kafka-clients/0.10.0.1/kafka-clients-0.10.0.1.jar

```

## Kafka
Start Zookeeper, Kafka, and a console consumer
```
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &


bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transactions --from-beginning
```
