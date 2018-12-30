package com.dayneko.crmValidation.database

import com.dayneko.shared.RedisModule
import redis.clients.jedis.Jedis

object CrmRedis extends RedisModule {
  def get(key: String): String = {
    val redis: Jedis = pool.getResource

    try {
      redis.get(key)
    }
    catch {
      case e: Exception =>
        log.warn("Exception into CrmRedis in method get. Exception = {}", e)
        throw e
    } finally {
      redis.close()
    }
  }
}
