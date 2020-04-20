import sbt._

object Dependencies {

  object v {
    final val Config = "1.4.0"
    final val ScalaLogging = "3.9.2"
    final val LogbackVersion = "1.2.3"
    final val Slf4jVersion = "1.7.29"
    final val Camel = "3.1.0"
  }

  lazy val camel: Seq[ModuleID] = Seq("camel-core", "camel-kafka", "camel-influxdb")
    .map("org.apache.camel" % _ % v.Camel)

  lazy val config = "com.typesafe" % "config" % v.Config

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-classic" % v.LogbackVersion,
    "org.slf4j" % "slf4j-api" % v.Slf4jVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % v.ScalaLogging
  )

  val AkkaVersion = "2.5.23"
  lazy val alpakkaKafka = Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  )

}
