package com.dayneko.shared

import java.util
import java.util.concurrent.TimeUnit
import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._


object ConfigVariables {
  val config: Config      = ConfigFactory.load()
  val statuses: Config    = config.getConfig("statuses")
  val expirations: Config = config.getConfig("expirations")
  val attributes: Config  = config.getConfig("attributes")

  val counter: String       = Some("counter").filter(config.hasPath).map(config.getString).getOrElse("counter")
  val error_code: Int       = Some("error_code").filter(statuses.hasPath).map(statuses.getInt).getOrElse(47)
  val standard_code: Int    = Some("standard_code").filter(statuses.hasPath).map(statuses.getInt).getOrElse(55)
  val standard_time: Long   = Some("expiration_time").filter(expirations.hasPath).map(expirations.getDuration(_, TimeUnit.SECONDS)).getOrElse(86400)
  val standard_counter: Int = Some("standard_counter").filter(config.hasPath).map(config.getInt).getOrElse(3)

  val businessAttrsKeys: List[String] = attributes.getStringList("business").asScala.toList
  val systemAttrsKeys: List[String]   = attributes.getStringList("system").asScala.toList
  val rules: util.List[_ <: Config]   = config.getConfigList("fields")

}
