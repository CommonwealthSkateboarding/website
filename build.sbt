name := """cw"""

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)
resolvers += "jitpack" at "https://jitpack.io"

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean, SbtWeb)

enablePlugins(sbtdocker.DockerPlugin)
enablePlugins(JavaServerAppPackaging)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("java")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir)
  }
}

scalaVersion := "2.11.7"
libraryDependencies += evolutions
libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"
libraryDependencies += "com.feth" %% "play-authenticate" % "0.7.1"
libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.4.0"
libraryDependencies += "com.stripe" % "stripe-java" % "1.24.1"
libraryDependencies += "com.github.dhorions" % "boxable" % "1.2"
// libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

// Force Set Dependencies 
// Netty
dependencyOverrides += "io.netty" % "netty" % "3.10.4.Final"
// Guava
dependencyOverrides += "com.google.guava" % "guava" % "16.0"
// Jboss Logging
dependencyOverrides += "org.jboss.logging" % "jboss-logging" % "3.2.1.Final"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  cache,
  javaWs
)