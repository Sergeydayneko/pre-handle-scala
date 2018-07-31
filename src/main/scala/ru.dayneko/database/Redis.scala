package database

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}
import utils.KeyVariables._
import utils.KeysRepository._

/**
  * Created by s.dayneko 03.07.2018
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

  /**
    * Save data from request
    * @param phcId - phc value
    * @param webId - web value
    * @param wsId - ws number
    * @return status of addition
    */
  def saveData(phcId: Option[String], webId: Option[String], wsId: String): Unit = {
    val redis = pool.getResource
    log.info("Database pool has been successfully open for saving new number(dataSave method)")
    try {
      webId.filter(_.nonEmpty).map(key(_, webPrefix, wsId)).foreach(k => {
        log.info("Saving number by web_key, ID of new key = {} , the time of existence = {}", k, exp_time_web)
        redis.setex(k, exp_time_web.toInt, curDate)
      })
      phcId.filter(_.nonEmpty).map(key(_, phcPrefix, wsId)).foreach(k => {
        log.info("Saving number by phc_key, ID of new key = {} , the time of existence = {}", k, exp_time_phc)
        redis.setex(k, exp_time_phc.toInt, curDate)
      })
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in dataSave method", e)
        throw e
    } finally {
      redis.close()
    }
  }

  /**
    * Check data retention in buffer
    * @param phcId - phc value
    * @param wsId - ws value
    * @param webId - web number
    * @return status of data in buffer
    */
  def checkData(phcId: Option[String], webId: Option[String], wsId: String): Option[Int] = {
    val redis = pool.getResource
    log.info("Database pool has been successfully open for number checking(dataCheck method)")
    try {
      webId.filter(c => redis.exists(key(c, webPrefix, wsId))).map(s => {
        log.info("Found a match by web_key = {}, number of response = {}", s, web_conflict)
        web_conflict
      }).orElse(
      phcId.filter(c => redis.exists(key(c, phcPrefix, wsId))).map(s => {
        log.info("Found a match by phc_key = {}, number of response  = {}", s, phc_conflict)
        phc_conflict
      }))
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in dataCheck method", e)
        throw e
    } finally {
      redis.close()
    }
  }
}
