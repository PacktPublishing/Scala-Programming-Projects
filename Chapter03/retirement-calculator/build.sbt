name := "retirement-calculator"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
scalacOptions += "-Ypartial-unification"

mainClass in Compile := Some("retcalc.SimulatePlanApp")