name := """cw"""

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)
resolvers += "jitpack" at "https://jitpack.io"

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

herokuAppName in Compile := "lastplace"

val appDependencies = Seq(
  evolutions,
  cache,
  javaWs,
  javaJdbc
)

scalaVersion := "2.11.7"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "3.0.1"

libraryDependencies += "com.feth" %% "play-authenticate" % "0.6.8"

libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.4.3"

libraryDependencies += "com.stripe" % "stripe-java" % "2.7.0"

libraryDependencies += "com.github.dhorions" % "boxable" % "1.4"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node


lazy val root = project.in(file(".")).dependsOn(sbtAutoprefixer).enablePlugins(PlayJava, PlayEbean).settings(libraryDependencies ++= appDependencies)
lazy val sbtAutoprefixer = uri("git://github.com/mkurz/sbt-autoprefixer")
