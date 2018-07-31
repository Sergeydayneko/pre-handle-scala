organization := "ru.dayneko"

name := "pre-handle"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "2.9.0",
  "javax.servlet" % "javax.servlet-api" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe" % "config" % "1.2.1",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.10" % Test
)

enablePlugins(TomcatPlugin)

webappWebInfClasses := true