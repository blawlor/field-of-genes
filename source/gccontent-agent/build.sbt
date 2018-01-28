name := "gccontent-agent"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.8"

val akkaStreamsVersion = "2.0.1"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
//  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
//  "com.typesafe.akka" %% "akka-stream" % "2.4.8",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
  "org.apache.kafka" %% "kafka" % "1.0.0" exclude("org.slf4j", "slf4j-log4j12"),
  "org.scalatest" %% "scalatest" % "2.1.6" % "test" ,
//  "org.mashupbots.socko" %% "socko-webserver" % "0.6.0",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ie.blawlor.fieldofgenes" % "gccontent" % "1.0-SNAPSHOT" excludeAll(
    ExclusionRule(organization = "ch.qos.logback"),
    ExclusionRule(organization = "org.apache.logging.log4j"),
    ExclusionRule(organization = "org.slf4j")
    ),
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)


assemblyMergeStrategy in assembly := {
  case PathList("ch.qos.logback", "logback-classic", xs @ _*)         => MergeStrategy.first
  case PathList("org.apache.logging.log4j", "log4j-slf4j-impl", xs @ _*)         => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("ie.blawlor.fieldofgenes.gccontent.agent.Main")
