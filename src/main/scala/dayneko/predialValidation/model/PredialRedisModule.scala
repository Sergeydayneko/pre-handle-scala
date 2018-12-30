package com.dayneko.predialValidation.model

import com.dayneko.shared.ConfigVariables._
import com.dayneko.shared.RedisModule

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import scala.collection.JavaConverters._

trait PredialRedisModule extends RedisModule {
  log.info(s"Connecting to Redis. HOST = $host, PORT = $port, DB Number = $db")

  /**
    * Saving contact in database.
    *
    * @param key consist of specified rule key values
    * @param duration for key in database
    * @param count value
    */
  def saveData(key: String, duration: Int, count: String): Unit = {
    val redis = pool.getResource
    log.info("Database pool has been successfully open for saving new number(saveContact method)")
    try {
      Some(key).filter(_.nonEmpty).foreach(k => {
        redis.hmset(k, Map(counter -> count).asJava)
        redis.expire(k, duration)
      })
      log.info("Key {} has been successfully added to black list", key)
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in saveContact method", e)
        throw e
    } finally {
      redis.close()
    }
  }

  /**
    * Check all key values of all request rules
    *
    * @param key for every rule
    * @param rule parameter from request
    * @return
    */
  def preProcessCheck(key: String, rule: Rule): LockStatus = {
    val redis: Jedis = pool.getResource
    log.info("Database pool has been successfully open for number checking(checkContact method)")

    try {
      Some(key).map(k => {
        if (redis.exists(k) && redis.hget(k, counter).toInt >= rule.counter) {
          log.info("Key {} already exists in black list", k)
          Lock(409, s"""{"GSW_CALL_RESULT":${rule.status}}""")
        }
        else if (redis.exists(k)) {
          log.info("Found Key {} with counter lower than in configuration", k)
          NotLock(k, redis.hget(k, counter), rule.duration)
        }
        else {
          log.info("Key {} hasn't been found in database", k)
          NotLock(k, "0", rule.duration)
        }
      }).getOrElse(LockServerError(409, s"""{"GSW_CALL_RESULT":$error_code}""" ))

    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in checkData method", e)
        throw e
    } finally {
      redis.close()
    }
  }

  /**
    * Increment current count value by one
    *
    * @param counter current counter value in String format
    * @return
    */
  def incrementCount(counter: String): String = (counter.toInt + 1).toString

  /**
    * Override depending on the type of list(black or white)
    * Update counter value in database.
    * It is understood that all rules response was 200 OK
    *
    * @param statuses is a list of first database check
    */
  def postProcessCounterUpdate(statuses: List[LockStatus]): Unit

  /**
    * Override depending on the type of list(black or white)
    *
    * @param statuses is a result from preProcessCheck() method
    * @return
    */
  def postProcessAct(statuses: List[LockStatus]): (Int, String)

}
