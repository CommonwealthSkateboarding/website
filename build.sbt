name := """cw"""

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

herokuAppName in Compile := "lastplace"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.4"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"

libraryDependencies += "com.feth" %% "play-authenticate" % "0.6.8"

libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.3.1"

libraryDependencies += "com.newrelic.agent.java" % "newrelic-agent" % "3.12.1"

libraryDependencies += "com.stripe" % "stripe-java" % "1.24.1"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

pipelineStages in Assets := Seq(autoprefixer)