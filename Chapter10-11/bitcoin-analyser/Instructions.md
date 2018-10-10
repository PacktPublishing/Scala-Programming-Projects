# Installation
## JDK
Download and install the latest JDK 1.8 from https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

## SBT
Follow the instructions here:
https://www.scala-sbt.org/1.0/docs/Setup.html

## Zeppelin
- download Zeppelin: http://www.apache.org/dyn/closer.cgi/zeppelin/zeppelin-0.7.3/zeppelin-0.7.3-bin-all.tgz

### Windows
We will install all tools in C:\opt . Feel free to use a different folder if you prefer.
- Create a directory C:\opt
- Install 7Zip to be able to decompress .tgz files: https://www.7-zip.org/download.html

Zeppelin depends on Spark which depends on Hadoop. On Windows, Hadoop requires additional binary utilities:
- Download https://github.com/steveloughran/winutils/archive/master.zip 
- Extract the directory hadoop-2.6.0 in master.zip to C:\opt\hadoop-2.6.0
- Extract zeppelin-0.7.3-bin-all.tgz to C:\opt\zeppelin-0.7.3-bin-all
- Edit C:\opt\zeppelin-0.7.3-bin-all\conf, and add the following line at the bottom:
set HADOOP_HOME=C:/opt/hadoop-2.6.0
- We need to fix some permissioning issue with the Hive folder:
```
cd C:\tmp\
mkdir hive
C:\opt\hadoop-2.6.0\bin\winutils.exe chmod 777 hive
```

Run Zeppelin:
```
cd C:\opt\zeppelin-0.7.3-bin-all\bin
zeppelin.cmd
```

### Linux / MacOs
We will install all tools in /opt. Feel free to use a different folder if you prefer.
```bash
sudo mkdir /opt && sudo chown $USER /opt
cd /opt
tar xfz ~/Downloads/zeppelin-0.7.3-bin-all.tgz 
cd /opt/zeppelin-0.7.3-bin-all
bin/zeppelin-daemon.sh start
```

### Post-installation steps (all platforms)
With your browser, go to http://localhost:8080 then click on the top right button -> Interpreter -> Spark -> Edit -> Dependencies

Add the following dependencies:
- org.apache.spark:spark-streaming-kafka-0-10_2.11:jar:2.1.0
- org.apache.kafka:kafka-clients:jar:0.10.2.2



## Kafka
- Download Kafka 1.1.1 https://www.apache.org/dyn/closer.cgi?path=/kafka/1.1.1/kafka_2.11-1.1.1.tgz

### Windows
- Extract kafka_2.11-1.1.1.tgz to C:\opt\kafka_2.11-1.1.1
- Start a Zookeeper in a command prompt:
```
cd C:\opt\kafka_2.11-1.1.1\bin\windows
zookeeper-server-start.bat ..\..\config\zookeeper.properties
```
- Start Kafka in another command prompt:
```
cd C:\opt\kafka_2.11-1.1.1\bin\windows
kafka-server-start.bat ..\..\config\server.properties
```
- Start a console consumer in another command prompt:
```
cd C:\opt\kafka_2.11-1.1.1\bin\windows
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transactions --from-beginning
```

### Linux / MacOs
- Extract kafka_2.11-1.1.1.tgz to /opt/kafka_2.11-1.1.1
- Start Zookeeper and Kafka in a terminal:
```
cd /opt/kafka_2.11-1.1.1
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &
```
- Start a console consumer in another terminal:
```
cd /opt/kafka_2.11-1.1.1
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transactions --from-beginning
```

# Running the Batch Producer
This will write the last day of transactions from midnight to the data directory, then write new transactions every hour.

Go to the project directory and run BatchProducerAppIntelliJ:
```
cd Scala-Programming-Projects/Chapter10-11/bitcoin-analyser
sbt 
test:runMain coinyser.BatchProducerAppIntelliJ
```

# Running the Streaming Producer
This will send live transactions to a kafka topic "transactions".
```
cd Scala-Programming-Projects/Chapter10-11/bitcoin-analyser
sbt 
test:runMain coinyser.StreamingProducerApp
```
