resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M8")