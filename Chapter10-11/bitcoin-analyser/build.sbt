name := "bitcoin-analyser"

version := "0.1"

scalaVersion := "2.11.11"
val sparkVersion = "2.3.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier "test-sources",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "test-sources",
  "org.apache.spark" %% "spark-catalyst" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-catalyst" % sparkVersion % Test classifier "test-sources",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % Provided exclude ("net.jpountz.lz4", "lz4"),
  "com.pusher" % "pusher-java-client" % "1.8.0")

scalacOptions += "-Ypartial-unification"

// Avoids SI-3623
target := file("/tmp/sbt/bitcoin-analyser")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
test in assembly := {}
mainClass in assembly := Some("coinyser.BatchProducerAppSpark")
