name := """cw"""

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)
resolvers += "jitpack" at "https://jitpack.io"

// For play-modules-redis
//resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

herokuAppName in Compile := "lastplace"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"

libraryDependencies += "com.feth" %% "play-authenticate" % "0.6.8"

libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.3.1"

libraryDependencies += "com.stripe" % "stripe-java" % "1.24.1"

libraryDependencies += "com.github.dhorions" % "boxable" % "1.2"

// Tried this as a solution to the EhCache 
// libraryDependencies += "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.0"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  cache,
  javaWs
)