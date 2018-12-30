package com.dayneko.predialValidation.whitelist.database

import com.dayneko.predialValidation.model._
import com.dayneko.shared.ConfigVariables._
import redis.clients.jedis.Jedis
import scala.collection.JavaConverters._

/**
  * Created by Dayneko 03.07.2018
  */
object WhitelistRedis extends PredialRedisModule {
  // сделать pool, добавив отдельные номера баз данных под black и white листы
  override val db: Int = 2

  override def postProcessAct(statuses: List[LockStatus]): (Int, String) = {
    if (statuses.forall(_.isInstanceOf[Lock])) {
      statuses.find(_.isInstanceOf[Lock])
        .map(s => s.asInstanceOf[Lock])
        .map(stat => (stat.responseCode, stat.text))
        .getOrElse(409, s"""{"GSW_CALL_RESULT":$error_code}""")
    } else (200, "{}")
  }

  override def postProcessCounterUpdate(statuses: List[LockStatus]): Unit = {
    val redis: Jedis = pool.getResource
    log.info("Database pool has been successfully open for counter update(postProcessCounterUpdate method)")

    try {
      statuses
        .map(s => s.asInstanceOf[NotLock])
        .foreach(s => {
          redis.hmset(s.key, Map(counter -> incrementCount(s.counter)).asJava)
          redis.expire(s.key, s.duration)
          log.info("Counter was successfully updated for key with value {}", s.key)
        })
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in postProcessCounterUpdate method", e)
    } finally {
      redis.close()
    }
  }

}


