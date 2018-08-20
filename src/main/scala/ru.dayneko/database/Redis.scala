package ru.dayneko.database

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}
import ru.dayneko.model.Rule
import ru.dayneko.utils.KeysRepository._


/**
  * Created by Dayneko 03.07.2018
  */
object Redis {
  val log: Logger    = LoggerFactory.getLogger(this.getClass)
  val config: Config = ConfigFactory.load.getConfig("redis")
  val host: String   = "127.0.0.1"
  val port: Int      = 6379
  val db: Int        = Some("db").filter(config.hasPath).map(config.getInt).getOrElse(3)
  val redisConfig    = new JedisPoolConfig()
  val pool           = new JedisPool(redisConfig, host, port, 60 * 1000, null)

  log.info(s"Connecting to Redis. HOST = $host, PORT = $port, DB Number = $db")

  def saveData(key: String, duration: Int, count: String): Unit = {
    val redis = pool.getResource
    log.info("Database pool has been successfully open for saving new number(saveContact method)")
    try {
      Some(key).filter(_.nonEmpty).foreach(redis.setex(_, duration, count))
      log.info("Key {} has been successfully added to black list", key)
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in saveContact method", e)
        throw e
    } finally {
      redis.close()
    }
  }


  def checkData(key: String, rule: Rule): dbResult = {
    val redis = pool.getResource
    log.info("Database pool has been successfully open for number checking(checkContact method)")
    try {
      Some(key).map(k => {
        if (redis.exists(k) && redis.get(k).toInt >= rule.counter) {
          log.info("Key {} already exists in black list", k)
          Lock(rule.status)
        }
        else if (redis.exists(k)) {
          redis.setex(k, rule.duration, (redis.get(k).toInt + 1).toString)
          log.info("Key {} has counter value less than in configuration and has been iterated by 1", k)
          NotLock()
        }
        else {
          redis.setex(k, rule.duration, "1")
          log.info("Key {} doesn't exist in database and has been added successfully with counter = 1", k)
          NotLock()
        }
      }).getOrElse(LockServerError(errorCode))

    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in checkData method", e)
        throw e
    } finally {
      redis.close()
    }
  }
}

sealed abstract class dbResult
case class Lock(statusCode: Int) extends dbResult
case class NotLock() extends dbResult
case class LockServerError(statusCode: Int) extends dbResult
