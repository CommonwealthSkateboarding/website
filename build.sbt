name := """cw"""

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)
resolvers += "jitpack" at "https://jitpack.io"

organization := "com.commonwealthskateboarding"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

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

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"

libraryDependencies += "com.feth" %% "play-authenticate" % "0.6.8"

libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.3.1"

libraryDependencies += "com.stripe" % "stripe-java" % "1.24.1"

libraryDependencies += "com.github.dhorions" % "boxable" % "1.2"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

pipelineStages in Assets := Seq(autoprefixer)
