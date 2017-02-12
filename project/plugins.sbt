resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

lazy val root = project.in(file(".")).dependsOn(sbtAutoprefixer)
lazy val sbtAutoprefixer = uri("git://github.com/cdelargy/sbt-autoprefixer-temp")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M8")