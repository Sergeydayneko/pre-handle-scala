package com.dayneko.shared

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

trait RedisModule {
  val log: Logger     = LoggerFactory.getLogger(this.getClass)
  val config: Config  = ConfigFactory.load.getConfig("redis")
  val host: String    = "127.0.0.1"
  val port: Int       = 6379
  val db: Int         = Some("db").filter(config.hasPath).map(config.getInt).getOrElse(3)
  val pool: JedisPool = new JedisPool(new JedisPoolConfig(), host, port, 60 * 1000, null)
}
