scalaVersion := "2.12.8"
version := "0.2.0-SNAPSHOT"
organization := "fraglab"
organizationName := "home"
maintainer := "yannis@frlab.eu"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "home-consumer",
    libraryDependencies ++= Dependencies.camel,
    libraryDependencies += Dependencies.config,
    libraryDependencies ++= Dependencies.logging
  )

