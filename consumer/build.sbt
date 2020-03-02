ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "fraglab"
ThisBuild / organizationName := "home"

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := "home-consumer.jar",
  assembly / mainClass := Some("fraglab.HomeEnvironmentConsumer"),
  assembly / assemblyOption := (assemblyOption in assembly).value.copy(includeScala = true),
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "home-consumer",
    libraryDependencies ++= Dependencies.camel,
    libraryDependencies += Dependencies.config,
    libraryDependencies ++= Dependencies.logging
  )
  .settings(assemblySettings)

