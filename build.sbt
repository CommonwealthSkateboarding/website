name := """cw"""

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.21"

libraryDependencies += "com.feth" %% "play-authenticate" % "0.6.8"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)