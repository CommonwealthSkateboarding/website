resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.4")

addSbtPlugin("com.heroku" % "sbt-heroku" % "0.3.4")

addSbtPlugin("com.github.mmizutani" % "sbt-play-gulp" % "0.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "2.0.0")