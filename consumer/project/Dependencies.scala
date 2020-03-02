import sbt._

object Dependencies {

  object v {
    final val Kafka = "2.4.0"
    final val Influx = "2.17"
    final val Config = "1.4.0"
    final val ScalaLogging = "3.9.2"
    final val LogbackVersion = "1.2.3"
    final val Slf4jVersion = "1.7.29"
  }

  lazy val kafka = "org.apache.kafka" %% "kafka" % v.Kafka
  lazy val influx = "org.influxdb" % "influxdb-java" % v.Influx
  lazy val config = "com.typesafe" % "config" % v.Config

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-classic" % v.LogbackVersion,
    "org.slf4j" % "slf4j-api" % v.Slf4jVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % v.ScalaLogging
  )

}
