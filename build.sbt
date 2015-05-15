name := "Tags"

version := "0.0.1-SNAPSHOT"

organization := "com.aire"

lazy val pie = (project in file("."))

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-feature", "-Xlint", "-Xfatal-warnings", "-language:reflectiveCalls", "-deprecation")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

libraryDependencies++= Seq("org.scala-lang" % "scala-reflect" % "2.11.1")